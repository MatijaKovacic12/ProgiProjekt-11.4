package touristAgency.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "rezervacija")
@Getter
@Setter
@NoArgsConstructor
public class Reservation extends BaseEntity{
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_korisnik", nullable = false)
    private AppUser customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_instanca", nullable = false)
    private TripSession tripSession;

    @Column(name = "broj_osoba", nullable = false)
    private Integer numberOfPeople;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    @Column(name = "ukupna_cijena", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    // Set (ne List): EntityGraph dohvaća istodobno i trip.locations i usluge, a List ("bag") bi se zbog
    // kartezijeva produkta popunio duplikatima.
    private Set<ReservationAdditionalService> reservationAdditionalServices = new LinkedHashSet<>();

    public void addAdditionalService(AdditionalService additionalService, int quantity) {
        reservationAdditionalServices.add(new ReservationAdditionalService(this, additionalService, quantity));
    }
}
