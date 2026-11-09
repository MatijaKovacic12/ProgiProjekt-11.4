package touristAgency.dto;

import org.antlr.v4.runtime.misc.NotNull;
import touristAgency.models.Role;

public record ChangeRoleRequest(@NotNull Role role) {
}
