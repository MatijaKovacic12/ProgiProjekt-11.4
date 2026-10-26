package touristAgency.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "izlet")
@Getter
@Setter
@NoArgsConstructor

public class Trip extends  BaseEntity {
    @Column(name = "naziv", nullable = false, length = 200)
    private String name;

    @Column(name = "opis", nullable = false, columnDefinition = "text")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_kategorija", nullable = false)
    private Category category;

    @Column(name = "trajanje_minuta", nullable = false)
    private Integer durationMinutes;

    /** cijena po osobi */
    @Column(name = "cijena", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "max_sudionika", nullable = false)
    private Integer maxParticipants;

    @Column(name = "broj_vodica", nullable = false)
    private Integer requiredGuides = 1;


    @ManyToMany
    @JoinTable(
            name = "izlet_usluga",
            joinColumns = @JoinColumn(name = "id_izlet"),
            inverseJoinColumns = @JoinColumn(name = "id_usluga"))
    private Set<AdditionalService> additionalServices = new HashSet<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Location> locations = new ArrayList<>();

    public void addLocation(Location location) {
        location.setTrip(this);
        locations.add(location);
    }
}
