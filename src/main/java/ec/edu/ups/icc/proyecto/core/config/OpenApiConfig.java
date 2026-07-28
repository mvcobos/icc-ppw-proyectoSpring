package ec.edu.ups.icc.proyecto.core.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/*
 * Agrega el esquema Bearer JWT
 * para poder probar endpoints protegidos desde la misma Swagger UI,
 * pegando el access token con el boton "Authorize".
 */
@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        Info info = new Info()
                .title("API de Eventos Académicos")
                .version("1.0.0")
                .description("Gestión de usuarios, eventos académicos, sesiones e inscripciones");

        Server localServer = new Server()
                .url("/api")
                .description("Servidor local");

        SecurityScheme bearerScheme = new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Ingrese el JWT generado en /auth/login");

        Components components = new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME, bearerScheme);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer))
                // Aplica el esquema a TODAS las operaciones: sin esto, el
                // boton Authorize no envia el header Authorization al
                // probar endpoints protegidos
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(components);
    }
}