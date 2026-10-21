// Paquete de interfaces de servicios de negocio según los Laboratorios 1 y 2
package com.PPOOII.proyecto.services.Interfaces;

// Importación de la entidad Usuario
import com.PPOOII.proyecto.entities.Usuario;
// Importación de la interfaz Map de Java
import java.util.Map;

// Interfaz que define el contrato de negocio para la gestión de Usuarios y autenticación JWT (Laboratorios 1 y 2)
public interface IUsuarioService {

    // Cambia la contraseña de un usuario existente identificado por su login
    Usuario cambiarPassword(String login, String nuevoPassword);

    // Regenera la APIKey de un usuario administrativo y retorna la nueva llave
    String regenerarApiKey(String login);

    // Autentica las credenciales del usuario y emite un token JWT firmado válido por 1 mes
    Map<String, Object> autenticarUsuario(String login, String password, String apiKey);

    // Verifica criptográficamente si un token JWT es válido y no ha expirado
    boolean esTokenValido(String token);

    // Busca un usuario por su login/username
    Usuario buscarPorUsername(String username);
}
