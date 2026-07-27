package ec.edu.ups.icc.proyecto.security.filters;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import ec.edu.ups.icc.proyecto.core.exceptions.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

/*
 * Se ejecuta cuando un usuario intenta acceder a un recurso protegido
 * sin autenticarse correctamente.
 *
 * Devuelve un JSON uniforme con código 401.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException {

        String code = (String) request.getAttribute("tokenErrorCode");
        String message;

        if (code == null) {
            code = "UNAUTHORIZED";
            message = "Debe autenticarse para acceder a este recurso.";
        } else if (code.equals("TOKEN_EXPIRED")) {
            message = "El token ha expirado. Use el refresh token para renovarlo.";
        } else {
            message = "El token proporcionado no es valido.";
        }

        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED,
                code,
                message,
                request.getRequestURI());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(response.getWriter(), error);
    }
}