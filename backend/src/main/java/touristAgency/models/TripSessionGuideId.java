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
public class TripSessionGuideId implements Serializable {

    @Column(name = "id_instanca")
    private Long sessionId;

    @Column(name = "id_vodic")
    private Long guideId;
}
