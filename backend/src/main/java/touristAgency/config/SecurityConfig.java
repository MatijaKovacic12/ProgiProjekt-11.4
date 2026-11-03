package touristAgency.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, OAuth2LoginSuccessHandler successHandler) throws  Exception{
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((auth->auth.anyRequest().permitAll()))
                .oauth2Login(oauth -> oauth.successHandler(successHandler))
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> response.setStatus(200)));
        return http.build();
    }
}
