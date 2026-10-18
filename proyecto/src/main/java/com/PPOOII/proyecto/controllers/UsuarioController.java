package com.PPOOII.proyecto.controllers; // Define el paquete de controladores del proyecto

import com.PPOOII.proyecto.entities.Usuario; // Importa la entidad Usuario
import com.PPOOII.proyecto.services.UsuarioService; // Importa el servicio de lógica de negocio para Usuario
import org.springframework.beans.factory.annotation.Autowired; // Importa la anotación para inyección de dependencias
import org.springframework.http.ResponseEntity; // Importa la clase para estructurar respuestas HTTP
import org.springframework.web.bind.annotation.*; // Importa las anotaciones REST de Spring Web

import java.util.Map; // Importa la interfaz Map para manejo de cuerpos JSON clave-valor

@RestController // Marca la clase como un controlador REST que retorna respuestas JSON
@RequestMapping("/api/usuarios") // Define la ruta base para todos los endpoints de usuarios
public class UsuarioController { // Declaración de la clase UsuarioController

    @Autowired // Inyecta automáticamente el servicio de usuarios
    private UsuarioService usuarioService; // Instancia de UsuarioService

    // Servicio POST para autenticar un usuario con login y password y obtener su Token de sesión y APIKey
    @PostMapping("/login") // Mapea las peticiones HTTP POST a /api/usuarios/login
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> credenciales) { // Recibe el login y password en el cuerpo JSON
        String login = credenciales.get("login"); // Extrae el valor del campo login enviado en el body
        String password = credenciales.get("password"); // Extrae el valor del campo password enviado en el body
        if (login == null || login.isBlank() || password == null || password.isBlank()) { // Valida que ambos campos hayan sido enviados
            throw new IllegalArgumentException("Debe enviar los campos 'login' y 'password'."); // Lanza error 400 si falta alguno
        } // Fin de validación de campos obligatorios
        return ResponseEntity.ok(usuarioService.autenticarYGenerarToken(login, password)); // Retorna 200 OK con el token y la apikey del usuario
    } // Fin del método login

    // Cambiar contraseña pasando el login en la URL y la nueva contraseña en el body
    @PutMapping("/{login}/password") // Mapea las peticiones HTTP PUT a /api/usuarios/{login}/password
    public ResponseEntity<Usuario> cambiarPassword( // Método para cambiar el password de un usuario específico
            @PathVariable String login, // Captura el login desde la URL
            @RequestBody Map<String, String> requestBody) { // Captura el cuerpo JSON con la nueva contraseña

        String nuevoPassword = requestBody.get("password"); // Obtiene el valor de la llave "password" del JSON
        if (nuevoPassword == null || nuevoPassword.isBlank()) { // Verifica que la nueva contraseña no sea nula ni vacía
            throw new IllegalArgumentException("La nueva contraseña no puede estar vacía."); // Lanza excepción si está vacía
        } // Fin de validación

        Usuario usuarioActualizado = usuarioService.cambiarPassword(login, nuevoPassword); // Invoca al servicio para actualizar la contraseña en BD
        return ResponseEntity.ok(usuarioActualizado); // Retorna código 200 OK con el usuario actualizado
    } // Fin del método cambiarPassword

    // Regenerar APIKey de un usuario específico mediante un servicio GET
    @GetMapping("/{login}/apikey/regenerar") // Mapea las peticiones HTTP GET a /api/usuarios/{login}/apikey/regenerar
    public ResponseEntity<Map<String, String>> regenerarApiKey(@PathVariable String login) { // Recibe el login del usuario por la URL
        String nuevaApiKey = usuarioService.regenerarApiKey(login); // Genera y persiste la nueva APIKey a través del servicio
        return ResponseEntity.ok(Map.of( // Retorna respuesta 200 OK con el detalle de la nueva llave
                "login", login, // Login del usuario al que se le regeneró la llave
                "nuevaApiKey", nuevaApiKey, // Valor de la nueva APIKey generada
                "mensaje", "APIKey regenerada con éxito" // Mensaje de confirmación
        )); // Fin de construcción de la respuesta
    } // Fin del método regenerarApiKey
} // Fin de la clase UsuarioController