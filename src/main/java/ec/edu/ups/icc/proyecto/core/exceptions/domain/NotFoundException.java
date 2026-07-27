package ec.edu.ups.icc.proyecto.core.exceptions.domain;

import org.springframework.http.HttpStatus;

import ec.edu.ups.icc.proyecto.core.exceptions.base.ApplicationException;

public class NotFoundException extends ApplicationException {

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", message); //404 Not Found
    }

}