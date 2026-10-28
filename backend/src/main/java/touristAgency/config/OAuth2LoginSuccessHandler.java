package touristAgency.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import touristAgency.services.AuthService;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final AuthService authService;
    private final String frontendUrl;

    public OAuth2LoginSuccessHandler(AuthService authService, @Value("${app.frontend-url}") String frontendUrl) {
        this.authService = authService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OidcUser oidcUser = (OidcUser) token.getPrincipal();

        // Microsoft sometimes doesn't send "email", then preferred_username is the email
        String email = oidcUser.getEmail() != null ? oidcUser.getEmail() : oidcUser.getPreferredUsername();

        // Microsoft sends only full name ("name"), Google sends given_name and family_name
        String firstName = oidcUser.getGivenName();
        String lastName = oidcUser.getFamilyName();
        if (firstName == null && oidcUser.getFullName() != null) {
            String[] parts = oidcUser.getFullName().trim().split(" ", 2);
            firstName = parts[0];
            lastName = parts.length > 1 ? parts[1] : null;
        }

        // Google sends email_verified, Microsoft sends xms_edov (if enabled in Azure token configuration)
        boolean emailVerified = Boolean.TRUE.equals(oidcUser.getEmailVerified())
                || Boolean.TRUE.equals(oidcUser.getClaimAsBoolean("xms_edov"));

        try {
            if (email == null || !email.contains("@")) {
                throw new IllegalStateException("Provider did not send an email");
            }
            authService.loginOrRegister(
                    token.getAuthorizedClientRegistrationId(),
                    oidcUser.getSubject(),
                    email,
                    firstName,
                    lastName,
                    emailVerified);
        } catch (IllegalStateException e) {
            request.getSession().invalidate();
            response.sendRedirect(frontendUrl + "/login?error=true");
            return;
        }

        response.sendRedirect(frontendUrl + "/tours");
    }
}
