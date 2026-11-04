package touristAgency.exceptions;

public class BusinessConflictException extends IllegalStateException {

    private final String code;

    public BusinessConflictException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}