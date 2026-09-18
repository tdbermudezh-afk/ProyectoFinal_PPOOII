package com.PPOOII.proyecto.config;

import com.PPOOII.proyecto.security.ApiKeyInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private ApiKeyInterceptor apiKeyInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiKeyInterceptor)
                // 1. Aplica la seguridad a toda la API 
                .addPathPatterns("/api/**")
                // 2. Excluye las rutas públicas para que no se bloqueen a sí mismas
                .excludePathPatterns(
                        "/api/usuarios/**",  // Para poder cambiar password y regenerar la llave
                        "/api/personas/**",  // Para poder registrar nuevas personas/administradores
                        "/swagger-ui/**",    // Para poder ver la documentación
                        "/v3/api-docs/**",    // Archivos internos de Swagger
                        "/api/public/**"   // Para poder acceder a los servicios públicos sin autenticación
                );
    }
}