package touristAgency.unitTests.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import touristAgency.models.Image;

import static org.junit.jupiter.api.Assertions.*;

public class ImageRepositoryTest extends  RepositoryTestBase{

    private Image image(String path) {
        Image i = new Image();
        i.setPath(path);
        i.setAltText("Opis slike");
        return i;
    }

    @Test
    void findByPath() {
        Image saved = images.save(image("/images/plitvice.jpg"));

        assertEquals(saved.getId(), images.findByPath("/images/plitvice.jpg").orElseThrow().getId());
        assertTrue(images.findByPath("/images/nema.jpg").isEmpty());
    }

    @Test
    void save_rejectsDuplicatePath() {
        images.saveAndFlush(image("/images/isto.jpg"));
        assertThrows(DataIntegrityViolationException.class, () -> images.saveAndFlush(image("/images/isto.jpg")));
    }
}
