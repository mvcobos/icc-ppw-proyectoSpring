package ec.edu.ups.icc.proyecto.registrations.services;

import org.springframework.data.domain.Page;

import ec.edu.ups.icc.proyecto.core.dto.PaginationDto;
import ec.edu.ups.icc.proyecto.registrations.dtos.ChangeRegistrationStatusDto;
import ec.edu.ups.icc.proyecto.registrations.dtos.RegistrationResponseDto;
import ec.edu.ups.icc.proyecto.registrations.entities.RegistrationStatus;

/*
 * Hay dos actores: el participante crea y cancela su propia
 * inscripción, y el organizador la confirma o la rechaza.
 */
public interface RegistrationService {

    // Inscribe al participante en un evento dentro de una transacción
    RegistrationResponseDto create(Long eventId, Long currentUserId);

    // Listado de inscritos de un evento, solo para su organizador.
    Page<RegistrationResponseDto> findByEvent(Long eventId, RegistrationStatus status,
            PaginationDto pagination, Long currentUserId);

    // Inscripciones del participante autenticado.
    Page<RegistrationResponseDto> findMine(Long currentUserId, PaginationDto pagination);

    RegistrationResponseDto findOne(Long registrationId, Long currentUserId);

    // El organizador confirma o rechaza. Confirmar descuenta un cupo.
    RegistrationResponseDto changeStatus(Long registrationId, ChangeRegistrationStatusDto dto, Long currentUserId);

    // El participante cancela y, si estaba confirmada, se libera el cupo.
    RegistrationResponseDto cancel(Long registrationId, Long currentUserId);
}