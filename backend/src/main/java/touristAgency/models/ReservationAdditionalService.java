package touristAgency.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

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

}
