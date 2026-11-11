package touristAgency.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "rezervacija_usluga")
@Getter
@Setter
@NoArgsConstructor
public class ReservationAdditionalService {
    @EmbeddedId
    private ReservationAdditionalServiceId id = new ReservationAdditionalServiceId();

    @MapsId("reservationId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_rezervacija")
    private Reservation reservation;

    @MapsId("additionalServiceId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usluga")
    private AdditionalService additionalService;

    @Column(name = "kolicina", nullable = false)
    private Integer quantity = 1;

    @Column(name = "cijena", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    public ReservationAdditionalService(Reservation reservation, AdditionalService additionalService, int quantity) {
        this.reservation = reservation;
        this.additionalService = additionalService;
        this.quantity = quantity;
        this.unitPrice = additionalService.getPrice();
    }

    // Jednakost po usluzi: id (EmbeddedId) je prazan dok se rezervacija ne spremi, pa se ne smije koristiti ovdje.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservationAdditionalService other)) return false;
        Long a = additionalService == null ? null : additionalService.getId();
        Long b = other.additionalService == null ? null : other.additionalService.getId();
        return a != null && a.equals(b);
    }

    @Override
    public int hashCode() {
        Long a = additionalService == null ? null : additionalService.getId();
        return Objects.hashCode(a);
    }
}
