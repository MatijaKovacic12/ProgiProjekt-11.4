package touristAgency.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import touristAgency.exceptions.ForbiddenException;
import touristAgency.exceptions.UnauthorizedException;
import touristAgency.models.AppUser;
import touristAgency.models.Role;

import java.util.Arrays;
import java.util.Optional;

/**
 * Određuje tko je trenutno prijavljen (OAuth 2.0 prijava preko Googlea/Microsofta)
 * i vraća njegov zapis iz baze. Uloga se čita iz baze pri svakom zahtjevu,
 * pa promjena uloge vrijedi odmah, bez ponovne prijave.
 */
@Service
public class CurrentUserService {

    private final AuthService authService;

    public CurrentUserService(AuthService authService) {
        this.authService = authService;
    }

    public Optional<AppUser> findCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof OAuth2AuthenticationToken token)) {
            return Optional.empty();
        }

        return authService.findUser(token.getAuthorizedClientRegistrationId(), token.getName());
    }

    /** ID prijavljenog korisnika ili UnauthorizedException (-> 401). */
    public Long requireUserId() {
        return findCurrentUser()
                .map(AppUser::getId)
                .orElseThrow(() -> new UnauthorizedException("Korisnik nije prijavljen."));
    }

    public boolean hasRole(Role role) {
        return findCurrentUser()
                .map(user -> user.getRole() == role)
                .orElse(false);
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
