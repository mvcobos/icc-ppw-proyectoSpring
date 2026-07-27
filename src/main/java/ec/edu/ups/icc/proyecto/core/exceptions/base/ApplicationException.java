package ec.edu.ups.icc.proyecto.core.exceptions.base;

import org.springframework.http.HttpStatus;

public class ApplicationException extends RuntimeException {

    private final HttpStatus status;
    private final String code; 

    protected ApplicationException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status; 
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}