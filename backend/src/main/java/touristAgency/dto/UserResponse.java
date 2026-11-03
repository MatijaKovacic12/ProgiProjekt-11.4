package touristAgency.dto;

import touristAgency.models.Role;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        Role role
) { }
