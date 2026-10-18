// Paquete de configuración web de Spring MVC
package com.PPOOII.proyecto.config;

// Importación del interceptor de seguridad ApiKeyInterceptor
import com.PPOOII.proyecto.security.ApiKeyInterceptor;
// Importación de la anotación @Autowired para inyección de dependencias
import org.springframework.beans.factory.annotation.Autowired;
// Importación de la anotación @Configuration de Spring
import org.springframework.context.annotation.Configuration;
// Importación de InterceptorRegistry para registrar interceptores HTTP
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
// Importación de WebMvcConfigurer para personalizar la configuración de Spring MVC
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Clase de configuración que registra el interceptor de seguridad en las rutas protegidas de la API
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Inyección del interceptor encargado de validar 'x-api-key' y 'token' (JWT)
    @Autowired
    private ApiKeyInterceptor apiKeyInterceptor;

    // Registra los interceptores y define las rutas protegidas y excluidas
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Añade el interceptor de seguridad al registro de Spring MVC
        registry.addInterceptor(apiKeyInterceptor)
                // 1. Aplica la seguridad a todos los endpoints bajo el prefijo /api/**
                .addPathPatterns("/api/**")
                // 2. Excluye las rutas públicas y de autenticación para que puedan consumirse sin bloqueo previo
                .excludePathPatterns(
                        // Endpoint oficial de autenticación JWT del Laboratorio 2
                        "/authenticate",
                        // Servicios de gestión de usuarios (cambio de password, regeneración de APIKey y login)
                        "/api/usuarios/**",
                        // Servicios de creación y consulta de personas (generación inicial de administrativos)
                        "/api/personas/**",
                        // Interfaz gráfica de documentación Swagger UI (Laboratorio 4)
                        "/swagger-ui/**",
                        // Ruta directa al HTML de Swagger UI (Laboratorio 4)
                        "/swagger-ui.html",
                        // Especificación OpenAPI en la ruta configurada del Laboratorio 4
                        "/api-docs/**",
                        // Especificación OpenAPI v3 por defecto
                        "/v3/api-docs/**",
                        // Servicios de consulta pública sin autenticación
                        "/api/public/**",
                        // Variante pública adicional
                        "/api/publico/**"
                );
    }
}