// Paquete de servicios de lógica de negocio de la aplicación
package com.PPOOII.proyecto.services;

// Importación de la entidad Persona
import com.PPOOII.proyecto.entities.Persona;
// Importación de la entidad Usuario
import com.PPOOII.proyecto.entities.Usuario;
// Importación de la llave primaria compuesta UsuarioPK
import com.PPOOII.proyecto.entities.UsuarioPK;
// Importación del repositorio JPA de Persona
import com.PPOOII.proyecto.repository.PersonaRepository;
// Importación del repositorio JPA de Usuario
import com.PPOOII.proyecto.repository.UsuarioRepository;
// Importación de la interfaz de servicio IPersonaService (Laboratorio 1)
import com.PPOOII.proyecto.services.Interfaces.IPersonaService;
// Importación del gestor de logs Log4j2 (Laboratorio 1)
import org.apache.logging.log4j.LogManager;
// Importación de la interfaz Logger de Log4j2 (Laboratorio 1)
import org.apache.logging.log4j.Logger;
// Importación de la anotación @Autowired para inyección de dependencias
import org.springframework.beans.factory.annotation.Autowired;
// Importación de la anotación @Service de Spring
import org.springframework.stereotype.Service;
// Importación de la anotación @Transactional para manejo transaccional
import org.springframework.transaction.annotation.Transactional;

// Importación de la estructura List de Java
import java.util.List;
// Importación del generador de identificadores únicos UUID
import java.util.UUID;

// Componente de servicio que implementa IPersonaService para la gestión de Personas (Conductores y Administrativos)
@Service("PersonaService")
public class PersonaService implements IPersonaService {

    // Instancia del logger de auditoría según el estándar del Laboratorio 1
    private static final Logger logger = LogManager.getLogger(PersonaService.class);

    // Inyección del repositorio JPA de Personas
    @Autowired
    private PersonaRepository personaRepository;

    // Inyección del repositorio JPA de Usuarios
    @Autowired
    private UsuarioRepository usuarioRepository;

    // Registra una nueva persona y, si es Administrativa ('A'), genera automáticamente su Usuario
    @Override
    @Transactional
    public Persona crearPersona(Persona persona) {
        // Verifica que el número de identificación no esté duplicado
        if (personaRepository.existsByIdentificacion(persona.getIdentificacion())) {
            // Lanza excepción si la identificación ya existe
            throw new IllegalArgumentException("Ya existe una persona registrada con la identificación: " + persona.getIdentificacion());
        }

        // Verifica que el correo electrónico no esté duplicado
        if (personaRepository.existsByCorreo(persona.getCorreo())) {
            // Lanza excepción si el correo ya existe
            throw new IllegalArgumentException("Ya existe una persona registrada con el correo: " + persona.getCorreo());
        }

        // Regla de negocio: Si la persona es de tipo CONDUCTOR ('C'), debe adjuntar su licencia de conducción en PDF Base64
        if ("C".equalsIgnoreCase(persona.getTipoPersona())) {
            // Verifica que el campo licenciaConduccion no sea nulo ni esté vacío
            if (persona.getLicenciaConduccion() == null || persona.getLicenciaConduccion().trim().isEmpty()) {
                // Lanza excepción indicando que la licencia de conducción en PDF Base64 es obligatoria para conductores
                throw new IllegalArgumentException("Para las personas de tipo CONDUCTOR (C) es obligatorio adjuntar la licencia de conducción en formato PDF Base64 ('licenciaConduccion').");
            }
        }

        // Persiste la entidad Persona en la base de datos
        Persona personaGuardada = personaRepository.save(persona);
        // Registra en el log la creación de la persona
        logger.info("Persona creada exitosamente con ID: {} y tipo: {}", personaGuardada.getId(), personaGuardada.getTipoPersona());

        // Regla: Si la persona es de tipo ADMINISTRATIVO ('A'), se genera automáticamente su Usuario
        if ("A".equalsIgnoreCase(persona.getTipoPersona())) {
            // Genera y guarda las credenciales del usuario administrativo
            Usuario usuario = generarUsuarioParaAdministrativo(personaGuardada);
            // Asocia el usuario generado al campo transiente de respuesta JSON
            personaGuardada.setUsuarioGenerado(usuario);
        }

        // Retorna la persona guardada (con su usuarioGenerado si aplica)
        return personaGuardada;
    }

    // Método auxiliar que construye el login nemotécnico, password y APIKey para un usuario administrativo
    private Usuario generarUsuarioParaAdministrativo(Persona persona) {
        // Nemotecnia: Primera letra del nombre + primera letra del apellido + identificación
        String primeraLetraNombre = persona.getNombres().trim().substring(0, 1).toLowerCase();
        // Obtiene la primera letra del apellido en minúscula
        String primeraLetraApellido = persona.getApellidos().trim().substring(0, 1).toLowerCase();
        // Concatena las iniciales con el número de identificación
        String loginNemotecnico = primeraLetraNombre + primeraLetraApellido + persona.getIdentificacion();

        // Generación automática de contraseña aleatoria con prefijo Pwd
        String passwordGenerado = "Pwd" + UUID.randomUUID().toString().substring(0, 8);
        // Generación automática de llave APIKey única mediante UUID
        String apiKeyGenerada = UUID.randomUUID().toString();

        // Construye la llave primaria compuesta (id de persona + login nemotécnico)
        UsuarioPK usuarioPK = new UsuarioPK(persona.getId(), loginNemotecnico);

        // Crea la instancia de la entidad Usuario
        Usuario usuario = new Usuario();
        // Asigna la llave primaria compuesta
        usuario.setId(usuarioPK);
        // Vincula la referencia a la entidad Persona
        usuario.setPersona(persona);
        // Asigna la contraseña generada
        usuario.setPassword(passwordGenerado);
        // Asigna la APIKey generada
        usuario.setApikey(apiKeyGenerada);

        // Registra en el log la generación automática del usuario administrativo
        logger.info("Usuario administrativo generado automáticamente con login: {}", loginNemotecnico);
        // Persiste y retorna el nuevo usuario
        return usuarioRepository.save(usuario);
    }

    // Actualiza la información de una persona existente por su ID
    @Override
    @Transactional
    public Persona actualizarPersona(Long id, Persona detalles) {
        // Recupera la persona actual o lanza excepción si no existe
        Persona persona = obtenerPorId(id);

        // Si la identificación cambia, verifica que no esté registrada en otra persona
        if (detalles.getIdentificacion() != null && !detalles.getIdentificacion().equals(persona.getIdentificacion())) {
            // Comprueba unicidad de la nueva identificación
            if (personaRepository.existsByIdentificacion(detalles.getIdentificacion())) {
                // Lanza excepción por identificación duplicada
                throw new IllegalArgumentException("Ya existe una persona registrada con la identificación: " + detalles.getIdentificacion());
            }
            // Actualiza el número de identificación
            persona.setIdentificacion(detalles.getIdentificacion());
        }

        // Si el correo cambia, verifica que no esté registrado en otra persona
        if (detalles.getCorreo() != null && !detalles.getCorreo().equalsIgnoreCase(persona.getCorreo())) {
            // Comprueba unicidad del nuevo correo electrónico
            if (personaRepository.existsByCorreo(detalles.getCorreo())) {
                // Lanza excepción por correo duplicado
                throw new IllegalArgumentException("Ya existe una persona registrada con el correo: " + detalles.getCorreo());
            }
            // Actualiza el correo electrónico
            persona.setCorreo(detalles.getCorreo());
        }

        // Actualiza los nombres si fueron proporcionados
        if (detalles.getNombres() != null) persona.setNombres(detalles.getNombres());
        // Actualiza los apellidos si fueron proporcionados
        if (detalles.getApellidos() != null) persona.setApellidos(detalles.getApellidos());
        // Actualiza el tipo de identificación si fue proporcionado
        if (detalles.getTipoIdentificacion() != null) persona.setTipoIdentificacion(detalles.getTipoIdentificacion());
        // Actualiza el PDF Base64 de la licencia de conducción si fue proporcionado
        if (detalles.getLicenciaConduccion() != null) persona.setLicenciaConduccion(detalles.getLicenciaConduccion());
        // Actualiza la fecha de vigencia de la licencia de conducción si fue proporcionada
        if (detalles.getFechaVigenciaLicencia() != null) persona.setFechaVigenciaLicencia(detalles.getFechaVigenciaLicencia());

        // Si cambia a administrativo, generar usuario si aún no tiene
        if ("A".equalsIgnoreCase(detalles.getTipoPersona()) && !"A".equalsIgnoreCase(persona.getTipoPersona())) {
            // Actualiza el tipo de persona a Administrativo ('A')
            persona.setTipoPersona("A");
            // Guarda los cambios de la persona
            Persona personaActualizada = personaRepository.save(persona);
            // Genera automáticamente su usuario administrativo
            Usuario usuario = generarUsuarioParaAdministrativo(personaActualizada);
            // Asigna el usuario generado a la respuesta
            personaActualizada.setUsuarioGenerado(usuario);
            // Retorna la persona actualizada con su usuario
            return personaActualizada;
        } else if (detalles.getTipoPersona() != null) {
            // Actualiza el tipo de persona cuando no requiere generación de nuevo usuario
            persona.setTipoPersona(detalles.getTipoPersona());
        }

        // Registra en el log la actualización de la persona
        logger.info("Persona actualizada exitosamente con ID: {}", id);
        // Guarda y retorna la persona actualizada
        return personaRepository.save(persona);
    }

    // Lista todas las personas registradas en la base de datos
    @Override
    public List<Persona> listarTodas() {
        // Retorna la lista completa de personas desde el repositorio
        return personaRepository.findAll();
    }

    // Busca una persona por su ID primario o lanza excepción si no existe
    @Override
    public Persona obtenerPorId(Long id) {
        // Busca por ID o lanza excepción de ejecución si no se encuentra
        return personaRepository.findById(id)
                // Mensaje descriptivo cuando el ID no existe
                .orElseThrow(() -> new RuntimeException("Persona no encontrada con ID: " + id));
    }

    // Filtra y retorna las personas según su tipo ('C' Conductor o 'A' Administrativo)
    @Override
    public List<Persona> listarPorTipo(String tipoPersona) {
        // Ejecuta la búsqueda por tipo de persona en el repositorio
        return personaRepository.findByTipoPersona(tipoPersona);
    }
}