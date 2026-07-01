package id.my.tudemaha.lobos.exception;

import id.my.tudemaha.lobos.dto.response.HttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<HttpResponse<Void>> handleValidationError(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(HttpResponse.error("invalid input", errors));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<HttpResponse<Void>> handleDuplicateEmail(DuplicateEmailException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(HttpResponse.error(e.getMessage(), null));
    }

    @ExceptionHandler(LoginException.class)
    public ResponseEntity<HttpResponse<Void>> handleLoginException(LoginException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(HttpResponse.error(e.getMessage(), null));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<HttpResponse<Void>> handleUserNotFoundException(UserNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(HttpResponse.error(e.getMessage(), null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<HttpResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        List<String> errors = new ArrayList<>();
        errors.add(e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(HttpResponse.error("invalid input", errors));
    }

    @ExceptionHandler(ForbiddenAccessException.class)
    public ResponseEntity<HttpResponse<Void>> handleForbiddenAccessException(ForbiddenAccessException e) {
        List<String> errors = new ArrayList<>();
        errors.add(e.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(HttpResponse.error("forbidden access", errors));
    }
}
