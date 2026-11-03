package touristAgency.unitTests.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import touristAgency.controllers.RoleManagementController;
import touristAgency.dto.ChangeRoleRequest;
import touristAgency.dto.UserRoleResponse;
import touristAgency.models.Role;
import touristAgency.services.RoleManagementService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class RoleManagementControllerTest {
    @Mock
    private RoleManagementService roleManagementService;
    @InjectMocks
    private RoleManagementController roleManagementController;

    @Test
    void getEmployees_returnsEmployeesFromService() {
        List<UserRoleResponse> employees = List.of(
                response(1L, "Petra", "Babić", "petra.babic@example.com", Role.ADMIN),
                response(2L, "Ivan", "Perić", "ivan.peric@example.com", Role.GUIDE));
        when(roleManagementService.getEmployees()).thenReturn(employees);

        List<UserRoleResponse> result = roleManagementController.getEmployees();

        assertEquals(employees, result);
        verify(roleManagementService).getEmployees();
    }

    @Test
    void getEmployees_returnsEmptyListWhenNoEmployees() {
        when(roleManagementService.getEmployees()).thenReturn(List.of());

        List<UserRoleResponse> result = roleManagementController.getEmployees();

        assertTrue(result.isEmpty());
    }

    @Test
    void lookup_returnsUserFoundByEmail() {
        UserRoleResponse user = response(5L, "Ana", "Horvat", "ana.horvat@example.com", Role.USER);
        when(roleManagementService.findByEmail("ana.horvat@example.com")).thenReturn(user);

        UserRoleResponse result = roleManagementController.lookup("ana.horvat@example.com");

        assertEquals(user, result);
        assertFalse(result.employee());
    }

    @Test
    void lookup_throws404WhenUserNotFound() {
        when(roleManagementService.findByEmail("nepostoji@example.com"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> roleManagementController.lookup("nepostoji@example.com"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void changeRole_passesUserIdAndRoleToService() {
        UserRoleResponse updated = response(5L, "Ana", "Horvat", "ana.horvat@example.com", Role.GUIDE);
        when(roleManagementService.changeRole(5L, Role.GUIDE)).thenReturn(updated);

        UserRoleResponse result = roleManagementController.changeRole(5L, new ChangeRoleRequest(Role.GUIDE));

        assertEquals(updated, result);
        verify(roleManagementService).changeRole(5L, Role.GUIDE);
    }

    @Test
    void changeRole_revokesRoleBySettingUser() {
        UserRoleResponse revoked = response(2L, "Ivan", "Perić", "ivan.peric@example.com", Role.USER);
        when(roleManagementService.changeRole(2L, Role.USER)).thenReturn(revoked);

        UserRoleResponse result = roleManagementController.changeRole(2L, new ChangeRoleRequest(Role.USER));

        assertEquals(Role.USER, result.role());
        assertFalse(result.employee());
    }

    @Test
    void changeRole_throws409WhenChangingAdmin() {
        when(roleManagementService.changeRole(1L, Role.GUIDE))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> roleManagementController.changeRole(1L, new ChangeRoleRequest(Role.GUIDE)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void changeRole_throws404WhenUserNotFound() {
        when(roleManagementService.changeRole(99L, Role.GUIDE))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> roleManagementController.changeRole(99L, new ChangeRoleRequest(Role.GUIDE)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    private UserRoleResponse response(Long id, String firstName, String lastName, String email, Role role) {
        return new UserRoleResponse(id, firstName, lastName, email, role, role != Role.USER);
    }

}
