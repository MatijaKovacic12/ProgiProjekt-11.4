package touristAgency.unitTests.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import touristAgency.dto.UserRoleResponse;
import touristAgency.models.AppUser;
import touristAgency.models.Role;
import touristAgency.repositories.AppUserRepository;
import touristAgency.repositories.TripSessionGuideRepository;
import touristAgency.services.RoleManagementService;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class RoleManagementServiceTest {

    @Mock
    private AppUserRepository users;
    @Mock
    private TripSessionGuideRepository tripSessionGuides;

    @InjectMocks
    private RoleManagementService roleManagementService;

    @Captor
    private ArgumentCaptor<Collection<Role>> rolesCaptor;
    @Captor
    private ArgumentCaptor<OffsetDateTime> timeCaptor;


    @Test
    void getEmployees_queriesOnlyEmployeeRoles() {
        when(users.findByRoleInOrderByLastNameAscFirstNameAsc(any())).thenReturn(List.of());

        roleManagementService.getEmployees();

        verify(users).findByRoleInOrderByLastNameAscFirstNameAsc(rolesCaptor.capture());
        assertEquals(Set.of(Role.ADMIN, Role.ORGANIZER, Role.GUIDE,Role.MANAGER), Set.copyOf(rolesCaptor.getValue()));
    }

    @Test
    void getEmployees_mapsUsersToResponses() {
        AppUser petra = user(1L, "Petra", "Babić", "petra.babic@example.com", Role.ADMIN);
        AppUser ivan = user(2L, "Ivan", "Perić", "ivan.peric@example.com", Role.GUIDE);
        when(users.findByRoleInOrderByLastNameAscFirstNameAsc(any())).thenReturn(List.of(petra, ivan));

        List<UserRoleResponse> result = roleManagementService.getEmployees();

        assertEquals(List.of(
                new UserRoleResponse(1L, "Petra", "Babić", "petra.babic@example.com", Role.ADMIN, true),
                new UserRoleResponse(2L, "Ivan", "Perić", "ivan.peric@example.com", Role.GUIDE, true)
        ), result);
    }

    @Test
    void getEmployees_returnsEmptyListWhenNoEmployees() {
        when(users.findByRoleInOrderByLastNameAscFirstNameAsc(any())).thenReturn(List.of());

        List<UserRoleResponse> result = roleManagementService.getEmployees();
        assertTrue(result.isEmpty());
    }

    @Test
    void findByEmail_returnsRegisteredUser() {
        AppUser ana = user(5L, "Ana", "Horvat", "ana.horvat@example.com", Role.USER);
        when(users.findByEmailIgnoreCase("ana.horvat@example.com")).thenReturn(Optional.of(ana));

        UserRoleResponse result = roleManagementService.findByEmail("ana.horvat@example.com");

        assertEquals(new UserRoleResponse(5L, "Ana", "Horvat", "ana.horvat@example.com", Role.USER, false), result);
    }

    @Test
    void findByEmail_trimsWhitespaceBeforeSearching() {
        AppUser ana = user(5L, "Ana", "Horvat", "ana.horvat@example.com", Role.USER);
        when(users.findByEmailIgnoreCase("ana.horvat@example.com")).thenReturn(Optional.of(ana));

        UserRoleResponse result = roleManagementService.findByEmail("  ana.horvat@example.com  ");

        assertEquals(5L, result.id());
        verify(users).findByEmailIgnoreCase("ana.horvat@example.com");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void findByEmail_throws400WhenEmailBlank(String email) {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> roleManagementService.findByEmail(email));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verifyNoInteractions(users);
    }

    @Test
    void findByEmail_throws404WhenUserNotFound() {
        when(users.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> roleManagementService.findByEmail("nepostoji@example.com"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }


    @Test
    void changeRole_assignsRoleToRegularUser() {
        AppUser ana = user(5L, "Ana", "Horvat", "ana.horvat@example.com", Role.USER);
        when(users.findById(5L)).thenReturn(Optional.of(ana));

        UserRoleResponse result = roleManagementService.changeRole(5L, Role.GUIDE);

        assertEquals(Role.GUIDE, ana.getRole());
        assertEquals(Role.GUIDE, result.role());
        assertTrue(result.employee());
        verify(users, never()).save(any());
        verifyNoInteractions(tripSessionGuides);
    }

    @Test
    void changeRole_guideChangedToOtherRole_deletesUpcomingAssignments() {
        AppUser ivan = user(2L, "Ivan", "Perić", "ivan.peric@example.com", Role.GUIDE);
        when(users.findById(2L)).thenReturn(Optional.of(ivan));
        OffsetDateTime before = OffsetDateTime.now();

        roleManagementService.changeRole(2L, Role.ORGANIZER);

        OffsetDateTime after = OffsetDateTime.now();
        assertEquals(Role.ORGANIZER, ivan.getRole());
        verify(tripSessionGuides).deleteUpcomingAssignments(eq(2L), timeCaptor.capture());
        OffsetDateTime usedTime = timeCaptor.getValue();
        assertFalse(usedTime.isBefore(before));
        assertFalse(usedTime.isAfter(after));
    }

    @Test
    void changeRole_guideRevoked_deletesUpcomingAssignments() {
        AppUser ivan = user(2L, "Ivan", "Perić", "ivan.peric@example.com", Role.GUIDE);
        when(users.findById(2L)).thenReturn(Optional.of(ivan));

        UserRoleResponse result = roleManagementService.changeRole(2L, Role.USER);

        assertEquals(Role.USER, ivan.getRole());
        assertFalse(result.employee());
        verify(tripSessionGuides).deleteUpcomingAssignments(eq(2L), any(OffsetDateTime.class));
    }

    @Test
    void changeRole_nonGuide_doesNotTouchAssignments() {
        AppUser tomislav = user(3L, "Tomislav", "Horvat", "tomislav.horvat@example.com", Role.ORGANIZER);
        when(users.findById(3L)).thenReturn(Optional.of(tomislav));

        roleManagementService.changeRole(3L, Role.USER);

        assertEquals(Role.USER, tomislav.getRole());
        verifyNoInteractions(tripSessionGuides);
    }

    @Test
    void changeRole_sameRole_changesNothing() {
        AppUser ivan = user(2L, "Ivan", "Perić", "ivan.peric@example.com", Role.GUIDE);
        when(users.findById(2L)).thenReturn(Optional.of(ivan));

        UserRoleResponse result = roleManagementService.changeRole(2L, Role.GUIDE);

        assertEquals(Role.GUIDE, result.role());
        verifyNoInteractions(tripSessionGuides);
    }

    @Test
    void changeRole_throws404WhenUserNotFound() {
        when(users.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> roleManagementService.changeRole(99L, Role.GUIDE));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verifyNoInteractions(tripSessionGuides);
    }

    private AppUser user(Long id, String firstName, String lastName, String email, Role role) {
        AppUser u = new AppUser();
        u.setId(id);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setEmail(email);
        u.setRole(role);
        return u;
    }
}