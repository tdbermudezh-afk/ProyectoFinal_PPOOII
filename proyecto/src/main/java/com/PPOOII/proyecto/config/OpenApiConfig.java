package com.PPOOII.proyecto.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String API_KEY_SCHEME = "x-api-key";
    public static final String TOKEN_SCHEME = "token";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Proyecto Final PPOOII")
                        .version("1.0")
                        .description("Documentación interactiva de la API con autenticación por ApiKey y Token."))
                // Requiere ambos esquemas de seguridad de forma global
                .addSecurityItem(new SecurityRequirement()
                        .addList(API_KEY_SCHEME)
                        .addList(TOKEN_SCHEME))
                .components(new Components()
                        // Esquema 1: Encabezado 'x-api-key'
                        .addSecuritySchemes(API_KEY_SCHEME, new SecurityScheme()
                                .name("x-api-key")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("Ingresa tu clave de API (x-api-key)"))
                        // Esquema 2: Encabezado 'token'
                        .addSecuritySchemes(TOKEN_SCHEME, new SecurityScheme()
                                .name("token")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("Ingresa tu Token de autenticación")));
    }
}

