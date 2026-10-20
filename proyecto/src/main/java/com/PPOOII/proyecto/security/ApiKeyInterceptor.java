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

    // Método auxiliar para garantizar cabeceras CORS incluso en respuestas de rechazo 401/403
    private void agregarCabecerasCors(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "*");
    }

    // Método que se ejecuta antes de cada controlador en las rutas protegidas (/api/**)
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        agregarCabecerasCors(response);

        // Permitir peticiones preflight CORS de tipo OPTIONS sin bloquearlas
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        // 1. Obtener los encabezados de seguridad requeridos por la Entrega 2: x-api-key (o APIKey) y token (o Authorization)
        String apiKey = request.getHeader("x-api-key");
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = request.getHeader("APIKey");
        }

        String token = request.getHeader("token");
        if (token == null || token.isBlank()) {
            token = request.getHeader("Authorization");
        }

        // 2. Validar presencia obligatoria del encabezado APIKey
        if (apiKey == null || apiKey.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\": 401, \"error\": \"Acceso denegado: Se requiere el encabezado 'x-api-key' (Modo Público/Sin Credenciales rechazado)\"}");
            return false;
        }

        // 3. Validar presencia obligatoria del encabezado Token / Authorization
        if (token == null || token.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\": 401, \"error\": \"Acceso denegado: Se requiere el encabezado 'token' o 'Authorization' (Bearer JWT)\"}");
            return false;
        }

        // 4. Validar que el token JWT sea auténtico y no haya expirado
        if (!usuarioService.esTokenValido(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\": 401, \"error\": \"Acceso denegado: El token JWT enviado es inválido o ha expirado\"}");
            return false;
        }

        // 5. Buscar el usuario correspondiente a la APIKey proporcionada
        Optional<Usuario> usuarioOpt = usuarioRepository.findByApikey(apiKey);
        if (usuarioOpt.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\": 401, \"error\": \"Acceso denegado: 'x-api-key' inválida o revocada tras regeneración\"}");
            return false;
        }

        // 6. Validar que el usuario corresponda al tipo de persona ADMINISTRADOR ('A')
        Usuario usuario = usuarioOpt.get();
        if (usuario.getPersona() != null && !"A".equalsIgnoreCase(usuario.getPersona().getTipoPersona())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\": 403, \"error\": \"Acceso denegado: La operación solo puede ser consumida por un usuario ADMINISTRATIVO ('A')\"}");
            return false;
        }

        return true;
    }
}
