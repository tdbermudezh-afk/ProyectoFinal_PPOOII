// Paquete de componentes de la aplicación según arquitectura del Laboratorio 3
package com.PPOOII.proyecto.Component;

// Importaciones de clientes externos y entidades
import com.PPOOII.proyecto.APIs.GoogleMaps.Geocoder;
import com.PPOOII.proyecto.entities.Persona;
import com.PPOOII.proyecto.entities.Trayecto;
import com.PPOOII.proyecto.entities.VehiculoDocumento;
import com.PPOOII.proyecto.entities.VehiculoPersona;
import com.PPOOII.proyecto.repository.PersonaRepository;
import com.PPOOII.proyecto.repository.TrayectoRepository;
import com.PPOOII.proyecto.repository.VehiculoDocumentoRepository;
import com.PPOOII.proyecto.repository.VehiculoPersonaRepository;

// Importaciones de Spring y logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Importaciones de utilidades de fecha y listas
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Componente que orquesta las tareas programadas periódicas en segundo plano
@Component
public class ScheduledTasks {

    // Logger para registrar la ejecución de las tareas en consola
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    // Formateador de fecha y hora para mensajes de trazabilidad
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Inyección del cliente Geocoder de Google Maps
    @Autowired
    private Geocoder geocoder;

    // Inyección de repositorios
    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private VehiculoPersonaRepository vehiculoPersonaRepository;

    @Autowired
    private VehiculoDocumentoRepository vehiculoDocumentoRepository;

    @Autowired
    private TrayectoRepository trayectoRepository;

    // -----------------------------------------------------------------------------------------
    // TAREA 1: Verificación de vigencia de licencias de conductores (Cada 2 minutos = 120.000 ms)
    // Cambia el estado del conductor en la relación con el vehículo a 'RO' (Restringido para Operar)
    // -----------------------------------------------------------------------------------------
    @Scheduled(fixedRate = 120000)
    @Transactional
    public void verificarVigenciaLicenciasConductores() {
        logger.info("[CRON LICENCIAS - {}] Iniciando verificación de vigencia de licencias de conducción...", formatter.format(LocalDateTime.now()));

        try {
            // Consultar conductores cuya licencia esté vencida frente a la fecha actual
            List<Persona> conductoresVencidos = personaRepository.findConductoresConLicenciaVencida();

            for (Persona conductor : conductoresVencidos) {
                // Obtener todas las relaciones del conductor con vehículos
                List<VehiculoPersona> relaciones = vehiculoPersonaRepository.findByPersonaId(conductor.getId());

                for (VehiculoPersona rel : relaciones) {
                    // Si el conductor no está ya en RO, actualizar su estado a RO
                    if (!"RO".equalsIgnoreCase(rel.getEstadoConductor())) {
                        rel.setEstadoConductor("RO");
                        vehiculoPersonaRepository.save(rel);
                        logger.warn("[ALERTA LICENCIA VENCIDA] Conductor '{} {}' (ID: {}) con licencia vencida el {}. Asignación ID: {} cambiada a 'RO'.",
                                conductor.getNombres(), conductor.getApellidos(), conductor.getId(),
                                conductor.getFechaVigenciaLicencia(), rel.getId());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("[ERROR EN TAREA LICENCIAS] Ocurrió una excepción: {}", e.getMessage());
        }
    }

    // -----------------------------------------------------------------------------------------
    // TAREA 2: Verificación de vigencia de documentos asociados al vehículo (Cada 2 minutos = 120.000 ms)
    // Cambia el estado del documento asociado al vehículo a 'Vencido'
    // -----------------------------------------------------------------------------------------
    @Scheduled(fixedRate = 120000)
    @Transactional
    public void verificarVigenciaDocumentosVehiculos() {
        logger.info("[CRON DOCUMENTOS - {}] Iniciando verificación de vigencia de documentos vehiculares...", formatter.format(LocalDateTime.now()));

        try {
            LocalDate hoy = LocalDate.now();
            List<VehiculoDocumento> todosLosDocumentos = vehiculoDocumentoRepository.findAll();

            for (VehiculoDocumento doc : todosLosDocumentos) {
                // Si la fecha de vencimiento es anterior a hoy y el estado no es ya 'Vencido'
                if (doc.getFechaVencimiento() != null && doc.getFechaVencimiento().isBefore(hoy)) {
                    if (!"Vencido".equalsIgnoreCase(doc.getEstado())) {
                        doc.setEstado("Vencido");
                        vehiculoDocumentoRepository.save(doc);
                        logger.warn("[ALERTA DOCUMENTO VENCIDO] Documento ID: {} del vehículo ID: {} venció el {}. Estado actualizado a 'Vencido'.",
                                doc.getId(), doc.getVehiculo().getId(), doc.getFechaVencimiento());
                    }
                }
            }
        } catch (Exception e) {
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
        logger.info("[CRON GEOCODIFICACION - {}] Buscando trayectos sin coordenadas geográficas...", formatter.format(LocalDateTime.now()));

        try {
            // Consultar trayectos con coordenadas nulas o 0
            List<Trayecto> pendientes = trayectoRepository.findTrayectosSinCoordenadas();

            if (pendientes.isEmpty()) {
                logger.info("[CRON GEOCODIFICACION] Todos los trayectos se encuentran debidamente geocodificados.");
                return;
            }

            for (Trayecto trayecto : pendientes) {
                logger.info("Geocodificando parada: '{}' (Trayecto ID: {})", trayecto.getUbicacion(), trayecto.getId());
                double[] coordenadas = geocoder.geocodificarDireccion(trayecto.getUbicacion());

                if (coordenadas != null && coordenadas.length == 2) {
                    trayecto.setLatitud(coordenadas[0]);
                    trayecto.setLongitud(coordenadas[1]);
                    trayectoRepository.save(trayecto);
                    logger.info("Coordenadas asignadas al Trayecto ID {}: Lat {}, Lng {}", trayecto.getId(), coordenadas[0], coordenadas[1]);
                }
            }
        } catch (Exception e) {
            logger.error("[ERROR EN TAREA GEOCODIFICACION] Ocurrió una excepción: {}", e.getMessage());
        }
    }
}

