package ec.edu.ups.icc.proyecto.core.exceptions.domain;

import org.springframework.http.HttpStatus;

import ec.edu.ups.icc.proyecto.core.exceptions.base.ApplicationException;

public class BadRequestException extends ApplicationException {

    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message); //400
    }

}