package pe.marathon.inventario.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.marathon.inventario.dto.ErrorDetalleDTO;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador Global de Excepciones para la API REST.
 * Estandariza las respuestas de error en formato JSON según el estándar RFC 7807.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorDetalleDTO> manejarNoEncontrado(
            RecursoNoEncontradoException ex, HttpServletRequest request) {

        ErrorDetalleDTO error = ErrorDetalleDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Recurso No Encontrado")
                .mensaje(ex.getMessage())
                .ruta(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ConteoInvalidoException.class)
    public ResponseEntity<ErrorDetalleDTO> manejarConteoInvalido(
            ConteoInvalidoException ex, HttpServletRequest request) {

        ErrorDetalleDTO error = ErrorDetalleDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Solicitud de Conteo Inválida")
                .mensaje(ex.getMessage())
                .ruta(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetalleDTO> manejarValidaciones(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> validaciones = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            validaciones.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ErrorDetalleDTO error = ErrorDetalleDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Error de Validación de Campos")
                .mensaje("Uno o más campos enviados no cumplen con el formato requerido")
                .ruta(request.getRequestURI())
                .validaciones(validaciones)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDetalleDTO> manejarCredencialesInvalidas(
            BadCredentialsException ex, HttpServletRequest request) {

        ErrorDetalleDTO error = ErrorDetalleDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Credenciales Inválidas")
                .mensaje("Usuario o contraseña incorrectos")
                .ruta(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetalleDTO> manejarAccesoDenegado(
            AccessDeniedException ex, HttpServletRequest request) {

        ErrorDetalleDTO error = ErrorDetalleDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Acceso Denegado")
                .mensaje("No cuenta con los privilegios suficientes para realizar esta acción")
                .ruta(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetalleDTO> manejarIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {

        ErrorDetalleDTO error = ErrorDetalleDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Parámetro Inválido")
                .mensaje(ex.getMessage())
                .ruta(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetalleDTO> manejarErrorInterno(
            Exception ex, HttpServletRequest request) {

        ErrorDetalleDTO error = ErrorDetalleDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Error Interno del Servidor")
                .mensaje("Ocurrió un error inesperado al procesar la solicitud: " + ex.getMessage())
                .ruta(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
