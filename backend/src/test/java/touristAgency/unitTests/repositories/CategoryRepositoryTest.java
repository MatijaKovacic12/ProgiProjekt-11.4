package touristAgency.unitTests.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import touristAgency.models.Category;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryRepositoryTest extends RepositoryTestBase{
    @Test
    void findByNameIgnoreCase() {
        assertEquals(hiking.getId(), categories.findByNameIgnoreCase("planinarenje").orElseThrow().getId());
        assertTrue(categories.findByNameIgnoreCase("Rafting").isEmpty());
    }

    @Test
    void existsByNameIgnoreCase() {
        assertTrue(categories.existsByNameIgnoreCase("PLANINARENJE"));
        assertFalse(categories.existsByNameIgnoreCase("Rafting"));
    }

    @Test
    void findAllByOrderByNameAsc_returnsAlphabetically() {
        Category cycling = new Category();
        cycling.setName("Biciklizam");
        categories.save(cycling);

        List<String> names = categories.findAllByOrderByNameAsc().stream().map(Category::getName).toList();
        assertEquals(List.of("Biciklizam", "Planinarenje"), names);
    }

    @Test
    void save_rejectsDuplicateNameWithDifferentCase() {
        Category duplicate = new Category();
        duplicate.setName("PLANINARENJE");
        assertThrows(DataIntegrityViolationException.class, () -> categories.saveAndFlush(duplicate));
    }
}

