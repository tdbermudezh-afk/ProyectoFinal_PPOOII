package com.PPOOII.proyecto.services;

import com.PPOOII.proyecto.entities.Persona;
import com.PPOOII.proyecto.entities.Usuario;
import com.PPOOII.proyecto.entities.UsuarioPK;
import com.PPOOII.proyecto.repository.PersonaRepository;
import com.PPOOII.proyecto.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PersonaService {

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public Persona crearPersona(Persona persona) {
        if (personaRepository.existsByIdentificacion(persona.getIdentificacion())) {
            throw new IllegalArgumentException("Ya existe una persona registrada con la identificación: " + persona.getIdentificacion());
        }

        if (personaRepository.existsByCorreo(persona.getCorreo())) {
            throw new IllegalArgumentException("Ya existe una persona registrada con el correo: " + persona.getCorreo());
        }

        Persona personaGuardada = personaRepository.save(persona);

        // Regla: Si la persona es de tipo ADMINISTRATIVO ('A'), se genera automáticamente su Usuario
        if ("A".equalsIgnoreCase(persona.getTipoPersona())) {
            generarUsuarioParaAdministrativo(personaGuardada);
        }

        return personaGuardada;
    }

    private void generarUsuarioParaAdministrativo(Persona persona) {
        // Nemotecnia: Primera letra del nombre + primera letra del apellido + identificación
        String primeraLetraNombre = persona.getNombres().trim().substring(0, 1).toLowerCase();
        String primeraLetraApellido = persona.getApellidos().trim().substring(0, 1).toLowerCase();
        String loginNemotecnico = primeraLetraNombre + primeraLetraApellido + persona.getIdentificacion();

        // Generación automática de contraseña y APIKey aleatoria
        String passwordGenerado = "Pwd" + UUID.randomUUID().toString().substring(0, 8);
        String apiKeyGenerada = UUID.randomUUID().toString();

        UsuarioPK usuarioPK = new UsuarioPK(persona.getId(), loginNemotecnico);

        Usuario usuario = new Usuario();
        usuario.setId(usuarioPK);
        usuario.setPersona(persona);
        usuario.setPassword(passwordGenerado);
        usuario.setApikey(apiKeyGenerada);

        usuarioRepository.save(usuario);
    }

    public List<Persona> listarTodas() {
        return personaRepository.findAll();
    }

    public Persona obtenerPorId(Long id) {
        return personaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Persona no encontrada con ID: " + id));
    }

    public List<Persona> listarPorTipo(String tipoPersona) {
        return personaRepository.findByTipoPersona(tipoPersona);
    }
}