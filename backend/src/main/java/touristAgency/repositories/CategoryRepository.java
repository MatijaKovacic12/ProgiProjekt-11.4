package touristAgency.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import touristAgency.models.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category,Long> {

    Optional<Category> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);

    List<Category> findAllByOrderByNameAsc();
}
