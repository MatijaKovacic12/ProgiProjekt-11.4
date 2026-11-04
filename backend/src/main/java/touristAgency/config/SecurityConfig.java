package touristAgency.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import touristAgency.models.Role;
import touristAgency.services.CurrentUserService;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           OAuth2LoginSuccessHandler successHandler,
                                           CurrentUserService currentUser) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // /api/admin/** smije samo ADMIN (uloga se čita iz baze, vidi CurrentUserService)
                        .requestMatchers("/api/admin/**").access((authentication, context) ->
                                new AuthorizationDecision(currentUser.hasRole(Role.ADMIN)))
                        // rezervacije zahtijevaju prijavu
                        .requestMatchers("/api/reservations/**").authenticated()
                        .anyRequest().permitAll())
                // API vraća 401 umjesto preusmjeravanja na stranicu za prijavu
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .oauth2Login(oauth -> oauth.successHandler(successHandler))
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> response.setStatus(200)));
        return http.build();
    }
}
