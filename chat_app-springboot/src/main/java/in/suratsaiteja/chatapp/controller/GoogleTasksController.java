package in.suratsaiteja.chatapp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/google-tasks")
@CrossOrigin(origins = "*")
public class GoogleTasksController {

	@Value("${google.oauth.client-id:}")
	private String clientId;

	@Value("${google.oauth.client-secret:}")
	private String clientSecret;

	@Value("${google.oauth.redirect-uri:http://localhost:8080/oauth-callback.html}")
	private String redirectUri;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@GetMapping(path = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Object> health() {
		Map<String, Object> resp = new HashMap<>();
		resp.put("status", "ok");
		return resp;
	}

    @GetMapping(path = "/auth-url", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAuthUrl(
            @RequestParam(value = "clientId", required = false) String clientIdOverride,
            @RequestParam(value = "redirectUri", required = false) String redirectUriOverride) {

        String effectiveClientId = (clientIdOverride != null && !clientIdOverride.isBlank()) ? clientIdOverride : clientId;
        String effectiveRedirect = (redirectUriOverride != null && !redirectUriOverride.isBlank()) ? redirectUriOverride : redirectUri;

        boolean clientIdMissing = (effectiveClientId == null || effectiveClientId.isBlank() || "your_client_id_here".equalsIgnoreCase(effectiveClientId));
        boolean clientSecretMissing = (clientSecret == null || clientSecret.isBlank() || "your_client_secret_here".equalsIgnoreCase(clientSecret));
        boolean redirectMissing = (effectiveRedirect == null || effectiveRedirect.isBlank());
		if (clientIdMissing || clientSecretMissing || redirectMissing) {
			Map<String, Object> err = new HashMap<>();
			err.put("error", "Missing or invalid Google OAuth configuration.");
			err.put("details", Map.of(
                    "GOOGLE_CLIENT_ID", clientIdMissing ? "missing_or_placeholder" : mask(effectiveClientId),
				"GOOGLE_CLIENT_SECRET", clientSecretMissing ? "missing_or_placeholder" : "ok",
                    "google.oauth.redirect-uri", redirectMissing ? "missing" : effectiveRedirect
		));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
		}

        String scope = urlEncode("https://www.googleapis.com/auth/tasks");
        String redirect = urlEncode(effectiveRedirect);
		String state = "chatapp-tasks";
		String authUrl = "https://accounts.google.com/o/oauth2/v2/auth" +
				"?response_type=code" +
                "&client_id=" + urlEncode(effectiveClientId) +
				"&redirect_uri=" + redirect +
				"&scope=" + scope +
				"&access_type=offline" +
				"&include_granted_scopes=true" +
                "&prompt=consent" +
                "&state=" + urlEncode(state);

		Map<String, Object> resp = new HashMap<>();
		resp.put("authUrl", authUrl);
        resp.put("debug", Map.of(
                "clientIdSuffix", mask(effectiveClientId),
                "redirectUri", effectiveRedirect
        ));
		return ResponseEntity.ok(resp);
	}

	@PostMapping(path = "/callback", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> handleCallback(@RequestBody Map<String, Object> body) {
		Object code = body.get("code");
		if (code == null || String.valueOf(code).isBlank()) {
			Map<String, Object> err = new HashMap<>();
			err.put("error", "Missing authorization code");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
		}

		try {
			Map<String, Object> tokenResp = exchangeCodeForTokens(String.valueOf(code));
			Map<String, Object> resp = new HashMap<>();
			resp.put("accessToken", tokenResp.get("access_token"));
			resp.put("refreshToken", tokenResp.get("refresh_token"));
			resp.put("expiresIn", tokenResp.get("expires_in"));
			return ResponseEntity.ok(resp);
		} catch (Exception e) {
			Map<String, Object> err = new HashMap<>();
			err.put("error", "OAuth token exchange failed: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
		}
	}

    @PostMapping(path = "/export", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> exportTasks(@RequestBody Map<String, Object> body) {
		Object accessToken = body.get("accessToken");
		Object tasksObj = body.get("tasks");
		if (accessToken == null || String.valueOf(accessToken).isBlank()) {
			Map<String, Object> err = new HashMap<>();
			err.put("error", "Missing accessToken");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
		}
		if (!(tasksObj instanceof List)) {
			Map<String, Object> err = new HashMap<>();
			err.put("error", "Invalid tasks payload");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
		}

        int created = 0;
        int failed = 0;
        Map<Integer, Object> failures = new HashMap<>();
		try {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> tasks = (List<Map<String, Object>>) tasksObj;
			for (Map<String, Object> t : tasks) {
				String title = Objects.toString(t.get("content"), "Untitled Task");
                String dueRfc3339 = toRfc3339(t.get("dueDate"));
                Map<String, Object> result = createGoogleTask(String.valueOf(accessToken), title, dueRfc3339);
                if (Boolean.TRUE.equals(result.get("ok"))) {
                    created++;
                } else {
                    failed++;
                    failures.put(created + failed, result);
                }
			}
			Map<String, Object> resp = new HashMap<>();
			resp.put("createdCount", created);
            resp.put("failedCount", failed);
            if (!failures.isEmpty()) {
                resp.put("failures", failures);
            }
			return ResponseEntity.ok(resp);
		} catch (Exception e) {
			Map<String, Object> err = new HashMap<>();
			err.put("error", "Export failed: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
		}
	}

	private Map<String, Object> exchangeCodeForTokens(String code) throws Exception {
		String tokenUrl = "https://oauth2.googleapis.com/token";
		String payload = "code=" + urlEncode(code)
				+ "&client_id=" + urlEncode(clientId)
				+ "&client_secret=" + urlEncode(clientSecret)
				+ "&redirect_uri=" + urlEncode(redirectUri)
				+ "&grant_type=authorization_code";

		byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
		HttpURLConnection conn = (HttpURLConnection) new URL(tokenUrl).openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setDoOutput(true);
		conn.getOutputStream().write(bytes);

		int status = conn.getResponseCode();
		if (status >= 200 && status < 300) {
			Map<String, Object> result = objectMapper.readValue(conn.getInputStream(), new TypeReference<Map<String, Object>>(){});
			conn.disconnect();
			return result;
		} else {
			Map<String, Object> err = objectMapper.readValue(conn.getErrorStream(), new TypeReference<Map<String, Object>>(){});
			conn.disconnect();
			throw new IllegalStateException("Token endpoint error: " + err);
		}
	}

    private Map<String, Object> createGoogleTask(String accessToken, String title, String dueRfc3339) throws Exception {
		String url = "https://tasks.googleapis.com/tasks/v1/lists/@default/tasks";
		Map<String, Object> body = new HashMap<>();
		body.put("title", title);
		if (dueRfc3339 != null) {
			body.put("due", dueRfc3339);
		}
		byte[] json = objectMapper.writeValueAsBytes(body);

		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Authorization", "Bearer " + accessToken);
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setDoOutput(true);
		conn.getOutputStream().write(json);

		int status = conn.getResponseCode();
        Map<String, Object> result = new HashMap<>();
        if (status >= 200 && status < 300) {
            try { if (conn.getInputStream() != null) conn.getInputStream().close(); } catch (Exception ignored) {}
            conn.disconnect();
            result.put("ok", true);
            return result;
        } else {
            Object errorBody = null;
            try {
                if (conn.getErrorStream() != null) {
                    errorBody = objectMapper.readValue(conn.getErrorStream(), new TypeReference<Map<String, Object>>(){});
                }
            } catch (Exception ignored) {}
            conn.disconnect();
            result.put("ok", false);
            result.put("status", status);
            result.put("error", errorBody != null ? errorBody : "Google API error");
            return result;
        }
	}

    private static String toRfc3339(Object dueDateObj) {
		if (dueDateObj == null) return null;
		try {
			// Handle ISO string
			if (dueDateObj instanceof String) {
				// If it already looks like RFC3339, return as-is
                String s = ((String) dueDateObj).trim();
                // If just a date (YYYY-MM-DD), normalize to midnight UTC
                if (s.matches("\\d{4}-\\d{2}-\\d{2}$")) {
                    return s + "T00:00:00Z";
                }
                // If date without timezone but with time, append Z
                if (s.matches("\\d{4}-\\d{2}-\\d{2}T[0-9:.]+$")) {
                    return s + "Z";
                }
                // If it already has timezone or Z, pass through
                if (s.matches("\\d{4}-\\d{2}-\\d{2}.*Z$") || s.matches(".*[+-][0-9]{2}:[0-9]{2}$")) {
                    return s;
                }
				// Try parse as millis string
				long ms = Long.parseLong(s);
				return DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(ms));
			}
			// Handle numeric epoch millis
			if (dueDateObj instanceof Number) {
				long ms = ((Number) dueDateObj).longValue();
				return DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(ms));
			}
		} catch (Exception ignored) {}
		// Fallback: no due
		return null;
	}

	private static String urlEncode(String v) {
		return URLEncoder.encode(v, StandardCharsets.UTF_8);
	}

    private static String mask(String s) {
        if (s == null || s.length() < 8) return "****";
        return "***" + s.substring(s.length() - 8);
    }
}


