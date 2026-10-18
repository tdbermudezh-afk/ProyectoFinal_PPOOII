// Paquete de controladores REST de la aplicación
package com.PPOOII.proyecto.controllers;

// Importación de la clase generadora de tokens JWT (Laboratorio 2)
import com.PPOOII.proyecto.config.JWTAuthtenticationConfig;
// Importación del modelo de petición JwtRequest (Laboratorio 2)
import com.PPOOII.proyecto.config.model.JwtRequest;
// Importación del modelo de respuesta JwtResponse (Laboratorio 2)
import com.PPOOII.proyecto.config.model.JwtResponse;
// Importación de la entidad Usuario
import com.PPOOII.proyecto.entities.Usuario;
// Importación del repositorio JPA de Usuario
import com.PPOOII.proyecto.repository.UsuarioRepository;
// Importación de las anotaciones Swagger OpenAPI (Laboratorio 4)
import io.swagger.v3.oas.annotations.Operation;
// Importación de @Content de Swagger OpenAPI
import io.swagger.v3.oas.annotations.media.Content;
// Importación de @Schema de Swagger OpenAPI
import io.swagger.v3.oas.annotations.media.Schema;
// Importación de @ApiResponse de Swagger OpenAPI
import io.swagger.v3.oas.annotations.responses.ApiResponse;
// Importación de @ApiResponses de Swagger OpenAPI
import io.swagger.v3.oas.annotations.responses.ApiResponses;
// Importación de @Tag de Swagger OpenAPI
import io.swagger.v3.oas.annotations.tags.Tag;
// Importación de la anotación @Autowired para inyección de dependencias
import org.springframework.beans.factory.annotation.Autowired;
// Importación de HttpStatus para códigos de respuesta HTTP
import org.springframework.http.HttpStatus;
// Importación de MediaType para definir el formato de respuesta JSON
import org.springframework.http.MediaType;
// Importación de ResponseEntity para estructurar la respuesta HTTP
import org.springframework.http.ResponseEntity;
// Importación de UserDetails de Spring Security (Laboratorio 2)
import org.springframework.security.core.userdetails.UserDetails;
// Importación de UserDetailsService de Spring Security (Laboratorio 2)
import org.springframework.security.core.userdetails.UserDetailsService;
// Importación de las anotaciones de mapeo web de Spring MVC
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

// Importación de Map para respuestas de error en JSON
import java.util.Map;

// Controlador REST para la autenticación JWT en el endpoint /authenticate según el Laboratorio 2 (págs. 16-18)
@RestController
@CrossOrigin
@Tag(name = "Autenticación JWT", description = "Endpoint oficial del Laboratorio 2 para generación de tokens JWT con vigencia de 1 mes")
public class JwtAuthenticationController {

    // Inyección del componente configurador de tokens JWT (Laboratorio 2)
    @Autowired
    private JWTAuthtenticationConfig jwtAuthtenticationConfig;

    // Inyección del servicio UserDetailsService implementado por UsuarioService (Laboratorio 2)
    @Autowired
    private UserDetailsService jwtInMemoryUserDetailsService;

    // Inyección del repositorio de usuarios para validación adicional de APIKey si se envía en el header
    @Autowired
    private UsuarioRepository usuarioRepository;

    // Endpoint POST /authenticate que valida username, password (y opcionalmente APIKey) y retorna el token JWT firmado
    @Operation(summary = "Autenticar usuario y generar token JWT (Laboratorio 2)", description = "Recibe username y password en el cuerpo JSON (y opcionalmente el header APIKey o x-api-key) y retorna el token JWT válido por 1 mes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa, devuelve el token JWT", content = @Content(schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas (usuario, contraseña o APIKey incorrectos)")
    })
    @RequestMapping(
            value = "/authenticate",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> createAuthenticationToken(
            @RequestBody JwtRequest authenticationRequest,
            @RequestHeader(value = "APIKey", required = false) String apiKeyHeader,
            @RequestHeader(value = "x-api-key", required = false) String xApiKeyHeader
    ) throws Exception {
        try {
            // Carga los detalles del usuario desde la base de datos usando UserDetailsService (Laboratorio 2, pág. 16)
            final UserDetails userDetails = jwtInMemoryUserDetailsService
                    .loadUserByUsername(authenticationRequest.getUsername());

            // Verifica que la contraseña enviada coincida con la contraseña registrada del usuario
            if (authenticationRequest.getPassword() == null
                    || !authenticationRequest.getPassword().equals(userDetails.getPassword())) {
                // Retorna HTTP 401 Unauthorized si la contraseña no coincide
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Credenciales inválidas: contraseña incorrecta."));
            }

            // Determina si se envió el encabezado APIKey (formato Lab 2) o x-api-key (formato Proyecto)
            String apiKeyEfectiva = (apiKeyHeader != null && !apiKeyHeader.isBlank()) ? apiKeyHeader : xApiKeyHeader;
            // Si el cliente envió una APIKey en el encabezado, verifica que corresponda al usuario autenticado (Laboratorio 2, pág. 18)
            if (apiKeyEfectiva != null && !apiKeyEfectiva.isBlank()) {
                // Consulta la entidad Usuario completa en la base de datos
                Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername());
                // Comprueba que la APIKey coincida con la registrada en la tabla usuario
                if (usuario == null || !apiKeyEfectiva.equals(usuario.getApikey())) {
                    // Retorna HTTP 401 Unauthorized si la APIKey no corresponde al usuario
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", "Credenciales inválidas: la APIKey no corresponde al usuario."));
                }
            }

            // Genera el token JWT firmado con HS512 y vigencia de 1 mes (Laboratorio 2, pág. 16)
            final String token = jwtAuthtenticationConfig.getJWTToken(userDetails.getUsername());
            // Retorna HTTP 200 OK con el objeto JwtResponse que contiene el token generado
            return ResponseEntity.ok(new JwtResponse(token));
        } catch (Exception e) {
            // Si el usuario no existe o ocurre un error de autenticación, retorna HTTP 401 Unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Acceso denegado: " + e.getMessage()));
        }
    }
}

