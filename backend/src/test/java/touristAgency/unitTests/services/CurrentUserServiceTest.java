package touristAgency.unitTests.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import touristAgency.exceptions.UnauthorizedException;
import touristAgency.models.AppUser;
import touristAgency.models.Role;
import touristAgency.services.AuthService;
import touristAgency.services.CurrentUserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CurrentUserServiceTest {

    private AuthService authService;
    private CurrentUserService currentUserService;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        currentUserService = new CurrentUserService(authService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void loginAs(AppUser user) {
        OAuth2AuthenticationToken token = mock(OAuth2AuthenticationToken.class);
        when(token.getAuthorizedClientRegistrationId()).thenReturn("google");
        when(token.getName()).thenReturn("sub-1");
        SecurityContextHolder.getContext().setAuthentication(token);
        when(authService.findUser("google", "sub-1")).thenReturn(Optional.ofNullable(user));
    }

    private AppUser user(Long id, Role role) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setRole(role);
        return user;
    }

    @Test
    void requireUserId_ReturnsIdOfLoggedInUser() {
        loginAs(user(7L, Role.USER));

        assertEquals(7L, currentUserService.requireUserId());
    }

    @Test
    void requireUserId_Throws_WhenNobodyIsLoggedIn() {
        assertThrows(UnauthorizedException.class, () -> currentUserService.requireUserId());
        verifyNoInteractions(authService);
    }

    @Test
    void requireUserId_Throws_WhenAuthenticationIsNotOAuth() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("x", "y"));

        assertThrows(UnauthorizedException.class, () -> currentUserService.requireUserId());
        verifyNoInteractions(authService);
    }

    @Test
    void requireUserId_Throws_WhenUserIsNotInDatabase() {
        loginAs(null);

        assertThrows(UnauthorizedException.class, () -> currentUserService.requireUserId());
    }

    @Test
    void hasRole_TrueWhenRoleMatches() {
        loginAs(user(1L, Role.ADMIN));

        assertTrue(currentUserService.hasRole(Role.ADMIN));
    }

    @Test
    void hasRole_FalseWhenRoleDiffers() {
        loginAs(user(1L, Role.USER));

        assertFalse(currentUserService.hasRole(Role.ADMIN));
    }

    @Test
    void hasRole_FalseWhenNobodyIsLoggedIn() {
        assertFalse(currentUserService.hasRole(Role.ADMIN));
    }
}
