package touristAgency.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ReservationAdditionalServiceId implements Serializable {
    @Column(name = "id_rezervacija")
    private Long reservationId;

    @Column(name = "id_usluga")
    private Long additionalServiceId;
}
