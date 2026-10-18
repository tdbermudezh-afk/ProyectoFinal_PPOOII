package com.PPOOII.proyecto.security; // Define el paquete de seguridad del proyecto

import com.PPOOII.proyecto.entities.Usuario; // Importa la entidad Usuario
import com.PPOOII.proyecto.repository.UsuarioRepository; // Importa el repositorio para consultar usuarios en BD
import com.PPOOII.proyecto.services.UsuarioService; // Importa el servicio de usuarios para validar tokens activos
import jakarta.servlet.http.HttpServletRequest; // Importa la clase que representa la petición HTTP entrante
import jakarta.servlet.http.HttpServletResponse; // Importa la clase que representa la respuesta HTTP saliente
import org.springframework.beans.factory.annotation.Autowired; // Importa la anotación para inyección de dependencias
import org.springframework.stereotype.Component; // Importa la anotación para registrar la clase como componente de Spring
import org.springframework.web.servlet.HandlerInterceptor; // Importa la interfaz de interceptor HTTP de Spring MVC

import java.util.Optional; // Importa Optional para manejar resultados que pueden ser vacíos

@Component // Registra el interceptor como un componente administrado por Spring
public class ApiKeyInterceptor implements HandlerInterceptor { // Declaración de la clase ApiKeyInterceptor

    @Autowired // Inyecta automáticamente el repositorio de usuarios
    private UsuarioRepository usuarioRepository; // Instancia de UsuarioRepository para buscar por APIKey

    @Autowired // Inyecta automáticamente el servicio de usuarios
    private UsuarioService usuarioService; // Instancia de UsuarioService para verificar la validez del token

    @Override // Sobrescribe el método que se ejecuta antes de llegar al controlador
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception { // Método interceptor preHandle
        // Permitir peticiones preflight CORS OPTIONS
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) { // Verifica si la petición es de tipo OPTIONS (CORS preflight)
            return true; // Permite el paso sin exigir cabeceras en preflight
        } // Fin de verificación OPTIONS

        // 1. Obtener los encabezados de seguridad requeridos por la Entrega 2: x-api-key y token
        String apiKey = request.getHeader("x-api-key"); // Extrae el encabezado x-api-key de la petición HTTP
        String token = request.getHeader("token"); // Extrae el encabezado token de la petición HTTP
        if (token == null || token.isBlank()) { // Si no se envió bajo el nombre 'token'
            token = request.getHeader("Authorization"); // Intenta leerlo desde el encabezado estándar 'Authorization'
        } // Fin de lectura alternativa de Authorization

        // 2. Validar presencia del APIKey
        if (apiKey == null || apiKey.isBlank()) { // Verifica si el encabezado x-api-key es nulo o está en blanco
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Establece el código de estado HTTP 401 Unauthorized
            response.setContentType("application/json;charset=UTF-8"); // Define el tipo de contenido como JSON en UTF-8
            response.getWriter().write("{\"error\": \"Acceso denegado: Se requiere el encabezado 'x-api-key'\"}"); // Escribe el mensaje de error en la respuesta
            return false; // Bloquea la petición impidiendo que llegue al controlador
        } // Fin de validación de presencia de APIKey

        // 3. Validar presencia del Token
        if (token == null || token.isBlank()) { // Verifica si el encabezado token es nulo o está en blanco
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Establece el código de estado HTTP 401 Unauthorized
            response.setContentType("application/json;charset=UTF-8"); // Define el tipo de contenido como JSON en UTF-8
            response.getWriter().write("{\"error\": \"Acceso denegado: Se requiere el encabezado 'token' o 'Authorization'\"}"); // Escribe el mensaje de error
            return false; // Bloquea la petición
        } // Fin de validación de presencia de Token

        // 4. Buscar el usuario correspondiente a la API Key en la base de datos
        Optional<Usuario> usuarioOpt = usuarioRepository.findByApikey(apiKey); // Consulta en la tabla usuarios si existe la APIKey enviada
        if (usuarioOpt.isEmpty()) { // Verifica si la APIKey no existe en la base de datos
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Establece el código de estado HTTP 401 Unauthorized
            response.setContentType("application/json;charset=UTF-8"); // Define el tipo de contenido como JSON en UTF-8
            response.getWriter().write("{\"error\": \"Acceso denegado: 'x-api-key' invalida o no reconocida\"}"); // Escribe el error de APIKey inválida
            return false; // Bloquea la petición
        } // Fin de validación de existencia de APIKey

        Usuario usuario = usuarioOpt.get(); // Obtiene la instancia del Usuario propietario de la APIKey

        // 5. Validar que el Token enviado sea un token legítimo y reconocido (no un texto inventado)
        if (!usuarioService.esTokenValido(token, usuario)) { // Comprueba mediante UsuarioService si el token es válido para este usuario
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Establece el código de estado HTTP 401 Unauthorized
            response.setContentType("application/json;charset=UTF-8"); // Define el tipo de contenido como JSON en UTF-8
            response.getWriter().write("{\"error\": \"Acceso denegado: 'token' invalido o no reconocido\"}"); // Escribe el error de token inválido
            return false; // Bloquea la petición cuando el token no existe o es falso
        } // Fin de validación de autenticidad del Token

        // 6. Validar que el usuario corresponda al tipo de persona ADMINISTRADOR ('A')
        if (usuario.getPersona() != null && !"A".equalsIgnoreCase(usuario.getPersona().getTipoPersona())) { // Verifica que el rol sea Administrador ('A')
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // Establece el código de estado HTTP 403 Forbidden
            response.setContentType("application/json;charset=UTF-8"); // Define el tipo de contenido como JSON en UTF-8
            response.getWriter().write("{\"error\": \"Acceso denegado: La operacion solo puede ser consumida por un usuario ADMINISTRADOR\"}"); // Escribe el error de rol
            return false; // Bloquea la petición si no es Administrador
        } // Fin de validación de rol Administrador

        return true; // Permite que la petición continúe hacia el controlador tras superar todas las validaciones
    } // Fin del método preHandle
} // Fin de la clase ApiKeyInterceptor