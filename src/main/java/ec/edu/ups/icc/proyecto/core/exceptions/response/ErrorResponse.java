package ec.edu.ups.icc.proyecto.core.exceptions.response;

import java.time.OffsetDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

/*
 * Respuesta uniforme de error exigida por el Punto 10:
 * fecha, código HTTP, código interno, mensaje y ruta.
 *
 * timestamp usa OffsetDateTime para salir en ISO 8601 con zona,
 * en coherencia con el Punto 14.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private OffsetDateTime timestamp;
    private int status;
    private String error;
    private String code;
    private String message;
    private String path;
    private Map<String, String> details;

    public ErrorResponse(HttpStatus status, String code, String message, String path, Map<String, String> details) {
        this.timestamp = OffsetDateTime.now();
        this.status = status.value(); // 400, 404, 500
        this.error = status.getReasonPhrase(); // Not Found, Bad Request, Internal Server Error
        this.code = code;
        this.message = message;
        this.path = path;
        this.details = details;
    }

    public ErrorResponse(HttpStatus status, String code, String message, String path) {
        this(status, code, message, path, null);
    }

    //Getters and Setters
    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }

}