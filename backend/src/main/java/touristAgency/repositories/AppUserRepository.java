package touristAgency.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import touristAgency.models.AppUser;
import touristAgency.models.Role;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser,Long> {

    Optional<AppUser> findByEmailIgnoreCase(String email);

    List<AppUser> findByRoleInOrderByLastNameAscFirstNameAsc(Collection<Role> roles);

    List<AppUser> findByRoleOrderByLastNameAscFirstNameAsc(Role role);

    @Query("""
            select u from AppUser u
            where u.role = touristAgency.models.Role.GUIDE
              and not exists (
                    select sg from TripSessionGuide sg
                    where sg.guide = u
                      and sg.tripSession.startTime < :end
                      and sg.tripSession.endTime   > :start)
            order by u.lastName, u.firstName
            """)
    List<AppUser> findAvailableGuides(@Param("start") OffsetDateTime start,
                                      @Param("end") OffsetDateTime end);

}

