package ec.edu.ups.icc.proyecto.sessions.services;

import org.springframework.data.domain.Page;

import ec.edu.ups.icc.proyecto.core.dto.PaginationDto;
import ec.edu.ups.icc.proyecto.sessions.dtos.CreateSessionDto;
import ec.edu.ups.icc.proyecto.sessions.dtos.SessionResponseDto;
import ec.edu.ups.icc.proyecto.sessions.dtos.UpdateSessionDto;

// Todas las operaciones reciben el eventId de la ruta:
public interface SessionService {

    Page<SessionResponseDto> findByEvent(Long eventId, PaginationDto pagination);

    SessionResponseDto findOne(Long eventId, Long sessionId);

    SessionResponseDto create(Long eventId, CreateSessionDto dto, Long currentUserId);

    SessionResponseDto update(Long eventId, Long sessionId, UpdateSessionDto dto, Long currentUserId);

    // Elimina físicamente una sesión.
    void delete(Long eventId, Long sessionId, Long currentUserId);
}