package touristAgency.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "usluga")
@Getter
@Setter
@NoArgsConstructor
public class AdditionalService extends  BaseEntity {
    @Column(name = "ime", nullable = false, length = 150)
    private String name;

    @Column(name = "opis", columnDefinition = "text")
    private String description;

    @Column(name = "cijena", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}
