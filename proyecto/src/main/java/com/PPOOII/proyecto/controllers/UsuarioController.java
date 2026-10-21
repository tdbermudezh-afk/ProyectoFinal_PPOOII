// Paquete de controladores REST de la aplicación
package com.PPOOII.proyecto.controllers;

// Importación de la entidad Usuario
import com.PPOOII.proyecto.entities.Usuario;
// Importación del servicio UsuarioService
import com.PPOOII.proyecto.services.UsuarioService;
// Importación de @Operation de Swagger OpenAPI (Laboratorio 4)
import io.swagger.v3.oas.annotations.Operation;
// Importación de @Content de Swagger OpenAPI (Laboratorio 4)
import io.swagger.v3.oas.annotations.media.Content;
// Importación de @Schema de Swagger OpenAPI (Laboratorio 4)
import io.swagger.v3.oas.annotations.media.Schema;
// Importación de @ApiResponse de Swagger OpenAPI (Laboratorio 4)
import io.swagger.v3.oas.annotations.responses.ApiResponse;
// Importación de @ApiResponses de Swagger OpenAPI (Laboratorio 4)
import io.swagger.v3.oas.annotations.responses.ApiResponses;
// Importación de @Tag de Swagger OpenAPI (Laboratorio 4)
import io.swagger.v3.oas.annotations.tags.Tag;
// Importación de @Autowired para inyección de dependencias
import org.springframework.beans.factory.annotation.Autowired;
// Importación de ResponseEntity para respuestas HTTP
import org.springframework.http.ResponseEntity;
// Importación de anotaciones web de Spring MVC
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Importación de la interfaz Map de Java
import java.util.Map;

// Controlador REST para la administración de credenciales de Usuarios (password, APIKey y login JWT)
@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
@Tag(name = "Usuarios y Credenciales (APIKey / JWT)", description = "Endpoints para login JWT de 1 mes de vigencia, cambio de contraseña y regeneración de APIKey")
public class UsuarioController {

    // Inyección del servicio de negocio de Usuarios
    @Autowired
    private UsuarioService usuarioService;

    // Endpoint POST /api/usuarios/login para autenticar un usuario y generar un token JWT firmado válido por 1 mes
    @Operation(summary = "Iniciar sesión y generar token JWT (vigencia 1 mes)", description = "Valida login y password (y opcionalmente x-api-key / APIKey) y retorna el token JWT firmado con HS512 válido por 30 días.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa, retorna el token JWT y datos de vigencia", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Credenciales inválidas", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> requestBody,
            @RequestHeader(value = "x-api-key", required = false) String xApiKeyHeader,
            @RequestHeader(value = "APIKey", required = false) String apiKeyHeader) {

        // Obtiene el login (o username) enviado en el cuerpo JSON
        String login = requestBody.getOrDefault("login", requestBody.get("username"));
        // Obtiene la contraseña enviada en el cuerpo JSON
        String password = requestBody.get("password");
        // Determina la APIKey si fue enviada en el cuerpo o en alguno de los encabezados HTTP
        String apiKey = requestBody.getOrDefault("apiKey", (xApiKeyHeader != null && !xApiKeyHeader.isBlank()) ? xApiKeyHeader : apiKeyHeader);

        // Valida que el login no sea nulo ni vacío
        if (login == null || login.isBlank()) {
            // Lanza excepción si no se especificó el login
            throw new IllegalArgumentException("Debe proporcionar el 'login' (o 'username') del usuario.");
        }

        // Autentica al usuario y genera el token JWT de 1 mes de duración
        Map<String, Object> respuesta = usuarioService.autenticarUsuario(login.trim(), password, apiKey);
        // Retorna HTTP 200 OK con el token y metadatos de expiración
        return ResponseEntity.ok(respuesta);
    }

    // Cambiar contraseña pasando el login en la URL y la nueva contraseña en el body
    @Operation(summary = "Cambiar contraseña de un usuario", description = "Actualiza la contraseña del usuario administrativo identificado por su login.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contraseña actualizada exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Usuario.class))),
            @ApiResponse(responseCode = "400", description = "Contraseña vacía o usuario no encontrado", content = @Content)
    })
    @PutMapping("/{login}/password")
    public ResponseEntity<Usuario> cambiarPassword(
            @PathVariable String login,
            @RequestBody Map<String, String> requestBody) {

        // Extrae la nueva contraseña del cuerpo JSON
        String nuevoPassword = requestBody.get("password");
        // Verifica que la nueva contraseña no sea nula ni esté vacía
        if (nuevoPassword == null || nuevoPassword.isBlank()) {
            // Lanza excepción si la contraseña está en blanco
            throw new IllegalArgumentException("La nueva contraseña no puede estar vacía.");
        }

        // Invoca el servicio para actualizar y persistir la nueva contraseña
        Usuario usuarioActualizado = usuarioService.cambiarPassword(login, nuevoPassword);
        // Retorna HTTP 200 OK con el usuario actualizado
        return ResponseEntity.ok(usuarioActualizado);
    }

    // Regenerar APIKey de un usuario específico mediante un servicio GET
    @Operation(summary = "Regenerar APIKey de un usuario", description = "Genera una nueva APIKey aleatoria (UUID) para el usuario especificado por su login.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "APIKey regenerada exitosamente", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Usuario no encontrado", content = @Content)
    })
    @GetMapping("/{login}/apikey/regenerar")
    public ResponseEntity<Map<String, String>> regenerarApiKey(@PathVariable String login) {
        // Invoca el servicio para regenerar la APIKey
        String nuevaApiKey = usuarioService.regenerarApiKey(login);
        // Retorna HTTP 200 OK con el login, la nueva APIKey y el mensaje de confirmación
        return ResponseEntity.ok(Map.of(
                // Login del usuario actualizado
                "login", login,
                // Nueva APIKey generada
                "nuevaApiKey", nuevaApiKey,
                // Mensaje de confirmación
                "mensaje", "APIKey regenerada con éxito"
        ));
    }
}