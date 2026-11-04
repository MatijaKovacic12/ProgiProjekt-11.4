package touristAgency.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ReservationCreateRequest(
        @NotNull(message = "Termin je obavezan.")
        Long sessionId,

        @NotNull(message = "Broj osoba je obavezan.")
        @Min(value = 1, message = "Broj osoba mora biti barem 1.")
        Integer participants,

        List<@NotNull(message = "ID dodatne usluge ne smije biti prazan.") Long> addonIds) {
}