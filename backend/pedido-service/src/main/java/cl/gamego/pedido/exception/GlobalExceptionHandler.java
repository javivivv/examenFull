package cl.gamego.pedido.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> manejarNoEncontrado(ResourceNotFoundException ex, HttpServletRequest request) {
        ApiError error = new ApiError(LocalDateTime.now(), 404, "No encontrado", List.of(ex.getMessage()), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> manejarConflicto(ConflictException ex, HttpServletRequest request) {
        ApiError error = new ApiError(LocalDateTime.now(), 409, "Conflicto", List.of(ex.getMessage()), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /** Un servicio del que depende este (cliente o producto) respondió con error: se traduce a un mensaje claro. */
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ApiError> manejarServicioDependiente(WebClientResponseException ex, HttpServletRequest request) {
        int estado = ex.getStatusCode().value();
        String detalle = switch (estado) {
            case 404 -> "No se encontró un cliente o producto del pedido";
            case 409 -> "Stock insuficiente para completar el pedido";
            default -> "Un servicio dependiente respondió con error " + estado;
        };
        ApiError error = new ApiError(LocalDateTime.now(), estado, "Servicio dependiente", List.of(detalle), request.getRequestURI());
        return ResponseEntity.status(estado).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> manejarValidaciones(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> mensajes = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        ApiError error = new ApiError(LocalDateTime.now(), 400, "Validación", mensajes, request.getRequestURI());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> manejarErrorGeneral(Exception ex, HttpServletRequest request) {
        ApiError error = new ApiError(LocalDateTime.now(), 500, "Error interno", List.of(ex.getMessage()), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
