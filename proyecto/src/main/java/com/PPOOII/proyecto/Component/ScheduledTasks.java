// Paquete de componentes programados de la aplicación según arquitectura del Laboratorio 3
package com.PPOOII.proyecto.Component;

// Importación del cliente Geocoder de Google Maps (Laboratorio 3)
import com.PPOOII.proyecto.APIs.GoogleMaps.Geocoder;
// Importación de la entidad Persona
import com.PPOOII.proyecto.entities.Persona;
// Importación de la entidad Trayecto
import com.PPOOII.proyecto.entities.Trayecto;
// Importación de la entidad VehiculoDocumento
import com.PPOOII.proyecto.entities.VehiculoDocumento;
// Importación de la entidad VehiculoPersona
import com.PPOOII.proyecto.entities.VehiculoPersona;
// Importación del repositorio JPA de Persona
import com.PPOOII.proyecto.repository.PersonaRepository;
// Importación del repositorio JPA de Trayecto
import com.PPOOII.proyecto.repository.TrayectoRepository;
// Importación del repositorio JPA de VehiculoDocumento
import com.PPOOII.proyecto.repository.VehiculoDocumentoRepository;
// Importación del repositorio JPA de VehiculoPersona
import com.PPOOII.proyecto.repository.VehiculoPersonaRepository;
// Importación de Logger de SLF4J (Laboratorio 3, pág. 10)
import org.slf4j.Logger;
// Importación de LoggerFactory de SLF4J (Laboratorio 3, pág. 10)
import org.slf4j.LoggerFactory;
// Importación de @Autowired para inyección de dependencias
import org.springframework.beans.factory.annotation.Autowired;
// Importación de @Scheduled para programación de tareas automáticas con expresiones cron y fixedRate (Laboratorio 3)
import org.springframework.scheduling.annotation.Scheduled;
// Importación de @Component de Spring (Laboratorio 3)
import org.springframework.stereotype.Component;
// Importación de @Transactional para persistencia atómica
import org.springframework.transaction.annotation.Transactional;

// Importación de LocalDate para comparación de fechas de vigencia
import java.time.LocalDate;
// Importación de LocalDateTime para estampas de tiempo en el log (Laboratorio 3)
import java.time.LocalDateTime;
// Importación de DateTimeFormatter para formatear la hora de ejecución (Laboratorio 3, pág. 10)
import java.time.format.DateTimeFormatter;
// Importación de la interfaz List de Java
import java.util.List;

// Componente que orquesta las tareas programadas periódicas en segundo plano según el Laboratorio 3 (págs. 10 y 15)
@Component
public class ScheduledTasks {

    // Logger para registrar la ejecución de las tareas programadas en consola (Laboratorio 3, pág. 10)
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    // Formateador de fecha y hora para los mensajes de trazabilidad (Laboratorio 3, pág. 10)
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Inyección del cliente Geocoder de Google Maps
    @Autowired
    private Geocoder geocoder;

    // Inyección del repositorio de Personas
    @Autowired
    private PersonaRepository personaRepository;

    // Inyección del repositorio de relaciones Vehículo-Persona
    @Autowired
    private VehiculoPersonaRepository vehiculoPersonaRepository;

    // Inyección del repositorio de documentos asociados a vehículos
    @Autowired
    private VehiculoDocumentoRepository vehiculoDocumentoRepository;

    // Inyección del repositorio de trayectos
    @Autowired
    private TrayectoRepository trayectoRepository;

    // -----------------------------------------------------------------------------------------
    // TAREA 1: Verificación de vigencia de licencias de conductores (Cada 2 minutos = 120.000 ms)
    // Cambia el estado del conductor en la relación con el vehículo a 'RO' (Restringido para Operar)
    // -----------------------------------------------------------------------------------------
    @Scheduled(fixedRate = 120000)
    @Transactional
    public void verificarVigenciaLicenciasConductores() {
        // Registra el inicio de ejecución de la tarea programada con el formato del Laboratorio 3
        logger.info("Cron Task [Vigencia Licencias] :: Execution Time - {}", dateTimeFormatter.format(LocalDateTime.now()));

        try {
            // Consultar conductores cuya licencia esté vencida frente a la fecha actual
            List<Persona> conductoresVencidos = personaRepository.findConductoresConLicenciaVencida();

            // Itera sobre cada conductor con licencia vencida encontrado
            for (Persona conductor : conductoresVencidos) {
                // Obtener todas las relaciones del conductor con vehículos
                List<VehiculoPersona> relaciones = vehiculoPersonaRepository.findByPersonaId(conductor.getId());

                // Recorre cada relación vehículo-conductor para restringir su operación
                for (VehiculoPersona rel : relaciones) {
                    // Si el conductor no está ya en RO, actualizar su estado a RO
                    if (!"RO".equalsIgnoreCase(rel.getEstadoConductor())) {
                        // Cambia el estado operativo a 'RO' (Restringido para Operar)
                        rel.setEstadoConductor("RO");
                        // Persiste el cambio en la base de datos inmediatamente
                        vehiculoPersonaRepository.saveAndFlush(rel);
                        // Registra alerta detallada en el log
                        logger.warn("[ALERTA LICENCIA VENCIDA] Conductor '{} {}' (ID: {}) con licencia vencida el {}. Asignación ID: {} cambiada a 'RO'.",
                                conductor.getNombres(), conductor.getApellidos(), conductor.getId(),
                                conductor.getFechaVigenciaLicencia(), rel.getId());

                        // Notificación aislada en try/catch independiente para jamás afectar la transacción de BD
                        try {
                            enviarNotificacionRestriccionConductor(conductor, rel);
                        } catch (Exception mailEx) {
                            logger.warn("[NOTIFICACION CORREO] No se pudo enviar correo a {}: {}", conductor.getCorreo(), mailEx.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Registra cualquier excepción ocurrida durante la tarea sin detener el planificador
            logger.error("[ERROR EN TAREA LICENCIAS] Ocurrió una excepción: {}", e.getMessage());
        }
    }

    // Método auxiliar aislado para trazabilidad de notificación al conductor restringido
    private void enviarNotificacionRestriccionConductor(Persona conductor, VehiculoPersona rel) {
        logger.info("[NOTIFICACION ENVIADA] Destinatario: {} | Asunto: Restricción Operativa (RO) | Vehículo ID: {} | Motivo: Licencia vencida el {}",
                conductor.getCorreo(), rel.getVehiculo().getId(), conductor.getFechaVigenciaLicencia());
    }

    // -----------------------------------------------------------------------------------------
    // TAREA 2: Verificación de vigencia de documentos asociados al vehículo (Cada 2 minutos = 120.000 ms)
    // Cambia el estado del documento asociado al vehículo a 'Vencido'
    // -----------------------------------------------------------------------------------------
    @Scheduled(fixedRate = 120000)
    @Transactional
    public void verificarVigenciaDocumentosVehiculos() {
        // Registra el inicio de ejecución de la tarea programada con el formato del Laboratorio 3
        logger.info("Cron Task [Vigencia Documentos Vehiculares] :: Execution Time - {}", dateTimeFormatter.format(LocalDateTime.now()));

        try {
            // Obtiene la fecha actual del sistema
            LocalDate hoy = LocalDate.now();
            // Recupera todos los documentos asociados a vehículos
            List<VehiculoDocumento> todosLosDocumentos = vehiculoDocumentoRepository.findAll();

            // Recorre cada documento para verificar su fecha de vencimiento
            for (VehiculoDocumento doc : todosLosDocumentos) {
                // Si la fecha de vencimiento es anterior a hoy y el estado no es ya 'Vencido'
                if (doc.getFechaVencimiento() != null && doc.getFechaVencimiento().isBefore(hoy)) {
                    // Comprueba si el estado actual es distinto de 'Vencido'
                    if (!"Vencido".equalsIgnoreCase(doc.getEstado())) {
                        // Actualiza el estado del documento a 'Vencido'
                        doc.setEstado("Vencido");
                        // Guarda el cambio en la base de datos inmediatamente
                        vehiculoDocumentoRepository.saveAndFlush(doc);
                        // Registra alerta detallada en el log
                        logger.warn("[ALERTA DOCUMENTO VENCIDO] Documento ID: {} del vehículo ID: {} venció el {}. Estado actualizado a 'Vencido'.",
                                doc.getId(), doc.getVehiculo().getId(), doc.getFechaVencimiento());
                    }
                }
            }
        } catch (Exception e) {
            // Registra cualquier excepción ocurrida durante la tarea
            logger.error("[ERROR EN TAREA DOCUMENTOS] Ocurrió una excepción: {}", e.getMessage());
        }
    }

    // -----------------------------------------------------------------------------------------
    // TAREA 3: Geocodificación de trayectos pendientes vía Google Maps API (Cada 90 segundos = 90.000 ms)
    // Extrae coordenadas (latitud y longitud) de la ubicación y las persiste en la base de datos
    // -----------------------------------------------------------------------------------------
    @Scheduled(fixedRate = 90000)
    @Transactional
    public void geocodificarTrayectosPendientes() {
        // Registra el inicio de ejecución de la tarea de geocodificación
        logger.info("Cron Task [Geocodificación Google Maps] :: Execution Time - {}", dateTimeFormatter.format(LocalDateTime.now()));

        try {
            // Consultar trayectos con coordenadas nulas o iguales a 0
            List<Trayecto> pendientes = trayectoRepository.findTrayectosSinCoordenadas();

            // Si no existen trayectos pendientes por geocodificar, finaliza la ejecución
            if (pendientes.isEmpty()) {
                // Registra mensaje informativo en consola
                logger.info("[CRON GEOCODIFICACION] Todos los trayectos se encuentran debidamente geocodificados.");
                // Termina el método
                return;
            }

            // Recorre cada trayecto pendiente para consultar sus coordenadas en Google Maps
            for (Trayecto trayecto : pendientes) {
                // Registra la parada que se va a geocodificar
                logger.info("Geocodificando parada: '{}' (Trayecto ID: {})", trayecto.getUbicacion(), trayecto.getId());
                // Invoca el componente Geocoder
                double[] coordenadas = geocoder.geocodificarDireccion(trayecto.getUbicacion());

                // Si se obtuvieron coordenadas válidas, actualiza la entidad Trayecto
                if (coordenadas != null && coordenadas.length == 2) {
                    // Asigna la latitud obtenida
                    trayecto.setLatitud(coordenadas[0]);
                    // Asigna la longitud obtenida
                    trayecto.setLongitud(coordenadas[1]);
                    // Guarda el trayecto actualizado en la base de datos inmediatamente
                    trayectoRepository.saveAndFlush(trayecto);
                    // Registra las coordenadas asignadas en el log
                    logger.info("Coordenadas asignadas al Trayecto ID {}: Lat {}, Lng {}", trayecto.getId(), coordenadas[0], coordenadas[1]);
                }
            }
        } catch (Exception e) {
            // Registra cualquier excepción ocurrida durante la geocodificación
            logger.error("[ERROR EN TAREA GEOCODIFICACION] Ocurrió una excepción: {}", e.getMessage());
        }
    }
}
