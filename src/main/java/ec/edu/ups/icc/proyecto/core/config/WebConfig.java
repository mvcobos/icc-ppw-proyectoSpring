package ec.edu.ups.icc.proyecto.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import ec.edu.ups.icc.proyecto.ratelimit.interceptors.RateLimitInterceptor;

/*
 * Registra el interceptor de rate limiting (Punto 7).
 *
 * Se excluyen login y registro porque tienen su propio control
 * dentro de AuthServiceImpl, y las rutas de documentación y salud
 * para no gastar el contador de un evaluador revisando Swagger.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    public WebConfig(RateLimitInterceptor rateLimitInterceptor) {
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/register",
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**");
    }
}