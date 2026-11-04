package touristAgency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private OffsetDateTime timestamp;
    private int status;
    private String error;
    private String code;
    private String message;
    private List<String> details;
    private Map<String, String> fieldErrors;

    // Konstruktor koji koristi controllers.GlobalExceptionHandler
    public ErrorResponse(int status, String code, String message,
                         Map<String, String> fieldErrors) {
        this.timestamp = OffsetDateTime.now();
        this.status = status;
        this.code = code;
        this.message = message;
        this.fieldErrors = fieldErrors;
    }

    public static ErrorResponse of(int status, String code, String message) {
        return new ErrorResponse(status, code, message, null);
    }
}