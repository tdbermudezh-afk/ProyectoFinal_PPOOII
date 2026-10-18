package com.PPOOII.proyecto.services; // Define el paquete de servicios del proyecto

import com.PPOOII.proyecto.entities.Persona; // Importa la entidad Persona
import com.PPOOII.proyecto.entities.Usuario; // Importa la entidad Usuario
import com.PPOOII.proyecto.entities.UsuarioPK; // Importa la llave primaria compuesta UsuarioPK
import com.PPOOII.proyecto.repository.PersonaRepository; // Importa el repositorio de Persona
import com.PPOOII.proyecto.repository.UsuarioRepository; // Importa el repositorio de Usuario
import org.springframework.beans.factory.annotation.Autowired; // Importa la anotación para inyección de dependencias
import org.springframework.stereotype.Service; // Importa la anotación que marca la clase como un servicio de Spring
import org.springframework.transaction.annotation.Transactional; // Importa la anotación para manejo transaccional

import java.util.List; // Importa la interfaz List
import java.util.UUID; // Importa la clase UUID para generación de contraseñas y APIKeys

@Service // Registra la clase como un componente de servicio de Spring
public class PersonaService { // Declaración de la clase PersonaService

    @Autowired // Inyecta automáticamente el repositorio de personas
    private PersonaRepository personaRepository; // Instancia de PersonaRepository

    @Autowired // Inyecta automáticamente el repositorio de usuarios
    private UsuarioRepository usuarioRepository; // Instancia de UsuarioRepository

    @Transactional // Ejecuta la creación dentro de una transacción de base de datos
    public Persona crearPersona(Persona persona) { // Método para registrar una nueva persona en el sistema
        if (personaRepository.existsByIdentificacion(persona.getIdentificacion())) { // Verifica si ya existe una persona con esa identificación
            throw new IllegalArgumentException("Ya existe una persona registrada con la identificación: " + persona.getIdentificacion()); // Lanza error 400 si está duplicada
        } // Fin de validación de identificación única

        if (personaRepository.existsByCorreo(persona.getCorreo())) { // Verifica si ya existe una persona con ese correo electrónico
            throw new IllegalArgumentException("Ya existe una persona registrada con el correo: " + persona.getCorreo()); // Lanza error 400 si el correo está duplicado
        } // Fin de validación de correo único

        // Si la persona es de tipo CONDUCTOR ('C'), exigir el documento PDF de licencia en Base64 y su fecha de vigencia (Entrega 3 - BD 2.1)
        if ("C".equalsIgnoreCase(persona.getTipoPersona())) { // Comprueba si el rol de la persona es Conductor ('C')
            if (persona.getLicenciaConduccion() == null || persona.getLicenciaConduccion().isBlank()) { // Verifica si no se envió el archivo de licencia en Base64
                throw new IllegalArgumentException("Para una persona de tipo CONDUCTOR ('C'), el documento de licencia de conducción en formato Base64 ('licenciaConduccion') es obligatorio."); // Lanza error 400 indicando que falta el documento
            } // Fin de validación de licencia en Base64
            if (persona.getFechaVigenciaLicencia() == null) { // Verifica si no se envió la fecha de vigencia de la licencia
                throw new IllegalArgumentException("Para una persona de tipo CONDUCTOR ('C'), la fecha de vigencia de la licencia ('fechaVigenciaLicencia') es obligatoria."); // Lanza error 400 indicando que falta la fecha de vigencia
            } // Fin de validación de fecha de vigencia
        } else if ("A".equalsIgnoreCase(persona.getTipoPersona())) { // Si la persona es de tipo ADMINISTRATIVO ('A')
            persona.setLicenciaConduccion(null); // Asegura que no almacene licencia de conducción ya que solo aplica a conductores
            persona.setFechaVigenciaLicencia(null); // Asegura que no almacene fecha de vigencia de licencia
        } // Fin de validación según tipo de persona

        Persona personaGuardada = personaRepository.save(persona); // Guarda la entidad Persona en la base de datos

        // Regla: Si la persona es de tipo ADMINISTRATIVO ('A'), se genera automáticamente su Usuario
        if ("A".equalsIgnoreCase(persona.getTipoPersona())) { // Verifica si es Administrativo para autogenerar su cuenta de usuario
            Usuario usuario = generarUsuarioParaAdministrativo(personaGuardada); // Crea el usuario con login por nemotecnia, password y APIKey
            personaGuardada.setUsuarioGenerado(usuario); // Adjunta el usuario generado a la respuesta transitoria
        } // Fin de autogeneración de usuario

        return personaGuardada; // Retorna la persona creada
    } // Fin del método crearPersona

    private Usuario generarUsuarioParaAdministrativo(Persona persona) { // Método auxiliar para generar el Usuario de un Administrativo
        // Nemotecnia: Primera letra del nombre + primera letra del apellido + identificación
        String primeraLetraNombre = persona.getNombres().trim().substring(0, 1).toLowerCase(); // Extrae la primera letra del nombre en minúscula
        String primeraLetraApellido = persona.getApellidos().trim().substring(0, 1).toLowerCase(); // Extrae la primera letra del apellido en minúscula
        String loginNemotecnico = primeraLetraNombre + primeraLetraApellido + persona.getIdentificacion(); // Concatena la nemotecnia del login

        // Generación automática de contraseña y APIKey aleatoria
        String passwordGenerado = "Pwd" + UUID.randomUUID().toString().substring(0, 8); // Genera una contraseña aleatoria segura
        String apiKeyGenerada = UUID.randomUUID().toString(); // Genera un UUID único como APIKey

        UsuarioPK usuarioPK = new UsuarioPK(persona.getId(), loginNemotecnico); // Construye la llave primaria compuesta (idpersona, login)

        Usuario usuario = new Usuario(); // Instancia la entidad Usuario
        usuario.setId(usuarioPK); // Asigna la llave primaria compuesta
        usuario.setPersona(persona); // Asocia la referencia a la entidad Persona
        usuario.setPassword(passwordGenerado); // Asigna la contraseña generada automáticamente
        usuario.setApikey(apiKeyGenerada); // Asigna la APIKey generada automáticamente

        return usuarioRepository.save(usuario); // Persiste y retorna el nuevo Usuario en la base de datos
    } // Fin del método generarUsuarioParaAdministrativo

    @Transactional // Ejecuta la actualización dentro de una transacción de base de datos
    public Persona actualizarPersona(Long id, Persona detalles) { // Método para actualizar los datos de una persona existente
        Persona persona = obtenerPorId(id); // Busca la persona por su ID o lanza excepción si no existe

        if (detalles.getIdentificacion() != null && !detalles.getIdentificacion().equals(persona.getIdentificacion())) { // Si cambió el número de identificación
            if (personaRepository.existsByIdentificacion(detalles.getIdentificacion())) { // Verifica que la nueva identificación no pertenezca a otra persona
                throw new IllegalArgumentException("Ya existe una persona registrada con la identificación: " + detalles.getIdentificacion()); // Lanza error 400 si ya existe
            } // Fin de verificación de duplicado
            persona.setIdentificacion(detalles.getIdentificacion()); // Actualiza la identificación
        } // Fin de actualización de identificación

        if (detalles.getCorreo() != null && !detalles.getCorreo().equalsIgnoreCase(persona.getCorreo())) { // Si cambió el correo electrónico
            if (personaRepository.existsByCorreo(detalles.getCorreo())) { // Verifica que el nuevo correo no esté registrado por otra persona
                throw new IllegalArgumentException("Ya existe una persona registrada con el correo: " + detalles.getCorreo()); // Lanza error 400 si ya existe
            } // Fin de verificación de duplicado
            persona.setCorreo(detalles.getCorreo()); // Actualiza el correo electrónico
        } // Fin de actualización de correo

        if (detalles.getNombres() != null) persona.setNombres(detalles.getNombres()); // Actualiza los nombres si fueron enviados
        if (detalles.getApellidos() != null) persona.setApellidos(detalles.getApellidos()); // Actualiza los apellidos si fueron enviados
        if (detalles.getTipoIdentificacion() != null) persona.setTipoIdentificacion(detalles.getTipoIdentificacion()); // Actualiza el tipo de identificación si fue enviado

        // Actualizar campos de licencia de conducción si fueron enviados en el cuerpo JSON (Entrega 3 - BD 2.1)
        if (detalles.getLicenciaConduccion() != null) { // Si se envió el campo licenciaConduccion en el JSON
            if (detalles.getLicenciaConduccion().isBlank()) { // Si se envió vacío en blanco ("")
                throw new IllegalArgumentException("El documento de licencia de conducción en formato Base64 ('licenciaConduccion') no puede estar vacío."); // Lanza error 400
            } // Fin de validación de cadena vacía
            persona.setLicenciaConduccion(detalles.getLicenciaConduccion().trim()); // Actualiza el soporte de licencia en Base64
        } // Fin de actualización de licenciaConduccion
        if (detalles.getFechaVigenciaLicencia() != null) { // Si se envió la fecha de vigencia de la licencia
            persona.setFechaVigenciaLicencia(detalles.getFechaVigenciaLicencia()); // Actualiza la fecha de vigencia de la licencia
        } // Fin de actualización de fechaVigenciaLicencia

        // Si cambia a administrativo, generar usuario si aún no tiene
        if ("A".equalsIgnoreCase(detalles.getTipoPersona()) && !"A".equalsIgnoreCase(persona.getTipoPersona())) { // Si pasa de Conductor a Administrativo
            persona.setTipoPersona("A"); // Cambia el tipo de persona a Administrativo ('A')
            persona.setLicenciaConduccion(null); // Limpia el campo de licencia porque solo aplica a Conductores
            persona.setFechaVigenciaLicencia(null); // Limpia la fecha de vigencia de licencia
            Persona personaActualizada = personaRepository.save(persona); // Guarda los cambios de la persona
            Usuario usuario = generarUsuarioParaAdministrativo(personaActualizada); // Genera su nuevo usuario administrativo
            personaActualizada.setUsuarioGenerado(usuario); // Adjunta el usuario generado
            return personaActualizada; // Retorna la persona actualizada
        } else if (detalles.getTipoPersona() != null) { // Si se mantiene el mismo tipo o se actualiza a Conductor ('C')
            persona.setTipoPersona(detalles.getTipoPersona()); // Asigna el tipo de persona
        } // Fin de manejo de cambio de tipoPersona

        // Validar que si la persona queda como CONDUCTOR ('C'), tenga su documento de licencia en Base64
        if ("C".equalsIgnoreCase(persona.getTipoPersona())) { // Verifica si la persona es Conductor ('C')
            if (persona.getLicenciaConduccion() == null || persona.getLicenciaConduccion().isBlank()) { // Verifica que tenga su licencia en Base64 registrada
                throw new IllegalArgumentException("Para una persona de tipo CONDUCTOR ('C'), el documento de licencia de conducción en formato Base64 ('licenciaConduccion') es obligatorio."); // Lanza error 400 si falta el documento
            } // Fin de validación de licencia obligatoria
        } // Fin de verificación para Conductor

        return personaRepository.save(persona); // Guarda y retorna la entidad Persona actualizada
    } // Fin del método actualizarPersona

    public List<Persona> listarTodas() { // Método para listar todas las personas registradas
        return personaRepository.findAll(); // Retorna todos los registros de la tabla personas
    } // Fin del método listarTodas

    public Persona obtenerPorId(Long id) { // Método para buscar una persona por su clave primaria
        return personaRepository.findById(id) // Consulta la persona en base de datos por ID
                .orElseThrow(() -> new RuntimeException("Persona no encontrada con ID: " + id)); // Lanza excepción si no existe
    } // Fin del método obtenerPorId

    public List<Persona> listarPorTipo(String tipoPersona) { // Método para listar personas filtradas por su tipo ('A' o 'C')
        return personaRepository.findByTipoPersona(tipoPersona); // Retorna la lista filtrada desde el repositorio
    } // Fin del método listarPorTipo
} // Fin de la clase PersonaService