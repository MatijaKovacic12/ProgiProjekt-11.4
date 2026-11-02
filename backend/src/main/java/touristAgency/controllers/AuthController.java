package touristAgency.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import touristAgency.dto.UserResponse;
import touristAgency.models.AppUser;
import touristAgency.services.AuthService;

import java.util.Optional;

@RestController
@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // returns the currently logged in user, or 401 if nobody is logged in
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(OAuth2AuthenticationToken token) {
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<AppUser> user = authService.findUser(token.getAuthorizedClientRegistrationId(), token.getName());
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AppUser u = user.get();
        return ResponseEntity.ok(new UserResponse(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(), u.getRole()));
    }
}
