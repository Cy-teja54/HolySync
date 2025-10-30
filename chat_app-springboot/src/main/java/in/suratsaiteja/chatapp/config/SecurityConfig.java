package in.suratsaiteja.chatapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // Disable CSRF for API endpoints
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login.html", "/signup.html", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/api/users/**").permitAll()  // Allow access to user API endpoints
                .requestMatchers("/api/google-tasks/**").permitAll()  // Allow access to Google Tasks endpoints
                .requestMatchers("/websocket/**").permitAll()  // Allow WebSocket connections
                .requestMatchers("/oauth-callback.html").permitAll()  // Allow OAuth callback
                .requestMatchers("/chat.html").permitAll()  // Allow chat page access
                .anyRequest().permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login.html")
                .defaultSuccessUrl("/chat.html", true)
            );
        
        return http.build();
    }
}