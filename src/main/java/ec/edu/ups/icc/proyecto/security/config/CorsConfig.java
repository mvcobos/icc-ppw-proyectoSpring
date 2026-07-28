package ec.edu.ups.icc.proyecto.security.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/*
 * CORS restringido del Punto 8. Los orígenes provienen de una
 * variable de entorno y nunca se usa "*".
 */
@Configuration
public class CorsConfig {

    private final CorsProperties corsProperties;

    public CorsConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    /*
     * El bean debe llamarse corsConfigurationSource: es el nombre que
     * busca .cors(Customizer.withDefaults()) en SecurityConfig.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos desde ALLOWED_ORIGINS
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());

        // Métodos restringidos
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Encabezados restringidos a los necesarios
        configuration.setAllowedHeaders(
                List.of("Authorization", "Content-Type"));

        // La autenticación viaja en Authorization, no en cookies
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}