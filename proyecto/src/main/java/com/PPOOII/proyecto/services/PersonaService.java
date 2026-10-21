// Paquete de servicios de lógica de negocio de la aplicación
package com.PPOOII.proyecto.services;

// Importación de la entidad Persona
import com.PPOOII.proyecto.entities.Persona;
// Importación de la entidad Usuario
import com.PPOOII.proyecto.entities.Usuario;
// Importación de la llave primaria compuesta UsuarioPK
import com.PPOOII.proyecto.entities.UsuarioPK;
// Importación de excepción personalizada ResourceNotFoundException
import com.PPOOII.proyecto.exceptions.ResourceNotFoundException;
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
// Importación de MultipartFile para carga de archivos PDF
import org.springframework.web.multipart.MultipartFile;

// Importación de IOException para lectura de bytes del PDF
import java.io.IOException;
// Importación de LocalDate para fecha de vigencia de licencia
import java.time.LocalDate;
// Importación de Base64 para codificar PDF a Base64
import java.util.Base64;
// Importación de HashMap para mapas de estadísticas
import java.util.HashMap;
// Importación de la estructura List de Java
import java.util.List;
// Importación de la interfaz Map de Java
import java.util.Map;
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
        // Verifica si la persona ya tiene un usuario asignado (relación 1:1 estricta)
        var existente = usuarioRepository.findByIdIdPersona(persona.getId());
        if (existente.isPresent()) {
            return existente.get();
        }

        // Nemotecnia: Primera letra del primer nombre + primera letra del primer apellido + identificación
        String primeraLetraNombre = persona.getNombres().trim().substring(0, 1).toLowerCase();
        // Obtiene la primera letra del primer apellido en minúscula
        String primeraLetraApellido = persona.getApellidos().trim().substring(0, 1).toLowerCase();
        // Concatena las iniciales con el número de identificación
        String loginNemotecnico = primeraLetraNombre + primeraLetraApellido + persona.getIdentificacion().trim();

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

    // Vincula el usuario generado en el campo transiente si la persona es de tipo Administrativo ('A')
    private void adjuntarUsuarioSiEsAdministrativo(Persona persona) {
        if (persona != null && "A".equalsIgnoreCase(persona.getTipoPersona()) && persona.getUsuarioGenerado() == null) {
            usuarioRepository.findByIdIdPersona(persona.getId()).ifPresent(persona::setUsuarioGenerado);
        }
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
        Persona guardada = personaRepository.save(persona);
        adjuntarUsuarioSiEsAdministrativo(guardada);
        return guardada;
    }

    // Sube un archivo PDF físico de licencia de conducción, lo convierte a Base64 y lo almacena en el campo LONGBLOB
    @Override
    @Transactional
    public Persona cargarLicenciaPdf(Long id, String fechaVigenciaLicencia, MultipartFile archivoPdf) {
        if (archivoPdf == null || archivoPdf.isEmpty()) {
            throw new IllegalArgumentException("Debe adjuntar un archivo PDF válido para la licencia de conducción.");
        }
        Persona persona = obtenerPorId(id);
        if (!"C".equalsIgnoreCase(persona.getTipoPersona())) {
            throw new IllegalArgumentException("Solo las personas de tipo CONDUCTOR (C) requieren licencia de conducción.");
        }
        try {
            String base64Pdf = Base64.getEncoder().encodeToString(archivoPdf.getBytes());
            persona.setLicenciaConduccion(base64Pdf);
            if (fechaVigenciaLicencia != null && !fechaVigenciaLicencia.isBlank()) {
                persona.setFechaVigenciaLicencia(LocalDate.parse(fechaVigenciaLicencia.trim()));
            }
            logger.info("Licencia PDF convertida a Base64 y guardada en LONGBLOB para el conductor ID: {}", id);
            return personaRepository.save(persona);
        } catch (IOException e) {
            throw new RuntimeException("Error al procesar el archivo PDF de la licencia: " + e.getMessage(), e);
        }
    }

    // Lista todas las personas registradas en la base de datos
    @Override
    @Transactional(readOnly = true)
    public List<Persona> listarTodas() {
        List<Persona> lista = personaRepository.findAll();
        lista.forEach(this::adjuntarUsuarioSiEsAdministrativo);
        return lista;
    }

    // Busca una persona por su ID primario o lanza excepción si no existe
    @Override
    @Transactional(readOnly = true)
    public Persona obtenerPorId(Long id) {
        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada con ID: " + id));
        adjuntarUsuarioSiEsAdministrativo(persona);
        return persona;
    }

    // Filtra y retorna las personas según su tipo ('C' Conductor o 'A' Administrativo)
    @Override
    @Transactional(readOnly = true)
    public List<Persona> listarPorTipo(String tipoPersona) {
        List<Persona> lista = personaRepository.findByTipoPersona(tipoPersona);
        lista.forEach(this::adjuntarUsuarioSiEsAdministrativo);
        return lista;
    }

    // Consulta pública 8.5: Total de personas agrupadas por tipo ('C' y 'A')
    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> obtenerTotalPersonasPorTipo() {
        List<Object[]> resultados = personaRepository.countPersonasByTipo();
        Map<String, Long> respuesta = new HashMap<>();
        respuesta.put("A", 0L);
        respuesta.put("C", 0L);
        for (Object[] fila : resultados) {
            String tipo = fila[0] != null ? fila[0].toString() : "Desconocido";
            Long total = fila[1] != null ? ((Number) fila[1]).longValue() : 0L;
            respuesta.put(tipo, total);
        }
        return respuesta;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerResumenPersonas() {
        long totalAdministrativos = personaRepository.findByTipoPersona("A").size();
        long totalConductores = personaRepository.findByTipoPersona("C").size();
        long totalGeneral = personaRepository.count();

        Map<String, Object> resumen = new HashMap<>();
        resumen.put("totalRegistrados", totalGeneral);
        resumen.put("administrativos", totalAdministrativos);
        resumen.put("conductores", totalConductores);
        return resumen;
    }
}
