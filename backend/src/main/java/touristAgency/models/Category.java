package touristAgency.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "kategorija")
@Getter
@Setter
@NoArgsConstructor
public class Category extends  BaseEntity {
    @Column(name = "ime", nullable = false, length = 100)
    private String name;
}
