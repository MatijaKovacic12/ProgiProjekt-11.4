package touristAgency.unitTests.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import touristAgency.exceptions.ForbiddenException;
import touristAgency.exceptions.UnauthorizedException;
import touristAgency.models.AppUser;
import touristAgency.models.Role;
import touristAgency.services.AuthService;
import touristAgency.services.CurrentUserService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private CurrentUserService currentUserService;

    @Test
    void requireUser_whenNotLoggedIn_throwsUnauthorized() {
        assertThatThrownBy(() -> currentUserService.requireUser(null))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void requireUser_whenUserNotInDatabase_throwsUnauthorized() {
        when(authService.findUser("google", "sub-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> currentUserService.requireUser(token("google", "sub-1")))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void requireRole_whenRoleMatches_returnsUser() {
        AppUser guide = user(Role.GUIDE);
        when(authService.findUser("google", "sub-1")).thenReturn(Optional.of(guide));

        assertThat(currentUserService.requireRole(token("google", "sub-1"), Role.GUIDE)).isSameAs(guide);
    }

    @Test
    void requireRole_whenRoleDoesNotMatch_throwsForbidden() {
        when(authService.findUser("google", "sub-1")).thenReturn(Optional.of(user(Role.USER)));

        assertThatThrownBy(() -> currentUserService.requireRole(token("google", "sub-1"), Role.GUIDE, Role.ORGANIZER))
                .isInstanceOf(ForbiddenException.class);
    }

    private AppUser user(Role role) {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setRole(role);
        return user;
    }

    private OAuth2AuthenticationToken token(String provider, String subject) {
        OAuth2AuthenticationToken token = mock(OAuth2AuthenticationToken.class);
        when(token.getAuthorizedClientRegistrationId()).thenReturn(provider);
        when(token.getName()).thenReturn(subject);
        return token;
    }
}
