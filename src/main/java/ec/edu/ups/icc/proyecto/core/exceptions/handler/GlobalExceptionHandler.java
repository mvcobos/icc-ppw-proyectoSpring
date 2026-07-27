package ec.edu.ups.icc.proyecto.core.exceptions.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import ec.edu.ups.icc.proyecto.core.exceptions.base.ApplicationException;
import ec.edu.ups.icc.proyecto.core.exceptions.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;

/*
 * Manejo centralizado de excepciones (Punto 10).
 *
 * E2 amplía este handler con los casos que falten:
 * tokens inválidos y exceso de solicitudes (429).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ============== EXCEPCIONES DE NEGOCIO ==============

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(
            ApplicationException ex,
            HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                ex.getStatus(),
                ex.getCode(),
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity
                .status(ex.getStatus())
                .body(response);
    }

    // ============== EXCEPCIONES DE VALIDACIÓN ==============

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Datos de entrada inválidos",
                request.getRequestURI(),
                errors);

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    /*
     * Maneja errores de validación en query params
     * recibidos mediante @ModelAttribute.
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(
            BindException ex,
            HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Parámetros de consulta inválidos",
                request.getRequestURI(),
                errors);

        return ResponseEntity
                .badRequest()
                .body(response);
    }

     /*
     * El cuerpo JSON no se puede convertir al DTO.
     *
     * Caso típico: un enum con un valor que no existe.
     * Es un error del cliente, así que responde 400 y no 500.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                "MALFORMED_REQUEST",
                "El cuerpo de la petición no es válido o contiene valores no permitidos",
                request.getRequestURI());

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    /*
     * Un parámetro de la URL no se puede convertir al tipo esperado.
     *
     * Caso típico: ?status=INVENTADO o un id que no es numérico.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        errors.put(ex.getName(), "Valor no permitido para este parámetro");

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_PARAMETER",
                "Parámetro inválido: " + ex.getName(),
                request.getRequestURI(),
                errors);

        return ResponseEntity
                .badRequest()
                .body(response);
    }
    
    // ============== EXCEPCIONES DE SEGURIDAD ==============

    /*
     * Se lanza cuando @PreAuthorize evalúa a false: el usuario
     * está autenticado pero no tiene el rol requerido.
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(
            AuthorizationDeniedException ex,
            HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "No tienes permisos para acceder a este recurso",
                request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }

    /*
     * Acceso denegado lanzado manualmente desde el servicio
     * al validar la propiedad del recurso (Punto 3).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {
        String message = ex.getMessage();

        if (message == null || message.isBlank()) {
            message = "Acceso denegado";
        }

        ErrorResponse response = new ErrorResponse(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                message,
                request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }

    /*
     * Credenciales inválidas, token inválido o sesión expirada.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "AUTHENTICATION_FAILED",
                "Credenciales inválidas o sesión expirada",
                request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    // ============== EXCEPCIONES GENERALES ==============

    /*
     * Maneja cualquier excepción no capturada por otros manejadores.
     * Debe ser el último manejador (más genérico).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception ex,
            HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "Error interno del servidor",
                request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}