package touristAgency.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "izlet_instanca_vodic")
@Getter
@Setter
@NoArgsConstructor
public class TripSessionGuide {
    @EmbeddedId
    private TripSessionGuideId id = new TripSessionGuideId();

    @MapsId("sessionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_instanca")
    private TripSession tripSession;

    @MapsId("guideId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_vodic")
    private AppUser guide;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public TripSessionGuide(TripSession tripSession, AppUser guide) {
        this.tripSession = tripSession;
        this.guide = guide;
    }

}
