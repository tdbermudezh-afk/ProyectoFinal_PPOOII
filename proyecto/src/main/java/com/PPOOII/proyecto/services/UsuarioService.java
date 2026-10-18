package com.PPOOII.proyecto.services; // Define el paquete al que pertenece el servicio

import com.PPOOII.proyecto.entities.Usuario; // Importa la entidad Usuario
import com.PPOOII.proyecto.repository.UsuarioRepository; // Importa el repositorio de Usuario
import org.springframework.beans.factory.annotation.Autowired; // Importa la anotación para inyección de dependencias
import org.springframework.stereotype.Service; // Importa la anotación que marca la clase como un servicio de Spring
import org.springframework.transaction.annotation.Transactional; // Importa la anotación para manejo transaccional

import java.util.Map; // Importa la interfaz Map para estructurar respuestas y almacenar tokens
import java.util.UUID; // Importa la clase UUID para generar identificadores únicos
import java.util.concurrent.ConcurrentHashMap; // Importa ConcurrentHashMap para almacenar tokens de sesión de forma segura en memoria

@Service // Indica a Spring que esta clase es un componente de capa de servicio
public class UsuarioService { // Declaración de la clase UsuarioService

    // Token maestro por defecto para pruebas automatizadas en Postman y Front-End
    private static final String TOKEN_DEFECTO = "token-admin-2026"; // Constante con el valor del token válido por defecto

    // Mapa en memoria que asocia el login de un usuario con el último token dinámico emitido al iniciar sesión
    private final Map<String, String> tokensActivosPorUsuario = new ConcurrentHashMap<>(); // Almacén concurrente de tokens activos

    @Autowired // Inyecta automáticamente la instancia de UsuarioRepository
    private UsuarioRepository usuarioRepository; // Repositorio para consultar y persistir usuarios en la base de datos

    @Transactional // Ejecuta el método dentro de una transacción de base de datos
    public Usuario cambiarPassword(String login, String nuevoPassword) { // Método para actualizar la contraseña de un usuario
        Usuario usuario = usuarioRepository.findByIdLogin(login) // Busca el usuario por su login en la base de datos
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con login: " + login)); // Lanza excepción si no existe

        usuario.setPassword(nuevoPassword); // Asigna la nueva contraseña a la entidad usuario
        return usuarioRepository.save(usuario); // Guarda y retorna el usuario actualizado en la base de datos
    } // Fin del método cambiarPassword

    @Transactional // Ejecuta el método dentro de una transacción de base de datos
    public String regenerarApiKey(String login) { // Método para generar una nueva APIKey para un usuario
        Usuario usuario = usuarioRepository.findByIdLogin(login) // Busca el usuario por su login en la base de datos
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con login: " + login)); // Lanza excepción si no existe

        String nuevaApiKey = UUID.randomUUID().toString(); // Genera una nueva cadena UUID aleatoria como APIKey
        usuario.setApikey(nuevaApiKey); // Actualiza el atributo apikey en la entidad usuario
        usuarioRepository.save(usuario); // Persiste el cambio en la tabla usuarios

        return nuevaApiKey; // Retorna la nueva APIKey generada
    } // Fin del método regenerarApiKey

    @Transactional(readOnly = true) // Ejecuta la consulta en modo de solo lectura
    public Map<String, String> autenticarYGenerarToken(String login, String password) { // Método para autenticar un usuario y emitir un token válido
        Usuario usuario = usuarioRepository.findByIdLogin(login) // Busca el usuario por login en la tabla usuarios
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas: Usuario no encontrado.")); // Lanza error si el login no existe

        if (!usuario.getPassword().equals(password)) { // Verifica si la contraseña enviada coincide con la almacenada en BD
            throw new IllegalArgumentException("Credenciales inválidas: Contraseña incorrecta."); // Lanza error si la contraseña no coincide
        } // Fin de validación de contraseña

        String tokenGenerado = "Bearer tok-" + UUID.randomUUID().toString(); // Construye un nuevo token con prefijo Bearer y un UUID único
        tokensActivosPorUsuario.put(login, tokenGenerado); // Registra el token emitido para este usuario en el mapa de sesiones activas

        return Map.of( // Construye y retorna el mapa con los datos de autenticación
                "login", login, // Incluye el login del usuario autenticado
                "apikey", usuario.getApikey(), // Incluye la x-api-key vigente del usuario en la base de datos
                "token", tokenGenerado, // Incluye el token generado para usar en los encabezados de seguridad
                "mensaje", "Autenticación exitosa" // Mensaje informativo de éxito
        ); // Fin de la construcción del mapa de respuesta
    } // Fin del método autenticarYGenerarToken

    public boolean esTokenValido(String tokenRecibido, Usuario usuario) { // Método que valida si el token enviado en el header es legítimo
        if (tokenRecibido == null || tokenRecibido.isBlank() || usuario == null) { // Verifica que ni el token ni el usuario sean nulos o vacíos
            return false; // Retorna falso si falta información
        } // Fin de verificación de nulidad

        String tokenLimpio = tokenRecibido.trim(); // Elimina espacios en blanco al inicio y al final del encabezado recibido

        // 1. Verifica si corresponde al token oficial configurado para la colección de Postman y el Front-End
        if ( tokenLimpio.equalsIgnoreCase("Bearer " + TOKEN_DEFECTO) // Compara si viene con el prefijo 'Bearer token-admin-2026'
                || tokenLimpio.equals(TOKEN_DEFECTO) ) { // O si se envió directamente 'token-admin-2026'
            return true; // Retorna verdadero al ser el token autorizado del sistema
        } // Fin de validación del token por defecto

        // 2. Verifica si corresponde a un token dinámico emitido por el servicio /api/usuarios/login para este usuario
        String loginUsuario = usuario.getId().getLogin(); // Obtiene el login del usuario dueño de la APIKey
        String tokenActivo = tokensActivosPorUsuario.get(loginUsuario); // Consulta si el usuario tiene un token dinámico activo en memoria
        if (tokenActivo != null) { // Si existe un token emitido para este usuario
            String tokenActivoSinBearer = tokenActivo.replace("Bearer ", "").trim(); // Extrae la parte del token sin el prefijo Bearer
            if (tokenLimpio.equals(tokenActivo) || tokenLimpio.equals(tokenActivoSinBearer)) { // Compara el token recibido con el token emitido
                return true; // Retorna verdadero si el token coincide con la sesión del usuario
            } // Fin de comparación con el token dinámico
        } // Fin de verificación de token en memoria

        return false; // Rechaza cualquier otro token inventado, inexistente o expirado retornando falso
    } // Fin del método esTokenValido
} // Fin de la clase UsuarioService