package touristAgency.dto;

import touristAgency.models.AppUser;
import touristAgency.models.Role;

import java.util.EnumSet;
import java.util.Set;

public record UserRoleResponse (
        Long id,
        String firstName,
        String lastName,
        String email,
        Role role,
        boolean employee
){
    public static final Set<Role> EMPLOYEE_ROLES = EnumSet.of(Role.ADMIN, Role.ORGANIZER, Role.GUIDE,Role.MANAGER);

    public static UserRoleResponse from(AppUser u) {
        return new UserRoleResponse(
                u.getId(),
                u.getFirstName(),
                u.getLastName(),
                u.getEmail(),
                u.getRole(),
                EMPLOYEE_ROLES.contains(u.getRole())
        );
    }
}
