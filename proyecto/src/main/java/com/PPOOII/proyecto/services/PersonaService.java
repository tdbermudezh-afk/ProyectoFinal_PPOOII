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
            Usuario usuario = generarUsuarioParaAdministrativo(personaGuardada);
            personaGuardada.setUsuarioGenerado(usuario);
        }

        return personaGuardada;
    }

    private Usuario generarUsuarioParaAdministrativo(Persona persona) {
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

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Persona actualizarPersona(Long id, Persona detalles) {
        Persona persona = obtenerPorId(id);

        if (detalles.getIdentificacion() != null && !detalles.getIdentificacion().equals(persona.getIdentificacion())) {
            if (personaRepository.existsByIdentificacion(detalles.getIdentificacion())) {
                throw new IllegalArgumentException("Ya existe una persona registrada con la identificación: " + detalles.getIdentificacion());
            }
            persona.setIdentificacion(detalles.getIdentificacion());
        }

        if (detalles.getCorreo() != null && !detalles.getCorreo().equalsIgnoreCase(persona.getCorreo())) {
            if (personaRepository.existsByCorreo(detalles.getCorreo())) {
                throw new IllegalArgumentException("Ya existe una persona registrada con el correo: " + detalles.getCorreo());
            }
            persona.setCorreo(detalles.getCorreo());
        }

        if (detalles.getNombres() != null) persona.setNombres(detalles.getNombres());
        if (detalles.getApellidos() != null) persona.setApellidos(detalles.getApellidos());
        if (detalles.getTipoIdentificacion() != null) persona.setTipoIdentificacion(detalles.getTipoIdentificacion());

        // Si cambia a administrativo, generar usuario si aún no tiene
        if ("A".equalsIgnoreCase(detalles.getTipoPersona()) && !"A".equalsIgnoreCase(persona.getTipoPersona())) {
            persona.setTipoPersona("A");
            Persona personaActualizada = personaRepository.save(persona);
            Usuario usuario = generarUsuarioParaAdministrativo(personaActualizada);
            personaActualizada.setUsuarioGenerado(usuario);
            return personaActualizada;
        } else if (detalles.getTipoPersona() != null) {
            persona.setTipoPersona(detalles.getTipoPersona());
        }

        return personaRepository.save(persona);
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