package com.PPOOII.proyecto.security;

import com.PPOOII.proyecto.entities.Usuario;
import com.PPOOII.proyecto.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Permitir peticiones preflight CORS OPTIONS
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 1. Obtener los encabezados de seguridad requeridos por la Entrega 2: x-api-key y token
        String apiKey = request.getHeader("x-api-key");
        String token = request.getHeader("token");
        if (token == null || token.isBlank()) {
            token = request.getHeader("Authorization");
        }

        // 2. Validar presencia del APIKey
        if (apiKey == null || apiKey.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"Acceso denegado: Se requiere el encabezado 'x-api-key'\"}");
            return false;
        }

        // 3. Validar presencia del Token
        if (token == null || token.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"Acceso denegado: Se requiere el encabezado 'token' o 'Authorization'\"}");
            return false;
        }

        // 4. Buscar el usuario correspondiente a la API Key
        Optional<Usuario> usuarioOpt = usuarioRepository.findByApikey(apiKey);
        if (usuarioOpt.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"Acceso denegado: 'x-api-key' invalida o no reconocida\"}");
            return false;
        }

        // 5. Validar que el usuario corresponda al tipo de persona ADMINISTRADOR ('A')
        Usuario usuario = usuarioOpt.get();
        if (usuario.getPersona() != null && !"A".equalsIgnoreCase(usuario.getPersona().getTipoPersona())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"Acceso denegado: La operacion solo puede ser consumida por un usuario ADMINISTRADOR\"}");
            return false;
        }

        return true;
    }
}