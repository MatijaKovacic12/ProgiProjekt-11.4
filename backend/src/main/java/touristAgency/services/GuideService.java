package touristAgency.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import touristAgency.dto.GuideResponse;
import touristAgency.dto.GuideScheduleResponse;
import touristAgency.dto.GuideSessionResponse;
import touristAgency.dto.SessionGuidesResponse;
import touristAgency.exceptions.BadRequestException;
import touristAgency.exceptions.ResourceNotFoundException;
import touristAgency.mappers.GuideMapper;
import touristAgency.models.AppUser;
import touristAgency.models.Location;
import touristAgency.models.LocationType;
import touristAgency.models.Role;
import touristAgency.models.TripSession;
import touristAgency.models.TripSessionGuide;
import touristAgency.models.TripSessionGuideId;
import touristAgency.repositories.AppUserRepository;
import touristAgency.repositories.LocationRepository;
import touristAgency.repositories.ReservationRepository;
import touristAgency.repositories.TripSessionGuideRepository;
import touristAgency.repositories.TripSessionRepository;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class GuideService {

    private final TripSessionGuideRepository tripSessionGuideRepository;
    private final TripSessionRepository tripSessionRepository;
    private final AppUserRepository appUserRepository;
    private final LocationRepository locationRepository;
    private final ReservationRepository reservationRepository;
    private final GuideMapper guideMapper;

    public GuideService(TripSessionGuideRepository tripSessionGuideRepository,
                        TripSessionRepository tripSessionRepository,
                        AppUserRepository appUserRepository,
                        LocationRepository locationRepository,
                        ReservationRepository reservationRepository,
                        GuideMapper guideMapper) {
        this.tripSessionGuideRepository = tripSessionGuideRepository;
        this.tripSessionRepository = tripSessionRepository;
        this.appUserRepository = appUserRepository;
        this.locationRepository = locationRepository;
        this.reservationRepository = reservationRepository;
        this.guideMapper = guideMapper;
    }

    /** Termini kojima je vodič dodijeljen, a još nisu završili (FR-21). */
    @Transactional(readOnly = true)
    public List<GuideSessionResponse> getSessionsForGuide(Long guideId) {
        return tripSessionGuideRepository.findActiveAssignmentsForGuide(guideId, OffsetDateTime.now())
                .stream()
                .map(sg -> toGuideSessionResponse(sg.getTripSession()))
                .collect(Collectors.toList());
    }

    /** Svi vodiči s budućim terminima kojima su dodijeljeni (FR-15). */
    @Transactional(readOnly = true)
    public List<GuideScheduleResponse> getGuideSchedules() {
        Map<Long, List<GuideSessionResponse>> sessionsByGuide = tripSessionGuideRepository
                .findUpcomingAssignments(OffsetDateTime.now())
                .stream()
                .filter(sg -> isActive(sg.getTripSession()))
                .collect(Collectors.groupingBy(
                        sg -> sg.getGuide().getId(),
                        Collectors.mapping(sg -> toGuideSessionResponse(sg.getTripSession()), Collectors.toList())));

        return appUserRepository.findByRoleOrderByLastNameAscFirstNameAsc(Role.GUIDE)
                .stream()
                .map(g -> guideMapper.toGuideScheduleResponse(g,
                        sessionsByGuide.getOrDefault(g.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    /** Budući aktivni termini s dodijeljenim i potrebnim brojem vodiča (UC-14, UC-15). */
    @Transactional(readOnly = true)
    public List<SessionGuidesResponse> getUpcomingSessionsWithGuides() {
        OffsetDateTime now = OffsetDateTime.now();
        Map<Long, List<AppUser>> guidesBySession = tripSessionGuideRepository.findUpcomingAssignments(now)
                .stream()
                .collect(Collectors.groupingBy(
                        sg -> sg.getTripSession().getId(),
                        Collectors.mapping(TripSessionGuide::getGuide, Collectors.toList())));

        return tripSessionRepository.findUpcomingActive(now)
                .stream()
                .map(s -> guideMapper.toSessionGuidesResponse(s,
                        guidesBySession.getOrDefault(s.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SessionGuidesResponse getSessionGuides(Long sessionId) {
        TripSession session = findSession(sessionId);
        List<AppUser> guides = tripSessionGuideRepository.findByTripSessionId(sessionId)
                .stream()
                .map(TripSessionGuide::getGuide)
                .collect(Collectors.toList());
        return guideMapper.toSessionGuidesResponse(session, guides);
    }

    /** Vodiči koji nemaju dodijeljen termin koji se preklapa s odabranim (FR-15). */
    @Transactional(readOnly = true)
    public List<GuideResponse> getAvailableGuides(Long sessionId) {
        TripSession session = findSession(sessionId);
        return appUserRepository.findAvailableGuides(session.getStartTime(), session.getEndTime())
                .stream()
                .map(guideMapper::toGuideResponse)
                .collect(Collectors.toList());
    }

    /** Dodjela vodiča terminu uz provjeru preklapanja (FR-14, FR-16). */
    public SessionGuidesResponse assignGuide(Long sessionId, Long guideId) {
        TripSession session = findSession(sessionId);

        if (!isActive(session)) {
            throw new BadRequestException("Cannot assign guides to a cancelled session");
        }
        if (!session.getStartTime().isAfter(OffsetDateTime.now())) {
            throw new BadRequestException("Cannot assign guides to a session that has already started");
        }

        AppUser guide = appUserRepository.findById(guideId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + guideId + " not found"));

        if (guide.getRole() != Role.GUIDE) {
            throw new BadRequestException("User with id " + guideId + " is not a guide");
        }
        if (tripSessionGuideRepository.existsById(new TripSessionGuideId(sessionId, guideId))) {
            throw new BadRequestException("Guide is already assigned to this session");
        }
        if (tripSessionGuideRepository.hasOverlap(guideId, session.getStartTime(), session.getEndTime())) {
            throw new BadRequestException("Guide " + guide.getFirstName() + " " + guide.getLastName()
                    + " is already assigned to an overlapping session");
        }

        tripSessionGuideRepository.save(new TripSessionGuide(session, guide));
        return getSessionGuides(sessionId);
    }

    public void unassignGuide(Long sessionId, Long guideId) {
        TripSessionGuideId id = new TripSessionGuideId(sessionId, guideId);
        if (!tripSessionGuideRepository.existsById(id)) {
            throw new ResourceNotFoundException("Guide " + guideId + " is not assigned to session " + sessionId);
        }
        tripSessionGuideRepository.deleteById(id);
    }

    private TripSession findSession(Long sessionId) {
        return tripSessionRepository.findWithTripById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip session with id " + sessionId + " not found"));
    }

    private boolean isActive(TripSession session) {
        return session.isActive() && session.getTrip().isActive();
    }

    private GuideSessionResponse toGuideSessionResponse(TripSession session) {
        String departurePoint = locationRepository
                .findFirstByTripIdAndType(session.getTrip().getId(), LocationType.DEPARTURE)
                .map(Location::getName)
                .orElse(null);
        long bookedPeople = reservationRepository.sumBookedSeats(session.getId());
        return guideMapper.toGuideSessionResponse(session, departurePoint, bookedPeople);
    }
}
