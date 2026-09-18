package com.PPOOII.proyecto.security;

import com.PPOOII.proyecto.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // 1. Obtener el encabezado x-api-key de la petición HTTP
        String apiKey = request.getHeader("x-api-key");

        // 2. Si no enviaron la llave, bloqueamos con un error 401 (No autorizado)
        if (apiKey == null || apiKey.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Acceso denegado: Se requiere el encabezado 'x-api-key'");
            return false;
        }

        // 3. Buscar si la API Key existe en la base de datos de usuarios
        boolean apiKeyValida = usuarioRepository.existsByApikey(apiKey);
        
        if (!apiKeyValida) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Acceso denegado: 'x-api-key' invalida o no reconocida");
            return false;
        }

        // Si la llave existe, dejamos pasar la petición al controlador
        return true;
    }
}