package touristAgency.unitTests.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import touristAgency.exceptions.ForbiddenException;
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

    private OAuth2AuthenticationToken token(String provider, String subject) {
        OAuth2AuthenticationToken token = mock(OAuth2AuthenticationToken.class);
        when(token.getAuthorizedClientRegistrationId()).thenReturn(provider);
        when(token.getName()).thenReturn(subject);
        return token;
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

    @Test
    void requireUser_whenNotLoggedIn_throwsUnauthorized() {
        assertThrows(UnauthorizedException.class, () -> currentUserService.requireUser(null));
    }

    @Test
    void requireUser_whenUserNotInDatabase_throwsUnauthorized() {
        when(authService.findUser("google", "sub-1")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class,
                () -> currentUserService.requireUser(token("google", "sub-1")));
    }

    @Test
    void requireRole_whenRoleMatches_returnsUser() {
        AppUser guide = user(1L, Role.GUIDE);
        when(authService.findUser("google", "sub-1")).thenReturn(Optional.of(guide));

        assertSame(guide, currentUserService.requireRole(token("google", "sub-1"), Role.GUIDE));
    }

    @Test
    void requireRole_whenRoleDoesNotMatch_throwsForbidden() {
        when(authService.findUser("google", "sub-1")).thenReturn(Optional.of(user(1L, Role.USER)));

        assertThrows(ForbiddenException.class,
                () -> currentUserService.requireRole(token("google", "sub-1"), Role.GUIDE, Role.ORGANIZER));
    }
}
