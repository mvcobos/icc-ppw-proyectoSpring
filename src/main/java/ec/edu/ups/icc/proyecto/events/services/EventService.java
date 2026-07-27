package ec.edu.ups.icc.proyecto.events.services;

import org.springframework.data.domain.Page;

import ec.edu.ups.icc.proyecto.core.dto.PaginationDto;
import ec.edu.ups.icc.proyecto.events.dtos.ChangeEventStatusDto;
import ec.edu.ups.icc.proyecto.events.dtos.CreateEventDto;
import ec.edu.ups.icc.proyecto.events.dtos.EventFilterDto;
import ec.edu.ups.icc.proyecto.events.dtos.EventResponseDto;
import ec.edu.ups.icc.proyecto.events.dtos.UpdateEventDto;

/*
 * Los métodos que modifican un evento reciben currentUserId
 * para validar la propiedad del recurso (Punto 3).
 */
public interface EventService {

    Page<EventResponseDto> findAll(EventFilterDto filters, PaginationDto pagination);

    EventResponseDto findOne(Long id);

    EventResponseDto create(CreateEventDto dto, Long currentUserId);

    EventResponseDto update(Long id, UpdateEventDto dto, Long currentUserId);

    EventResponseDto changeStatus(Long id, ChangeEventStatusDto dto, Long currentUserId);

    /*
     * Elimina lógicamente: No se permite si tiene inscripciones confirmadas.
     */
    void delete(Long id, Long currentUserId);
}