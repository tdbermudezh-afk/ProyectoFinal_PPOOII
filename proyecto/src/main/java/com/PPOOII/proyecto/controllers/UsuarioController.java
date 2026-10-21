package com.PPOOII.proyecto.controllers;

import com.PPOOII.proyecto.entities.Usuario;
import com.PPOOII.proyecto.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // Cambiar contraseña pasando el login en la URL y la nueva contraseña en el body
    @PutMapping("/{login}/password")
    public ResponseEntity<Usuario> cambiarPassword(
            @PathVariable String login,
            @RequestBody Map<String, String> requestBody) {

        String nuevoPassword = requestBody.get("password");
        if (nuevoPassword == null || nuevoPassword.isBlank()) {
            throw new IllegalArgumentException("La nueva contraseña no puede estar vacía.");
        }

        Usuario usuarioActualizado = usuarioService.cambiarPassword(login, nuevoPassword);
        return ResponseEntity.ok(usuarioActualizado);
    }

    // Regenerar APIKey de un usuario específico mediante un servicio GET
    @GetMapping("/{login}/apikey/regenerar")
    public ResponseEntity<Map<String, String>> regenerarApiKey(@PathVariable String login) {
        String nuevaApiKey = usuarioService.regenerarApiKey(login);
        return ResponseEntity.ok(Map.of(
                "login", login,
                "nuevaApiKey", nuevaApiKey,
                "mensaje", "APIKey regenerada con éxito"
        ));
    }
}