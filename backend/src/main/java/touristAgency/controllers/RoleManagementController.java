package touristAgency.controllers;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import touristAgency.dto.ChangeRoleRequest;
import touristAgency.dto.UserRoleResponse;
import touristAgency.services.RoleManagementService;

import java.util.List;


@RestController
@RequestMapping(value = "/api/manager/employees",produces = MediaType.APPLICATION_JSON_VALUE)
public class RoleManagementController {

    private final RoleManagementService roleManagementService;

    public RoleManagementController(RoleManagementService roleManagementService){
        this.roleManagementService=roleManagementService;
    }

    @GetMapping
    public List<UserRoleResponse> getEmployees() {
        return roleManagementService.getEmployees();
    }

    @GetMapping(params = "email")
    public UserRoleResponse lookup(@RequestParam String email) {
        return roleManagementService.findByEmail(email);
    }

    @PutMapping("/{userId}/role")
    public UserRoleResponse changeRole(@PathVariable Long userId,
                                   @Validated @RequestBody ChangeRoleRequest request) {
        return roleManagementService.changeRole(userId, request.role());
    }


}
