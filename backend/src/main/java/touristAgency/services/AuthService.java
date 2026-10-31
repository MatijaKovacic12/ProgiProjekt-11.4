package touristAgency.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import touristAgency.models.AppUser;
import touristAgency.models.ExternalIdentity;
import touristAgency.repositories.AppUserRepository;
import touristAgency.repositories.ExternalIdentityRepository;

import java.util.Optional;

@Service
public class AuthService {
    private final AppUserRepository userRepository;
    private final ExternalIdentityRepository externalIdentityRepository;

    public AuthService(AppUserRepository userRepository, ExternalIdentityRepository externalIdentityRepository) {
        this.userRepository = userRepository;
        this.externalIdentityRepository = externalIdentityRepository;
    }

    // called after successful login with external provider (FR-23, FR-24, FR-25)
    @Transactional
    public AppUser loginOrRegister(String provider, String providerUserId, String email,
                                   String firstName, String lastName, boolean emailVerified) {
        // user already logged in with this provider before
        Optional<ExternalIdentity> existingIdentity =
                externalIdentityRepository.findByProviderAndProviderUserId(provider, providerUserId);
        if (existingIdentity.isPresent()) {
            return existingIdentity.get().getUser();
        }

        // user with same email already exists -> link this provider to that account
        Optional<AppUser> existingUser = userRepository.findByEmailIgnoreCase(email);
        if (existingUser.isPresent()) {
            if (!emailVerified) {
                throw new IllegalStateException("Email is not verified by provider");
            }
            saveIdentity(existingUser.get(), provider, providerUserId);
            return existingUser.get();
        }

        // first login -> register new user
        AppUser newUser = new AppUser();
        newUser.setEmail(email);
        newUser.setFirstName(firstName != null ? firstName : email.substring(0, email.indexOf('@')));
        newUser.setLastName(lastName != null ? lastName : "");
        userRepository.save(newUser);

        saveIdentity(newUser, provider, providerUserId);
        return newUser;
    }

    public Optional<AppUser> findUser(String provider, String providerUserId) {
        return externalIdentityRepository.findByProviderAndProviderUserId(provider, providerUserId)
                .map(ExternalIdentity::getUser);
    }

    private void saveIdentity(AppUser user, String provider, String providerUserId) {
        ExternalIdentity identity = new ExternalIdentity();
        identity.setUser(user);
        identity.setProvider(provider);
        identity.setProviderUserId(providerUserId);
        externalIdentityRepository.save(identity);
    }
}
