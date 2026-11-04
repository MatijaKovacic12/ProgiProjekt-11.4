package touristAgency.controllers;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import touristAgency.dto.AssignGuideRequest;
import touristAgency.dto.GuideResponse;
import touristAgency.dto.GuideScheduleResponse;
import touristAgency.dto.GuideSessionResponse;
import touristAgency.dto.SessionGuidesResponse;
import touristAgency.models.AppUser;
import touristAgency.models.Role;
import touristAgency.services.CurrentUserService;
import touristAgency.services.GuideService;

import java.util.List;

@RestController
@RequestMapping(value = "/api/guides", produces = MediaType.APPLICATION_JSON_VALUE)
public class GuideController {

    private final GuideService guideService;
    private final CurrentUserService currentUserService;

    public GuideController(GuideService guideService, CurrentUserService currentUserService) {
        this.guideService = guideService;
        this.currentUserService = currentUserService;
    }

    // vodič: samo termini kojima je on dodijeljen (FR-21)
    @GetMapping("/me/sessions")
    public ResponseEntity<List<GuideSessionResponse>> getMySessions(OAuth2AuthenticationToken token) {
        AppUser guide = currentUserService.requireRole(token, Role.GUIDE);
        return ResponseEntity.ok(guideService.getSessionsForGuide(guide.getId()));
    }

    // organizator: svi vodiči s budućim terminima (FR-15)
    @GetMapping
    public ResponseEntity<List<GuideScheduleResponse>> getGuideSchedules(OAuth2AuthenticationToken token) {
        currentUserService.requireRole(token, Role.ORGANIZER);
        return ResponseEntity.ok(guideService.getGuideSchedules());
    }

    // organizator: budući termini s dodijeljenim vodičima (UC-14)
    @GetMapping("/sessions")
    public ResponseEntity<List<SessionGuidesResponse>> getUpcomingSessions(OAuth2AuthenticationToken token) {
        currentUserService.requireRole(token, Role.ORGANIZER);
        return ResponseEntity.ok(guideService.getUpcomingSessionsWithGuides());
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<SessionGuidesResponse> getSessionGuides(@PathVariable Long sessionId,
                                                                  OAuth2AuthenticationToken token) {
        currentUserService.requireRole(token, Role.ORGANIZER);
        return ResponseEntity.ok(guideService.getSessionGuides(sessionId));
    }

    // organizator: vodiči slobodni u vremenu termina (FR-15)
    @GetMapping("/sessions/{sessionId}/available")
    public ResponseEntity<List<GuideResponse>> getAvailableGuides(@PathVariable Long sessionId,
                                                                  OAuth2AuthenticationToken token) {
        currentUserService.requireRole(token, Role.ORGANIZER);
        return ResponseEntity.ok(guideService.getAvailableGuides(sessionId));
    }

    // organizator: dodjela vodiča terminu (FR-14, FR-16)
    @PostMapping(value = "/sessions/{sessionId}/assignments", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SessionGuidesResponse> assignGuide(@PathVariable Long sessionId,
                                                             @Valid @RequestBody AssignGuideRequest request,
                                                             OAuth2AuthenticationToken token) {
        currentUserService.requireRole(token, Role.ORGANIZER);
        return ResponseEntity.ok(guideService.assignGuide(sessionId, request.getGuideId()));
    }

    @DeleteMapping("/sessions/{sessionId}/assignments/{guideId}")
    public ResponseEntity<Void> unassignGuide(@PathVariable Long sessionId,
                                              @PathVariable Long guideId,
                                              OAuth2AuthenticationToken token) {
        currentUserService.requireRole(token, Role.ORGANIZER);
        guideService.unassignGuide(sessionId, guideId);
        return ResponseEntity.noContent().build();
    }
}
