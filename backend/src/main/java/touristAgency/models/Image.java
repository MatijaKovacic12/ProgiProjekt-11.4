package touristAgency.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="slika")
@Getter
@Setter
@NoArgsConstructor

public class Image extends BaseEntity {
    @Column(name = "putanja", nullable = false, length = 500, unique = true)
    private String path;

    @Column(name = "alt_text")
    private String altText;
}
