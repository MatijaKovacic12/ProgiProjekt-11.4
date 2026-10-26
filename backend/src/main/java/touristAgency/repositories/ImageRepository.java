package touristAgency.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import touristAgency.models.Image;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image,Long> {
    Optional<Image>  findByPath(String path);
}
