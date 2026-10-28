package touristAgency.unitTests.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import touristAgency.config.OAuth2LoginSuccessHandler;
import touristAgency.services.AuthService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OAuth2LoginSuccessHandlerTest {
    private final AuthService authService = mock(AuthService.class);
    private final OAuth2LoginSuccessHandler handler = new OAuth2LoginSuccessHandler(authService, "http://localhost:5173");

    @Test
    void onAuthenticationSuccess_savesUserAndRedirectsToFrontend() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(new MockHttpServletRequest(), response, googleLogin());

        verify(authService).loginOrRegister("google", "sub-1", "ana@test.hr", "Ana", "Horvat", true);
        assertEquals("http://localhost:5173/tours", response.getRedirectedUrl());
    }

    @Test
    void onAuthenticationSuccess_redirectsToLoginWithErrorWhenLoginFails() throws Exception {
        when(authService.loginOrRegister("google", "sub-1", "ana@test.hr", "Ana", "Horvat", true))
                .thenThrow(new IllegalStateException("Email is not verified by provider"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(new MockHttpServletRequest(), response, googleLogin());

        assertEquals("http://localhost:5173/login?error=true", response.getRedirectedUrl());
    }

    private OAuth2AuthenticationToken googleLogin() {
        OidcIdToken idToken = OidcIdToken.withTokenValue("token")
                .subject("sub-1")
                .claim("email", "ana@test.hr")
                .claim("email_verified", true)
                .claim("given_name", "Ana")
                .claim("family_name", "Horvat")
                .build();
        OidcUser user = new DefaultOidcUser(List.of(), idToken);
        return new OAuth2AuthenticationToken(user, List.of(), "google");
    }
}
