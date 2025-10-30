package in.suratsaiteja.chatapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Component
public class GoogleOAuthConfig {
    
    @Value("${GOOGLE_CLIENT_ID}")
    private String clientId;
    
    @Value("${GOOGLE_CLIENT_SECRET}")
    private String clientSecret;
    
    @Value("${google.oauth.redirect-uri:http://localhost:8080/oauth-callback.html}")
    private String redirectUri;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }
}