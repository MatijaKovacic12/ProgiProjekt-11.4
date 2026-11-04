package touristAgency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Vodič s budućim terminima kojima je dodijeljen (FR-15, UC-15). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuideScheduleResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private List<GuideSessionResponse> upcomingSessions;
}
