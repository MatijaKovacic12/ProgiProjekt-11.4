package touristAgency.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "lokacija")
@Getter
@Setter
@NoArgsConstructor
public class Location extends  BaseEntity {
    @Column(name = "naziv", nullable = false, length = 200)
    private String name;

    @Column(name = "opis", nullable = false, length = 1000)
    private String description;

    @Column(name = "znacaj", length = 1000)
    private String significance;

    @Column(name = "latitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "tip", nullable = false, length = 20)
    private LocationType type = LocationType.LANDMARK;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_slika")
    private Image image;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_izlet", nullable = false)
    private Trip trip;
}
