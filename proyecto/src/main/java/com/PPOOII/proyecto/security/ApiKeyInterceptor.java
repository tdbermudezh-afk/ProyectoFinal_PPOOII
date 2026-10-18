// Paquete que contiene los interceptores y componentes de seguridad de la API
package com.PPOOII.proyecto.security;

// Importación de la entidad Usuario
import com.PPOOII.proyecto.entities.Usuario;
// Importación del repositorio JPA de Usuario
import com.PPOOII.proyecto.repository.UsuarioRepository;
// Importación del servicio de Usuario para validar criptográficamente el token JWT
import com.PPOOII.proyecto.services.UsuarioService;
// Importación de HttpServletRequest de Jakarta Servlet
import jakarta.servlet.http.HttpServletRequest;
// Importación de HttpServletResponse de Jakarta Servlet
import jakarta.servlet.http.HttpServletResponse;
// Importación de la anotación @Autowired para inyección de dependencias
import org.springframework.beans.factory.annotation.Autowired;
// Importación de la anotación @Component de Spring
import org.springframework.stereotype.Component;
// Importación de la interfaz HandlerInterceptor de Spring MVC
import org.springframework.web.servlet.HandlerInterceptor;

// Importación del contenedor Optional de Java
import java.util.Optional;

// Interceptor de seguridad que valida la presencia y validez de 'x-api-key' y 'token' (JWT) en los endpoints privados
@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    // Inyección del repositorio de usuarios para verificar la APIKey y el rol administrativo
    @Autowired
    private UsuarioRepository usuarioRepository;

    // Inyección del servicio de usuarios para verificar la firma y vigencia del token JWT
    @Autowired
    private UsuarioService usuarioService;

    // Método que se ejecuta antes de cada controlador en las rutas protegidas (/api/**)
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Permitir peticiones preflight CORS de tipo OPTIONS sin bloquearlas
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            // Retorna verdadero para permitir el preflight CORS
            return true;
        }

        // 1. Obtener los encabezados de seguridad requeridos por la Entrega 2: x-api-key (o APIKey) y token (o Authorization)
        String apiKey = request.getHeader("x-api-key");
        // Si no se envió 'x-api-key', verifica también el encabezado 'APIKey' del Laboratorio 2
        if (apiKey == null || apiKey.isBlank()) {
            // Lee el encabezado alternativo 'APIKey'
            apiKey = request.getHeader("APIKey");
        }

        // Lee el encabezado 'token' requerido en las pruebas del proyecto
        String token = request.getHeader("token");
        // Si no se envió 'token', verifica el encabezado estándar 'Authorization' del Laboratorio 2
        if (token == null || token.isBlank()) {
            // Lee el encabezado 'Authorization'
            token = request.getHeader("Authorization");
        }

        // 2. Validar presencia obligatoria del encabezado APIKey
        if (apiKey == null || apiKey.isBlank()) {
            // Establece código de respuesta 401 Unauthorized
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            // Establece tipo de contenido JSON UTF-8
            response.setContentType("application/json;charset=UTF-8");
            // Escribe el mensaje de error JSON esperado
            response.getWriter().write("{\"error\": \"Acceso denegado: Se requiere el encabezado 'x-api-key'\"}");
            // Detiene la ejecución de la petición
            return false;
        }

        // 3. Validar presencia obligatoria del encabezado Token / Authorization
        if (token == null || token.isBlank()) {
            // Establece código de respuesta 401 Unauthorized
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            // Establece tipo de contenido JSON UTF-8
            response.setContentType("application/json;charset=UTF-8");
            // Escribe el mensaje de error JSON esperado
            response.getWriter().write("{\"error\": \"Acceso denegado: Se requiere el encabezado 'token' o 'Authorization'\"}");
            // Detiene la ejecución de la petición
            return false;
        }

        // 4. Validar que el token JWT sea auténtico y no haya expirado (o sea el token administrativo de pruebas)
        if (!usuarioService.esTokenValido(token)) {
            // Establece código de respuesta 401 Unauthorized
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            // Establece tipo de contenido JSON UTF-8
            response.setContentType("application/json;charset=UTF-8");
            // Escribe el mensaje de error indicando que el token JWT es inválido o expiró
            response.getWriter().write("{\"error\": \"Acceso denegado: El token JWT enviado es inválido o ha expirado\"}");
            // Detiene la ejecución de la petición
            return false;
        }

        // 5. Buscar el usuario correspondiente a la APIKey proporcionada
        Optional<Usuario> usuarioOpt = usuarioRepository.findByApikey(apiKey);
        // Verifica si no existe ningún usuario con esa APIKey
        if (usuarioOpt.isEmpty()) {
            // Establece código de respuesta 401 Unauthorized
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            // Establece tipo de contenido JSON UTF-8
            response.setContentType("application/json;charset=UTF-8");
            // Escribe el mensaje de error JSON esperado
            response.getWriter().write("{\"error\": \"Acceso denegado: 'x-api-key' invalida o no reconocida\"}");
            // Detiene la ejecución de la petición
            return false;
        }

        // 6. Validar que el usuario corresponda al tipo de persona ADMINISTRADOR ('A')
        Usuario usuario = usuarioOpt.get();
        // Comprueba que la persona vinculada al usuario sea de tipo Administrativo ('A')
        if (usuario.getPersona() != null && !"A".equalsIgnoreCase(usuario.getPersona().getTipoPersona())) {
            // Establece código de respuesta 403 Forbidden
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            // Establece tipo de contenido JSON UTF-8
            response.setContentType("application/json;charset=UTF-8");
            // Escribe el mensaje de error JSON esperado
            response.getWriter().write("{\"error\": \"Acceso denegado: La operacion solo puede ser consumida por un usuario ADMINISTRADOR\"}");
            // Detiene la ejecución de la petición
            return false;
        }

        // Si todas las validaciones de APIKey, Token JWT y Rol Administrativo son exitosas, permite continuar
        return true;
    }
}