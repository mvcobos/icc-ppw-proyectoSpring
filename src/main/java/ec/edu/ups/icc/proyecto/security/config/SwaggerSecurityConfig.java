package ec.edu.ups.icc.proyecto.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/*
 * Protege /swagger-ui/** y /v3/api-docs/** con Basic Auth
 * usando credenciales de SWAGGER_USER y SWAGGER_PASSWORD
 */
@Configuration
@Profile("prod")
public class SwaggerSecurityConfig {

    @Value("${swagger.user}")
    private String swaggerUser;

    @Value("${swagger.password}")
    private String swaggerPassword;

    private final PasswordEncoder passwordEncoder;

    // Constructor lleno: reutiliza el mismo BCryptPasswordEncoder ya definido en SecurityConfig.
    public SwaggerSecurityConfig(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /*
     * @Order(1): se evalua antes que SecurityConfig
     * asi que intercepta primero las rutas de swagger en produccion.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain swaggerFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/swagger-ui/**", "/v3/api-docs/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .userDetailsService(swaggerUserDetailsService());

        return http.build();
    }

    private UserDetailsService swaggerUserDetailsService() {
        UserDetails user = User.withUsername(swaggerUser)
                .password(passwordEncoder.encode(swaggerPassword))
                .roles("SWAGGER")
                .build();

        return new InMemoryUserDetailsManager(user);
    }
}