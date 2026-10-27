package touristAgency.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name="izlet_instanca")
@Getter
@Setter
@NoArgsConstructor
public class TripSession extends  BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "id_izlet",nullable = false)
    private Trip trip;

    @Column(name = "termin_pocetak", nullable = false)
    private OffsetDateTime startTime;

    /** Uvijek = startTime + trip.durationMinutes (FR-28). */
    @Column(name = "termin_kraj", nullable = false)
    private OffsetDateTime endTime;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    /** Postavi početak i izračunaj kraj iz trajanja izleta. */
    public void schedule(OffsetDateTime start) {
        this.startTime = start;
        this.endTime = start.plusMinutes(trip.getDurationMinutes());
    }


}
