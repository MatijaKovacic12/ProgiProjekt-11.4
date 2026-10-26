package touristAgency.unitTests.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import touristAgency.models.ExternalIdentity;

import static org.junit.jupiter.api.Assertions.*;

public class ExternalIdentityRepositoryTest extends RepositoryTestBase{
    @Test
    void findByProviderAndProviderUserId_returnsLinkedUser() {
        ExternalIdentity identity = externalIdentities
                .findByProviderAndProviderUserId("microsoft", "sub-123").orElseThrow();

        assertEquals(customer.getId(), identity.getUser().getId());
        assertEquals("ana@test.hr", identity.getUser().getEmail().toLowerCase());
    }

    @Test
    void findByProviderAndProviderUserId_returnsEmptyForOtherProvider() {
        assertTrue(externalIdentities.findByProviderAndProviderUserId("google", "sub-123").isEmpty());
    }

    @Test
    void existsByUserIdAndProvider() {
        assertTrue(externalIdentities.existsByUserIdAndProvider(customer.getId(), "microsoft"));
        assertFalse(externalIdentities.existsByUserIdAndProvider(customer.getId(), "google"));
    }

    @Test
    void findByUserId_returnsAllProvidersOfUser() {
        ExternalIdentity google = new ExternalIdentity();
        google.setUser(customer);
        google.setProvider("google");
        google.setProviderUserId("google-456");
        externalIdentities.save(google);

        assertEquals(2, externalIdentities.findByUserId(customer.getId()).size());
    }

    @Test
    void save_rejectsSameProviderAccountTwice() {
        ExternalIdentity duplicate = new ExternalIdentity();
        duplicate.setUser(admin);
        duplicate.setProvider("microsoft");
        duplicate.setProviderUserId("sub-123");

        assertThrows(DataIntegrityViolationException.class, () -> externalIdentities.saveAndFlush(duplicate));
    }
}
