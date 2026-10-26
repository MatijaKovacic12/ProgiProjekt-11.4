package touristAgency.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="korisnik")
@Getter
@Setter
@NoArgsConstructor
public class AppUser extends BaseEntity {
    @Column(name = "ime", nullable = false, length = 100)
    private String firstName;

    @Column(name = "prezime", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "display_name", length = 200)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "uloga", nullable = false, length = 20)
    private Role role = Role.USER;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_slika")
    private Image image;
}
