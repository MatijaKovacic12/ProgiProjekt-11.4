package touristAgency.services;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import touristAgency.exceptions.ForbiddenException;
import touristAgency.exceptions.UnauthorizedException;
import touristAgency.models.AppUser;
import touristAgency.models.Role;

import java.util.Arrays;

// resolves the logged in user (OAuth session) and checks his role
@Service
public class CurrentUserService {
    private final AuthService authService;

    public CurrentUserService(AuthService authService) {
        this.authService = authService;
    }

    public AppUser requireUser(OAuth2AuthenticationToken token) {
        if (token == null) {
            throw new UnauthorizedException("User is not logged in");
        }
        return authService.findUser(token.getAuthorizedClientRegistrationId(), token.getName())
                .orElseThrow(() -> new UnauthorizedException("Logged in user does not exist"));
    }

    public AppUser requireRole(OAuth2AuthenticationToken token, Role... allowedRoles) {
        AppUser user = requireUser(token);
        if (Arrays.stream(allowedRoles).noneMatch(role -> role == user.getRole())) {
            throw new ForbiddenException("User does not have the required role");
        }
        return user;
    }
}
