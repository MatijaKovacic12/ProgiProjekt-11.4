package touristAgency.unitTests.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import touristAgency.models.AppUser;
import touristAgency.models.ExternalIdentity;
import touristAgency.models.Role;
import touristAgency.repositories.AppUserRepository;
import touristAgency.repositories.ExternalIdentityRepository;
import touristAgency.services.AuthService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private AppUserRepository userRepository;
    @Mock
    private ExternalIdentityRepository externalIdentityRepository;
    @InjectMocks
    private AuthService authService;

    @Test
    void loginOrRegister_registersNewUserOnFirstLogin() {
        when(externalIdentityRepository.findByProviderAndProviderUserId("google", "sub-1")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("ana@test.hr")).thenReturn(Optional.empty());

        AppUser user = authService.loginOrRegister("google", "sub-1", "ana@test.hr", "Ana", "Horvat", true);

        assertEquals("Ana", user.getFirstName());
        assertEquals("Horvat", user.getLastName());
        assertEquals("ana@test.hr", user.getEmail());
        assertEquals(Role.USER, user.getRole());
        verify(userRepository).save(user);

        ArgumentCaptor<ExternalIdentity> identity = ArgumentCaptor.forClass(ExternalIdentity.class);
        verify(externalIdentityRepository).save(identity.capture());
        assertEquals("google", identity.getValue().getProvider());
        assertEquals("sub-1", identity.getValue().getProviderUserId());
        assertSame(user, identity.getValue().getUser());
    }

    @Test
    void loginOrRegister_returnsExistingUserOnNextLogin() {
        AppUser existing = new AppUser();
        ExternalIdentity identity = new ExternalIdentity();
        identity.setUser(existing);
        when(externalIdentityRepository.findByProviderAndProviderUserId("google", "sub-1")).thenReturn(Optional.of(identity));

        AppUser user = authService.loginOrRegister("google", "sub-1", "ana@test.hr", "Ana", "Horvat", true);

        assertSame(existing, user);
        verify(userRepository, never()).save(any());
        verify(externalIdentityRepository, never()).save(any());
    }

    @Test
    void loginOrRegister_linksProviderToExistingUserWithSameVerifiedEmail() {
        AppUser existing = new AppUser();
        existing.setEmail("Ana@Test.hr");
        when(externalIdentityRepository.findByProviderAndProviderUserId("google", "sub-1")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("ana@test.hr")).thenReturn(Optional.of(existing));

        AppUser user = authService.loginOrRegister("google", "sub-1", "ana@test.hr", "Ana", "Horvat", true);

        assertSame(existing, user);
        verify(userRepository, never()).save(any());
        verify(externalIdentityRepository).save(any(ExternalIdentity.class));
    }

    @Test
    void loginOrRegister_rejectsExistingEmailWhenNotVerified() {
        when(externalIdentityRepository.findByProviderAndProviderUserId("google", "sub-1")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("ana@test.hr")).thenReturn(Optional.of(new AppUser()));

        assertThrows(IllegalStateException.class,
                () -> authService.loginOrRegister("google", "sub-1", "ana@test.hr", "Ana", "Horvat", false));
        verify(externalIdentityRepository, never()).save(any());
    }

    @Test
    void loginOrRegister_usesEmailAsNameWhenProviderSendsNoName() {
        when(externalIdentityRepository.findByProviderAndProviderUserId("google", "sub-1")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("ana@test.hr")).thenReturn(Optional.empty());

        AppUser user = authService.loginOrRegister("google", "sub-1", "ana@test.hr", null, null, true);

        assertEquals("ana", user.getFirstName());
        assertEquals("", user.getLastName());
    }

    @Test
    void findUser_returnsUserLinkedToIdentity() {
        AppUser existing = new AppUser();
        ExternalIdentity identity = new ExternalIdentity();
        identity.setUser(existing);
        when(externalIdentityRepository.findByProviderAndProviderUserId("google", "sub-1")).thenReturn(Optional.of(identity));

        assertSame(existing, authService.findUser("google", "sub-1").orElseThrow());
    }
}
