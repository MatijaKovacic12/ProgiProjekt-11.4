package touristAgency.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import touristAgency.models.TripSessionGuide;
import touristAgency.models.TripSessionGuideId;

import java.time.OffsetDateTime;
import java.util.List;

public interface TripSessionGuideRepository extends JpaRepository<TripSessionGuide, TripSessionGuideId> {

    @Query("select sg from TripSessionGuide sg join fetch sg.guide g where sg.tripSession.id = :tripSessionId order by g.lastName")
    List<TripSessionGuide> findByTripSessionId(@Param("tripSessionId") Long tripSessionId);

    long countByTripSessionId(Long sessionId);


    @Query("""
            select case when count(sg) > 0 then true else false end
            from TripSessionGuide sg join sg.tripSession s
            where sg.guide.id = :guideId
              and s.startTime < :end
              and s.endTime   > :start
            """)
    boolean hasOverlap(@Param("guideId") Long guideId,
                       @Param("start") OffsetDateTime start,
                       @Param("end") OffsetDateTime end);

    @Query("""
            select sg from TripSessionGuide sg
            join fetch sg.tripSession s
            join fetch s.trip
            where sg.guide.id = :guideId
            order by s.startTime
            """)
    List<TripSessionGuide> findAssignmentsForGuide(@Param("guideId") Long guideId);

    @Query("""
            select sg from TripSessionGuide sg
            join fetch sg.guide g
            join fetch sg.tripSession s
            join fetch s.trip
            where s.startTime > :now
            order by g.lastName, g.firstName, s.startTime
            """)
    List<TripSessionGuide> findUpcomingAssignments(@Param("now") OffsetDateTime now);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from TripSessionGuide sg
            where sg.id.guideId = :guideId
              and sg.id.sessionId in (select s.id from TripSession s where s.startTime > :now)
            """)
    int deleteUpcomingAssignments(@Param("guideId") Long guideId, @Param("now") OffsetDateTime now);
}
