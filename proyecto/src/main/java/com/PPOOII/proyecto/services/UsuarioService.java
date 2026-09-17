package com.PPOOII.proyecto.services;

import com.PPOOII.proyecto.entities.Usuario;
import com.PPOOII.proyecto.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public Usuario cambiarPassword(String login, String nuevoPassword) {
        Usuario usuario = usuarioRepository.findByIdLogin(login)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con login: " + login));

        usuario.setPassword(nuevoPassword);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public String regenerarApiKey(String login) {
        Usuario usuario = usuarioRepository.findByIdLogin(login)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con login: " + login));

        String nuevaApiKey = UUID.randomUUID().toString();
        usuario.setApikey(nuevaApiKey);
        usuarioRepository.save(usuario);

        return nuevaApiKey;
    }
}