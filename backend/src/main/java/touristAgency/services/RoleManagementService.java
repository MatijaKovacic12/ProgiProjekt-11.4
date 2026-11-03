package touristAgency.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import touristAgency.dto.UserRoleResponse;
import touristAgency.models.AppUser;
import touristAgency.models.Role;
import touristAgency.repositories.AppUserRepository;
import touristAgency.repositories.TripSessionGuideRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class RoleManagementService{
    private final AppUserRepository users;
    private final TripSessionGuideRepository tripSessionGuides;

    public RoleManagementService(AppUserRepository users, TripSessionGuideRepository tripSessionGuides){
        this.users=users;
        this.tripSessionGuides=tripSessionGuides;
    }

    @Transactional(readOnly = true)
    public List<UserRoleResponse> getEmployees(){
        return users.findByRoleInOrderByLastNameAscFirstNameAsc(UserRoleResponse.EMPLOYEE_ROLES)
                .stream()
                .map(UserRoleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserRoleResponse findByEmail(String email){
        String normalized = email==null ? "":email.trim();
        if(normalized.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Email is required field");
        }
        return users.findByEmailIgnoreCase(normalized)
                .map(UserRoleResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registered user was not found for email "+normalized));

    }
    @Transactional
    public UserRoleResponse changeRole(Long userId, Role newRole) {
        AppUser user = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User " + userId + " does not exist"));

        Role oldRole = user.getRole();
        if (oldRole == newRole) {
            return UserRoleResponse.from(user);
        }

        if (oldRole == Role.MANAGER) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Manager role is not able to be revoked");
        }

        if (oldRole == Role.GUIDE) {
            tripSessionGuides.deleteUpcomingAssignments(user.getId(), OffsetDateTime.now());
        }

        user.setRole(newRole);
        return UserRoleResponse.from(user);
    }
}
