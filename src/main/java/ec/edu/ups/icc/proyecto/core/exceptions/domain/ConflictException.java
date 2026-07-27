package ec.edu.ups.icc.proyecto.core.exceptions.domain;

import org.springframework.http.HttpStatus;

import ec.edu.ups.icc.proyecto.core.exceptions.base.ApplicationException;

public class ConflictException extends ApplicationException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, "RESOURCE_CONFLICT", message); //409
    }

}