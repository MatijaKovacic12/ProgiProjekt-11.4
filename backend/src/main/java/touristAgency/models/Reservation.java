package touristAgency.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
    private List<ReservationAdditionalService> reservationAdditionalServices = new ArrayList<>();

    public void addAdditionalService(AdditionalService additionalService, int quantity) {
        reservationAdditionalServices.add(new ReservationAdditionalService(this, additionalService, quantity));
    }
}
