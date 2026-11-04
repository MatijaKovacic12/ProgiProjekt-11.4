package touristAgency.unitTests.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import touristAgency.controllers.GuideController;
import touristAgency.dto.GuideResponse;
import touristAgency.dto.GuideSessionResponse;
import touristAgency.dto.SessionGuidesResponse;
import touristAgency.exceptions.BadRequestException;
import touristAgency.exceptions.ForbiddenException;
import touristAgency.exceptions.GlobalExceptionHandler;
import touristAgency.exceptions.UnauthorizedException;
import touristAgency.models.AppUser;
import touristAgency.models.Role;
import touristAgency.services.CurrentUserService;
import touristAgency.services.GuideService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GuideControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GuideService guideService;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private GuideController guideController;

    private final OAuth2AuthenticationToken token = mock(OAuth2AuthenticationToken.class);

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(guideController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/guides/me/sessions vraća termine prijavljenog vodiča")
    void getMySessions_returnsSessionsOfLoggedInGuide() throws Exception {
        AppUser guide = new AppUser();
        guide.setId(5L);
        when(currentUserService.requireRole(any(), eq(Role.GUIDE))).thenReturn(guide);
        when(guideService.getSessionsForGuide(5L)).thenReturn(List.of(
                GuideSessionResponse.builder().sessionId(10L).tripName("Zagreb Tour").build()));

        mockMvc.perform(get("/api/guides/me/sessions").principal(token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sessionId").value(10L))
                .andExpect(jsonPath("$[0].tripName").value("Zagreb Tour"));
    }

    @Test
    @DisplayName("GET /api/guides/me/sessions vraća 401 kad korisnik nije prijavljen")
    void getMySessions_whenNotLoggedIn_returns401() throws Exception {
        when(currentUserService.requireRole(any(), eq(Role.GUIDE)))
                .thenThrow(new UnauthorizedException("User is not logged in"));

        mockMvc.perform(get("/api/guides/me/sessions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/guides vraća 403 korisniku koji nije organizator")
    void getGuideSchedules_whenNotOrganizer_returns403() throws Exception {
        when(currentUserService.requireRole(any(), eq(Role.ORGANIZER)))
                .thenThrow(new ForbiddenException("User does not have the required role"));

        mockMvc.perform(get("/api/guides").principal(token))
                .andExpect(status().isForbidden());
        verify(guideService, never()).getGuideSchedules();
    }

    @Test
    @DisplayName("GET /api/guides/sessions/{id}/available vraća slobodne vodiče")
    void getAvailableGuides_returnsGuides() throws Exception {
        when(guideService.getAvailableGuides(10L)).thenReturn(List.of(
                GuideResponse.builder().id(6L).firstName("Petra").build()));

        mockMvc.perform(get("/api/guides/sessions/10/available").principal(token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(6L));
    }

    @Test
    @DisplayName("POST /api/guides/sessions/{id}/assignments dodjeljuje vodiča")
    void assignGuide_returns200WithUpdatedSession() throws Exception {
        when(guideService.assignGuide(10L, 5L)).thenReturn(SessionGuidesResponse.builder()
                .sessionId(10L)
                .requiredGuides(1)
                .assignedGuides(List.of(GuideResponse.builder().id(5L).build()))
                .fullyStaffed(true)
                .build());

        mockMvc.perform(post("/api/guides/sessions/10/assignments")
                        .principal(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"guideId\": 5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedGuides[0].id").value(5L))
                .andExpect(jsonPath("$.fullyStaffed").value(true));
    }

    @Test
    @DisplayName("POST bez guideId vraća 400")
    void assignGuide_withoutGuideId_returns400() throws Exception {
        mockMvc.perform(post("/api/guides/sessions/10/assignments")
                        .principal(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        verify(guideService, never()).assignGuide(any(), any());
    }

    @Test
    @DisplayName("POST vraća 400 s porukom kad se termini preklapaju")
    void assignGuide_whenOverlap_returns400WithMessage() throws Exception {
        when(guideService.assignGuide(10L, 5L))
                .thenThrow(new BadRequestException("Guide is already assigned to an overlapping session"));

        mockMvc.perform(post("/api/guides/sessions/10/assignments")
                        .principal(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"guideId\": 5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Guide is already assigned to an overlapping session"));
    }

    @Test
    @DisplayName("DELETE /api/guides/sessions/{id}/assignments/{guideId} vraća 204")
    void unassignGuide_returns204() throws Exception {
        mockMvc.perform(delete("/api/guides/sessions/10/assignments/5").principal(token))
                .andExpect(status().isNoContent());
        verify(guideService).unassignGuide(10L, 5L);
    }
}
