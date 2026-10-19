package id.my.tudemaha.lobos.exception;

import id.my.tudemaha.lobos.dto.response.HttpResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<HttpResponse<Void>> handleValidationError(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(HttpResponse.error("VALIDATION_ERROR", errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<HttpResponse<Void>> handleMalformedJson(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(HttpResponse.error("MALFORMED_REQUEST", "malformed JSON request body"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<HttpResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String msg = String.format("parameter '%s' has invalid format", e.getName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(HttpResponse.error("TYPE_MISMATCH", msg));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<HttpResponse<Void>> handleDuplicateEmail(DuplicateEmailException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(HttpResponse.error("DUPLICATE_EMAIL", e.getMessage()));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<HttpResponse<Void>> handleDuplicateResource(DuplicateResourceException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(HttpResponse.error("DUPLICATE_RESOURCE", e.getMessage()));
    }

    @ExceptionHandler(LoginException.class)
    public ResponseEntity<HttpResponse<Void>> handleLoginException(LoginException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(HttpResponse.error("INVALID_CREDENTIALS", e.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<HttpResponse<Void>> handleNotFoundException(NotFoundException e) {
        String msg = e.getMessage() != null ? e.getMessage() : "resource not found";
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(HttpResponse.error("NOT_FOUND", msg));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<HttpResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(HttpResponse.error("INVALID_ARGUMENT", e.getMessage()));
    }

    @ExceptionHandler(ForbiddenAccessException.class)
    public ResponseEntity<HttpResponse<Void>> handleForbiddenAccessException(ForbiddenAccessException e) {
        String msg = e.getMessage() != null ? e.getMessage() : "forbidden access";
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(HttpResponse.error("FORBIDDEN", msg));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<HttpResponse<Void>> handleInvalidTokenException(InvalidTokenException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(HttpResponse.error("INVALID_TOKEN", e.getMessage()));
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<HttpResponse<Void>> handleEmailNotVerifiedException(EmailNotVerifiedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(HttpResponse.error("EMAIL_NOT_VERIFIED", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpResponse<Void>> handleGeneralException(Exception e) {
        log.error("Unhandled server exception: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(HttpResponse.error("INTERNAL_ERROR", "an unexpected error occurred on the server"));
    }
}
