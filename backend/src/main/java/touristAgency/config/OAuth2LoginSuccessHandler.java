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

        try {
            authService.loginOrRegister(
                    token.getAuthorizedClientRegistrationId(),
                    oidcUser.getSubject(),
                    oidcUser.getEmail(),
                    oidcUser.getGivenName(),
                    oidcUser.getFamilyName(),
                    Boolean.TRUE.equals(oidcUser.getEmailVerified()));
        } catch (IllegalStateException e) {
            request.getSession().invalidate();
            response.sendRedirect(frontendUrl + "/login?error=true");
            return;
        }

        response.sendRedirect(frontendUrl + "/tours");
    }
}
