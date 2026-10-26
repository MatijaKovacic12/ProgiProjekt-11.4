package touristAgency.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "korisnik_vanjski")
@Getter
@Setter
public class ExternalIdentity extends  BaseEntity{
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_korisnik", nullable = false)
    private AppUser user;

    @Column(name = "provider", nullable = false, length = 30)
    private String provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;
}
