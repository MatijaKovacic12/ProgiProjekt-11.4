package touristAgency.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import touristAgency.models.ExternalIdentity;

import java.util.List;
import java.util.Optional;

public interface ExternalIdentityRepository extends JpaRepository<ExternalIdentity,Long> {
    @EntityGraph(attributePaths = "user")
    Optional<ExternalIdentity> findByProviderAndProviderUserId(String provider, String providerUserId);

    boolean existsByUserIdAndProvider(Long userId, String provider);

    List<ExternalIdentity> findByUserId(Long userId);
}
