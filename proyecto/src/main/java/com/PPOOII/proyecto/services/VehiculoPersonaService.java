// Paquete de servicios de lógica de negocio de la aplicación
package com.PPOOII.proyecto.services;

// Importación de la entidad Persona
import com.PPOOII.proyecto.entities.Persona;
// Importación de la entidad Vehiculo
import com.PPOOII.proyecto.entities.Vehiculo;
// Importación de la entidad de relación VehiculoPersona
import com.PPOOII.proyecto.entities.VehiculoPersona;
// Importación de excepción personalizada ResourceNotFoundException
import com.PPOOII.proyecto.exceptions.ResourceNotFoundException;
// Importación del repositorio JPA de Persona
import com.PPOOII.proyecto.repository.PersonaRepository;
// Importación del repositorio JPA de VehiculoPersona
import com.PPOOII.proyecto.repository.VehiculoPersonaRepository;
// Importación del repositorio JPA de Vehiculo
import com.PPOOII.proyecto.repository.VehiculoRepository;
// Importación de la interfaz de servicio IVehiculoPersonaService (Laboratorio 1)
import com.PPOOII.proyecto.services.Interfaces.IVehiculoPersonaService;
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

// Importación de la clase LocalDate para registrar la fecha de asociación
import java.time.LocalDate;
// Importación de la interfaz List de Java
import java.util.List;

// Servicio que implementa IVehiculoPersonaService para gestionar la relación entre Conductores y Vehículos
@Service("VehiculoPersonaService")
public class VehiculoPersonaService implements IVehiculoPersonaService {

    // Instancia del logger de auditoría según el estándar del Laboratorio 1
    private static final Logger logger = LogManager.getLogger(VehiculoPersonaService.class);

    // Inyección del repositorio de relación Vehículo-Persona
    @Autowired
    private VehiculoPersonaRepository vehiculoPersonaRepository;

    // Inyección del repositorio de Personas
    @Autowired
    private PersonaRepository personaRepository;

    // Inyección del repositorio de Vehículos
    @Autowired
    private VehiculoRepository vehiculoRepository;

    // Asocia una persona de tipo Conductor ('C') a un vehículo con un estado inicial
    @Override
    @Transactional
    public VehiculoPersona asociarConductorAVehiculo(Long personaId, Long vehiculoId, String estadoInicial) {
        // Busca la persona por su ID o lanza excepción si no existe
        Persona persona = personaRepository.findById(personaId)
                // Mensaje de error cuando la persona no existe
                .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada con ID: " + personaId));

        // Regla de negocio: Solo las personas de tipo CONDUCTOR ('C') pueden asociarse a vehículos para operación
        if (!"C".equalsIgnoreCase(persona.getTipoPersona())) {
            // Lanza excepción si la persona es Administrativa ('A') u otro tipo
            throw new IllegalArgumentException("La persona seleccionada no es de tipo CONDUCTOR");
        }

        // Convierte el ID del vehículo a entero para consultar el repositorio
        int vId = Integer.parseInt(vehiculoId.toString());
        // Busca el vehículo por su ID o lanza excepción si no existe
        Vehiculo vehiculo = vehiculoRepository.findById(vId)
                // Mensaje de error cuando el vehículo no existe
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + vehiculoId));

        String estadoEfectivo = (estadoInicial != null && !estadoInicial.isBlank()) ? estadoInicial.trim().toUpperCase() : "EA";
        if (!estadoEfectivo.matches("^(PO|EA|RO)$")) {
            throw new IllegalArgumentException("Estado inválido. Los estados permitidos son: PO (Puede Operar), EA (Espera de Aprobación), RO (Restringido para Operar).");
        }

        // Crea la nueva instancia de relación VehiculoPersona
        VehiculoPersona vp = new VehiculoPersona();
        // Asigna la persona conductora
        vp.setPersona(persona);
        // Asigna el vehículo
        vp.setVehiculo(vehiculo);
        // Asigna la fecha actual como fecha de asociación
        vp.setFechaAsociacion(LocalDate.now());
        // Asigna el estado inicial o 'EA' (Espera de Aprobación) por defecto
        vp.setEstadoConductor(estadoEfectivo);

        // Registra en el log la asociación realizada
        logger.info("Asociando conductor ID: {} al vehículo ID: {} con estado: {}", personaId, vehiculoId, vp.getEstadoConductor());
        // Persiste y retorna la relación creada
        return vehiculoPersonaRepository.save(vp);
    }

    // Cambia el estado operativo del conductor respecto al vehículo ('PO', 'EA' o 'RO')
    @Override
    @Transactional
    public VehiculoPersona cambiarEstadoConductor(Long idRelacion, String nuevoEstado) {
        // Valida que el nuevo estado corresponda a uno de los valores permitidos
        if (nuevoEstado == null || !nuevoEstado.trim().matches("^(PO|EA|RO)$")) {
            // Lanza excepción si el código de estado es inválido
            throw new IllegalArgumentException("Estado inválido. Los estados permitidos son: PO, EA, RO");
        }

        // Busca la relación existente por su ID o lanza excepción si no existe
        VehiculoPersona vp = vehiculoPersonaRepository.findById(idRelacion)
                // Mensaje de error cuando la relación no existe
                .orElseThrow(() -> new ResourceNotFoundException("Relación Vehículo-Persona no encontrada con ID: " + idRelacion));

        // Actualiza el estado del conductor
        vp.setEstadoConductor(nuevoEstado.trim());
        // Registra en el log el cambio de estado
        logger.info("Cambiando estado de la relación ID: {} a '{}'", idRelacion, nuevoEstado);
        // Guarda y retorna la relación actualizada
        return vehiculoPersonaRepository.save(vp);
    }

    // Lista todas las asociaciones de vehículos correspondientes a una persona
    @Override
    @Transactional(readOnly = true)
    public List<VehiculoPersona> listarPorPersona(Long personaId) {
        // Consulta y retorna las relaciones filtradas por ID de persona
        return vehiculoPersonaRepository.findByPersonaId(personaId);
    }

    // Lista todas las asociaciones conductor-vehículo existentes en el sistema
    @Override
    @Transactional(readOnly = true)
    public List<VehiculoPersona> listarTodas() {
        return vehiculoPersonaRepository.findAll();
    }

    // Consulta pública 8.2: Retorna todos los conductores cuyo estado sea 'PO' (Puede Operar)
    @Override
    @Transactional(readOnly = true)
    public List<Persona> obtenerConductoresQuePuedenOperar() {
        return vehiculoPersonaRepository.findConductoresQuePuedenOperar();
    }
}
