// Paquete de configuración de seguridad de la aplicación (Laboratorio 2)
package com.PPOOII.proyecto.config;

// Importación de la constante LOGIN_URL desde Constans (Laboratorio 2)
import com.PPOOII.proyecto.config.model.Constans;
// Importación de la anotación @Autowired para inyección de dependencias
import org.springframework.beans.factory.annotation.Autowired;
// Importación de la anotación @Bean para declarar componentes en el contenedor de Spring
import org.springframework.context.annotation.Bean;
// Importación de la anotación @Configuration de Spring
import org.springframework.context.annotation.Configuration;
// Importación de Customizer para habilitar CORS en Spring Security
import org.springframework.security.config.Customizer;
// Importación de HttpSecurity para configurar la cadena de seguridad web
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// Importación de @EnableWebSecurity para habilitar Spring Security en el proyecto (Laboratorio 2)
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// Importación de AbstractHttpConfigurer para deshabilitar CSRF mediante expresión lambda
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
// Importación de SecurityFilterChain de Spring Security
import org.springframework.security.web.SecurityFilterChain;
// Importación de UsernamePasswordAuthenticationFilter para posicionar el filtro JWT
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Clase de configuración de Spring Security que registra el filtro de autorización JWT según el Laboratorio 2
@EnableWebSecurity
@Configuration
public class WebSecurityConfig {

    // Inyección del filtro de autorización JWT personalizado
    @Autowired
    private JWTAuthorizationFilter jwtAuthorizationFilter;

    // Define y configura la cadena de filtros de seguridad HTTP de la aplicación
    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        // Configura las reglas de seguridad sobre las peticiones HTTP
        http
                // Habilita soporte CORS integrado con WebConfig
                .cors(Customizer.withDefaults())
                // Deshabilita la protección CSRF para permitir peticiones REST sin sesiones de navegador (Laboratorio 2)
                .csrf(AbstractHttpConfigurer::disable)
                // Configura los permisos de acceso a las rutas del servidor
                .authorizeHttpRequests(authz -> authz
                        // Permite el acceso público al endpoint de autenticación JWT (/authenticate)
                        .requestMatchers(Constans.LOGIN_URL).permitAll()
                        // Permite que las demás rutas pasen al filtro JWT y al ApiKeyInterceptor para validación dual (APIKey + Token)
                        .anyRequest().permitAll()
                )
                // Registra el filtro JWTAuthorizationFilter después de UsernamePasswordAuthenticationFilter (Laboratorio 2)
                .addFilterAfter(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

        // Construye y retorna la configuración del SecurityFilterChain
        return http.build();
    }
}
