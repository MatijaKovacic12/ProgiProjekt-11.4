package touristAgency.unitTests.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import touristAgency.controllers.AuthController;
import touristAgency.dto.UserResponse;
import touristAgency.models.AppUser;
import touristAgency.models.Role;
import touristAgency.services.AuthService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {
    @Mock
    private AuthService authService;
    @InjectMocks
    private AuthController authController;

    @Test
    void me_returns401WhenNotLoggedIn() {
        ResponseEntity<UserResponse> response = authController.me(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void me_returnsLoggedInUser() {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setFirstName("Ana");
        user.setLastName("Horvat");
        user.setEmail("ana@test.hr");
        user.setRole(Role.ADMIN);
        when(authService.findUser("google", "sub-1")).thenReturn(Optional.of(user));

        ResponseEntity<UserResponse> response = authController.me(token("google", "sub-1"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(new UserResponse(1L, "Ana", "Horvat", "ana@test.hr", Role.ADMIN), response.getBody());
    }

    @Test
    void me_returns401WhenUserNotInDatabase() {
        when(authService.findUser("google", "sub-1")).thenReturn(Optional.empty());

        ResponseEntity<UserResponse> response = authController.me(token("google", "sub-1"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    private OAuth2AuthenticationToken token(String provider, String providerUserId) {
        OAuth2AuthenticationToken token = mock(OAuth2AuthenticationToken.class);
        when(token.getAuthorizedClientRegistrationId()).thenReturn(provider);
        when(token.getName()).thenReturn(providerUserId);
        return token;
    }
}
