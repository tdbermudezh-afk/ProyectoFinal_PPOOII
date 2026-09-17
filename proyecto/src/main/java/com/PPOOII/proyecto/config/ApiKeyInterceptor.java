package com.PPOOII.proyecto.config;

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
        String path = request.getRequestURI();

        // Excluir endpoints públicos y Swagger de la validación
        if (path.startsWith("/api/publico") || path.contains("/swagger-ui") || path.contains("/v3/api-docs")) {
            return true;
        }

        String apiKey = request.getHeader("X-API-KEY");

        if (apiKey == null || apiKey.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Header X-API-KEY no proporcionado.\"}");
            return false;
        }

        boolean existeKey = usuarioRepository.existsByApikey(apiKey);
        if (!existeKey) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"APIKey invalida o no registrada.\"}");
            return false;
        }

        return true;
    }
}