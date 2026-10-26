package touristAgency.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import touristAgency.models.Image;

import java.util.Optional;

public interface IdentityRepository extends JpaRepository<Image,Long> {
    Optional<Image>  findByPath(String path);
}
