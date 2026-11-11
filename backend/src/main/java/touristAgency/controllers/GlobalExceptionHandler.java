package touristAgency.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import touristAgency.dto.ErrorResponse;
import touristAgency.exceptions.BusinessConflictException;
import touristAgency.exceptions.ResourceNotFoundException;
import touristAgency.exceptions.UnauthorizedException;
import java.util.LinkedHashMap;
import java.util.Map;
import touristAgency.exceptions.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException e) {
        return build(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", e.getMessage(), null);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException e) {
        return build(HttpStatus.FORBIDDEN, "FORBIDDEN", e.getMessage(), null);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException e) {
        return build(HttpStatus.NOT_FOUND, e.getCode(), e.getMessage(), null);
    }

    @ExceptionHandler(BusinessConflictException.class)
    public ResponseEntity<ErrorResponse> handleBusinessConflict(BusinessConflictException e) {
        return build(HttpStatus.CONFLICT, e.getCode(), e.getMessage(), null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", e.getMessage(), null);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e) {
        return build(HttpStatus.CONFLICT, "CONFLICT", e.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBodyValidation(MethodArgumentNotValidException e) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        return build(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED","Zahtjev nije ispravan.", fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException e) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED","Tijelo zahtjeva je neispravno.", null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
                "Neispravna vrijednost parametra '" + e.getName() + "'.", null);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException e) {
        int status = e.getStatusCode().value();
        String code = status == 400 ? "VALIDATION_FAILED" : "ERROR";
        String message = e.getReason() != null ? e.getReason() : "Greška.";

        return ResponseEntity.status(status).body(ErrorResponse.of(status, code, message));
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status,
                                                String code,
                                                String message,
                                                Map<String, String> fieldErrors) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), code, message, fieldErrors));
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException e) {
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", e.getMessage(), null);
    }
}
