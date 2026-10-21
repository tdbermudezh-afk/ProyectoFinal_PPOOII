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
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

// Componente que orquesta las tareas programadas periódicas en segundo plano según el Laboratorio 3 (págs. 10 y 15)
@Component
public class ScheduledTasks {

    // Logger para registrar la ejecución de las tareas programadas en consola (Laboratorio 3, pág. 10)
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    // Formateador de fecha y hora para los mensajes de trazabilidad (Laboratorio 3, pág. 10)
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter fullIsoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

    // Estructuras en memoria para auditoría y visualización en tiempo real desde el Frontend
    private final Deque<Map<String, Object>> historialEjecuciones = new ConcurrentLinkedDeque<>();
    private static final int MAX_HISTORIAL = 40;

    private String ultimaEjecucionLicencias = "Aún no ejecutada";
    private String ultimaEjecucionDocumentos = "Aún no ejecutada";
    private String ultimaEjecucionGeocodificacion = "Aún no ejecutada";

    private int totalLicenciasRestringidas = 0;
    private int totalDocumentosVencidosActualizados = 0;
    private int totalTrayectosGeocodificados = 0;

    private void registrarEventoHistorial(String tarea, String tipoEjecucion, int analizados, int modificados, String detalle) {
        Map<String, Object> evento = new LinkedHashMap<>();
        evento.put("timestamp", LocalDateTime.now().format(fullIsoFormatter));
        evento.put("tarea", tarea);
        evento.put("tipo", tipoEjecucion); // AUTOMATICA o MANUAL
        evento.put("analizados", analizados);
        evento.put("modificados", modificados);
        evento.put("detalle", detalle);

        historialEjecuciones.addFirst(evento);
        while (historialEjecuciones.size() > MAX_HISTORIAL) {
            historialEjecuciones.removeLast();
        }
    }

    // -----------------------------------------------------------------------------------------
    // TAREA 1: Verificación de vigencia de licencias de conductores (Cada 2 minutos = 120.000 ms, inicio a los 5s)
    // Cambia el estado del conductor en la relación con el vehículo a 'RO' (Restringido para Operar)
    // -----------------------------------------------------------------------------------------
    @Scheduled(fixedRate = 120000, initialDelay = 5000)
    @Transactional
    public void verificarVigenciaLicenciasConductores() {
        ejecutarVerificarLicenciasInterno("AUTOMATICA");
    }

    @Transactional
    public Map<String, Object> ejecutarVerificarLicenciasManual() {
        return ejecutarVerificarLicenciasInterno("MANUAL");
    }

    private Map<String, Object> ejecutarVerificarLicenciasInterno(String origen) {
        String horaEjecucion = dateTimeFormatter.format(LocalDateTime.now());
        ultimaEjecucionLicencias = LocalDateTime.now().format(fullIsoFormatter);
        logger.info("Cron Task [Vigencia Licencias] ({}) :: Execution Time - {}", origen, horaEjecucion);

        int totalAnalizados = 0;
        int totalCambiados = 0;
        List<String> detallesCambios = new ArrayList<>();

        try {
            LocalDate hoy = LocalDate.now();
            List<Persona> conductoresVencidos = personaRepository.findConductoresConLicenciaVencida(hoy);
            totalAnalizados = conductoresVencidos.size();

            for (Persona conductor : conductoresVencidos) {
                List<VehiculoPersona> relaciones = vehiculoPersonaRepository.findByPersonaId(conductor.getId());
                for (VehiculoPersona rel : relaciones) {
                    if (!"RO".equalsIgnoreCase(rel.getEstadoConductor())) {
                        String anteriorEstado = rel.getEstadoConductor();
                        rel.setEstadoConductor("RO");
                        vehiculoPersonaRepository.saveAndFlush(rel);
                        totalCambiados++;
                        totalLicenciasRestringidas++;

                        String msg = String.format("Conductor '%s %s' (CC %s, vigencia: %s) cambió asignación vehículo #%s de '%s' a 'RO'",
                                conductor.getNombres(), conductor.getApellidos(), conductor.getIdentificacion(),
                                conductor.getFechaVigenciaLicencia(), rel.getVehiculo() != null ? rel.getVehiculo().getPlaca() : rel.getId(),
                                anteriorEstado);
                        detallesCambios.add(msg);
                        logger.warn("[ALERTA LICENCIA VENCIDA] {}", msg);

                        try {
                            enviarNotificacionRestriccionConductor(conductor, rel);
                        } catch (Exception mailEx) {
                            logger.warn("[NOTIFICACION CORREO] No se pudo enviar correo a {}: {}", conductor.getCorreo(), mailEx.getMessage());
                        }
                    }
                }
            }

            String resumen = totalCambiados > 0
                    ? ("Se restringieron " + totalCambiados + " asignación(es) a estado 'RO'.")
                    : ("Sin cambios requeridos: " + totalAnalizados + " conductor(es) con licencia vencida ya se encontraban en 'RO'.");

            registrarEventoHistorial("1. Vigencia de Licencias (-> RO)", origen, totalAnalizados, totalCambiados, resumen);

            Map<String, Object> resultado = new LinkedHashMap<>();
            resultado.put("tarea", "Vigencia Licencias");
            resultado.put("origen", origen);
            resultado.put("ejecutadoEn", ultimaEjecucionLicencias);
            resultado.put("conductoresVencidosDetectados", totalAnalizados);
            resultado.put("relacionesCambiadasARO", totalCambiados);
            resultado.put("detalles", detallesCambios);
            resultado.put("mensaje", resumen);
            return resultado;
        } catch (Exception e) {
            logger.error("[ERROR EN TAREA LICENCIAS] Ocurrió una excepción: {}", e.getMessage(), e);
            registrarEventoHistorial("1. Vigencia de Licencias (-> RO)", origen, totalAnalizados, 0, "Error: " + e.getMessage());
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", e.getMessage());
            return err;
        }
    }

    private void enviarNotificacionRestriccionConductor(Persona conductor, VehiculoPersona rel) {
        logger.info("[NOTIFICACION ENVIADA] Destinatario: {} | Asunto: Restricción Operativa (RO) | Vehículo ID: {} | Motivo: Licencia vencida el {}",
                conductor.getCorreo(), rel.getVehiculo() != null ? rel.getVehiculo().getId() : "N/A", conductor.getFechaVigenciaLicencia());
    }

    // -----------------------------------------------------------------------------------------
    // TAREA 2: Verificación de vigencia de documentos asociados al vehículo (Cada 2 minutos = 120.000 ms, inicio a los 5s)
    // Cambia el estado del documento asociado al vehículo a 'Vencido'
    // -----------------------------------------------------------------------------------------
    @Scheduled(fixedRate = 120000, initialDelay = 5000)
    @Transactional
    public void verificarVigenciaDocumentosVehiculos() {
        ejecutarVerificarDocumentosInterno("AUTOMATICA");
    }

    @Transactional
    public Map<String, Object> ejecutarVerificarDocumentosManual() {
        return ejecutarVerificarDocumentosInterno("MANUAL");
    }

    private Map<String, Object> ejecutarVerificarDocumentosInterno(String origen) {
        String horaEjecucion = dateTimeFormatter.format(LocalDateTime.now());
        ultimaEjecucionDocumentos = LocalDateTime.now().format(fullIsoFormatter);
        logger.info("Cron Task [Vigencia Documentos Vehiculares] ({}) :: Execution Time - {}", origen, horaEjecucion);

        int totalAnalizados = 0;
        int totalCambiados = 0;
        List<String> detallesCambios = new ArrayList<>();

        try {
            LocalDate hoy = LocalDate.now();
            List<VehiculoDocumento> todosLosDocumentos = vehiculoDocumentoRepository.findAll();
            totalAnalizados = todosLosDocumentos.size();

            for (VehiculoDocumento doc : todosLosDocumentos) {
                if (doc.getFechaVencimiento() != null && doc.getFechaVencimiento().isBefore(hoy)) {
                    if (!"Vencido".equalsIgnoreCase(doc.getEstado())) {
                        String anterior = doc.getEstado();
                        doc.setEstado("Vencido");
                        vehiculoDocumentoRepository.saveAndFlush(doc);
                        totalCambiados++;
                        totalDocumentosVencidosActualizados++;

                        String placa = (doc.getVehiculo() != null) ? doc.getVehiculo().getPlaca() : "ID " + doc.getVehiculoId();
                        String nomDoc = (doc.getDocumento() != null) ? doc.getDocumento().getNombre() : "Doc #" + doc.getId();
                        String msg = String.format("Documento '%s' (ID %d) del vehículo %s venció el %s. Cambió de '%s' a 'Vencido'.",
                                nomDoc, doc.getId(), placa, doc.getFechaVencimiento(), anterior);
                        detallesCambios.add(msg);
                        logger.warn("[ALERTA DOCUMENTO VENCIDO] {}", msg);
                    }
                }
            }

            String resumen = totalCambiados > 0
                    ? ("Se actualizaron " + totalCambiados + " documento(s) expirados al estado 'Vencido'.")
                    : ("Sin cambios requeridos: todos los documentos expirados (" + totalAnalizados + " analizados) ya se encuentran en estado 'Vencido'.");

            registrarEventoHistorial("2. Vigencia de Documentos (-> Vencido)", origen, totalAnalizados, totalCambiados, resumen);

            Map<String, Object> resultado = new LinkedHashMap<>();
            resultado.put("tarea", "Vigencia Documentos Vehiculares");
            resultado.put("origen", origen);
            resultado.put("ejecutadoEn", ultimaEjecucionDocumentos);
            resultado.put("documentosAnalizados", totalAnalizados);
            resultado.put("documentosActualizadosAVencido", totalCambiados);
            resultado.put("detalles", detallesCambios);
            resultado.put("mensaje", resumen);
            return resultado;
        } catch (Exception e) {
            logger.error("[ERROR EN TAREA DOCUMENTOS] Ocurrió una excepción: {}", e.getMessage(), e);
            registrarEventoHistorial("2. Vigencia de Documentos (-> Vencido)", origen, totalAnalizados, 0, "Error: " + e.getMessage());
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", e.getMessage());
            return err;
        }
    }

    // -----------------------------------------------------------------------------------------
    // TAREA 3: Geocodificación de trayectos pendientes vía Google Maps / Geocoder (Cada 90 segundos = 90.000 ms, inicio a los 5s)
    // Extrae coordenadas (latitud y longitud) de la ubicación y las persiste en la base de datos
    // -----------------------------------------------------------------------------------------
    @Scheduled(fixedRate = 90000, initialDelay = 5000)
    @Transactional
    public void geocodificarTrayectosPendientes() {
        ejecutarGeocodificacionInterno("AUTOMATICA");
    }

    @Transactional
    public Map<String, Object> ejecutarGeocodificacionManual() {
        return ejecutarGeocodificacionInterno("MANUAL");
    }

    private Map<String, Object> ejecutarGeocodificacionInterno(String origen) {
        String horaEjecucion = dateTimeFormatter.format(LocalDateTime.now());
        ultimaEjecucionGeocodificacion = LocalDateTime.now().format(fullIsoFormatter);
        logger.info("Cron Task [Geocodificación Google Maps] ({}) :: Execution Time - {}", origen, horaEjecucion);

        int totalPendientes = 0;
        int totalGeocodificados = 0;
        List<String> detallesCambios = new ArrayList<>();

        try {
            List<Trayecto> pendientes = trayectoRepository.findTrayectosSinCoordenadas();
            totalPendientes = pendientes.size();

            if (pendientes.isEmpty()) {
                String msg = "Todos los trayectos se encuentran debidamente geocodificados.";
                logger.info("[CRON GEOCODIFICACION] {}", msg);
                registrarEventoHistorial("3. Geocodificación Google Maps", origen, 0, 0, msg);

                Map<String, Object> resultado = new LinkedHashMap<>();
                resultado.put("tarea", "Geocodificación Google Maps");
                resultado.put("origen", origen);
                resultado.put("ejecutadoEn", ultimaEjecucionGeocodificacion);
                resultado.put("pendientes", 0);
                resultado.put("geocodificados", 0);
                resultado.put("mensaje", msg);
                return resultado;
            }

            for (Trayecto trayecto : pendientes) {
                logger.info("Geocodificando parada: '{}' (Trayecto ID: {})", trayecto.getUbicacion(), trayecto.getId());
                double[] coordenadas = geocoder.geocodificarDireccion(trayecto.getUbicacion());

                if (coordenadas != null && coordenadas.length == 2) {
                    trayecto.setLatitud(coordenadas[0]);
                    trayecto.setLongitud(coordenadas[1]);
                    trayectoRepository.saveAndFlush(trayecto);
                    totalGeocodificados++;
                    totalTrayectosGeocodificados++;

                    String detalle = String.format("Trayecto #%d (Ruta '%s', Parada #%d - '%s') geocodificado: [Lat: %.4f, Lng: %.4f]",
                            trayecto.getId(), trayecto.getCodigoRuta(), trayecto.getOrdenParada(),
                            trayecto.getUbicacion(), coordenadas[0], coordenadas[1]);
                    detallesCambios.add(detalle);
                    logger.info("{}", detalle);
                }
            }

            String resumen = String.format("Se geocodificaron %d de %d parada(s) pendiente(s).", totalGeocodificados, totalPendientes);
            registrarEventoHistorial("3. Geocodificación Google Maps", origen, totalPendientes, totalGeocodificados, resumen);

            Map<String, Object> resultado = new LinkedHashMap<>();
            resultado.put("tarea", "Geocodificación Google Maps");
            resultado.put("origen", origen);
            resultado.put("ejecutadoEn", ultimaEjecucionGeocodificacion);
            resultado.put("pendientes", totalPendientes);
            resultado.put("geocodificados", totalGeocodificados);
            resultado.put("detalles", detallesCambios);
            resultado.put("mensaje", resumen);
            return resultado;
        } catch (Exception e) {
            logger.error("[ERROR EN TAREA GEOCODIFICACION] Ocurrió una excepción: {}", e.getMessage(), e);
            registrarEventoHistorial("3. Geocodificación Google Maps", origen, totalPendientes, 0, "Error: " + e.getMessage());
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", e.getMessage());
            return err;
        }
    }

    // Consulta de estado consolidado en vivo de las 3 tareas programadas
    public Map<String, Object> obtenerEstadoConsolidado() {
        Map<String, Object> estado = new LinkedHashMap<>();
        LocalDate hoy = LocalDate.now();

        // Conteos en vivo
        long conductoresVencidos = personaRepository.findConductoresConLicenciaVencida(hoy).size();
        long relacionesEnRO = vehiculoPersonaRepository.findByEstadoConductor("RO").size();
        long relacionesEnPO = vehiculoPersonaRepository.findByEstadoConductor("PO").size();
        long relacionesEnEA = vehiculoPersonaRepository.findByEstadoConductor("EA").size();

        long docsExpirados = vehiculoDocumentoRepository.findAll().stream()
                .filter(d -> d.getFechaVencimiento() != null && d.getFechaVencimiento().isBefore(hoy))
                .count();
        long docsEnVencido = vehiculoDocumentoRepository.findAll().stream()
                .filter(d -> "Vencido".equalsIgnoreCase(d.getEstado()))
                .count();

        long trayectosPendientesGeo = trayectoRepository.findTrayectosSinCoordenadas().size();

        Map<String, Object> tarea1 = new LinkedHashMap<>();
        tarea1.put("nombre", "Verificación de Vigencia de Licencias de Conducción");
        tarea1.put("frecuencia", "Cada 2 minutos (120.000 ms)");
        tarea1.put("ultimaEjecucion", ultimaEjecucionLicencias);
        tarea1.put("conductoresLicenciaVencida", conductoresVencidos);
        tarea1.put("relacionesEnRO", relacionesEnRO);
        tarea1.put("relacionesEnPO", relacionesEnPO);
        tarea1.put("relacionesEnEA", relacionesEnEA);
        tarea1.put("totalRestringidasHistorico", totalLicenciasRestringidas);

        Map<String, Object> tarea2 = new LinkedHashMap<>();
        tarea2.put("nombre", "Actualización de Documentos Vehiculares Expirados");
        tarea2.put("frecuencia", "Cada 2 minutos (120.000 ms)");
        tarea2.put("ultimaEjecucion", ultimaEjecucionDocumentos);
        tarea2.put("documentosConFechaExpirada", docsExpirados);
        tarea2.put("documentosEnEstadoVencido", docsEnVencido);
        tarea2.put("totalActualizadosHistorico", totalDocumentosVencidosActualizados);

        Map<String, Object> tarea3 = new LinkedHashMap<>();
        tarea3.put("nombre", "Geocodificación Automática de Trayectos (Google Maps / Geocoder)");
        tarea3.put("frecuencia", "Cada 90 segundos (90.000 ms)");
        tarea3.put("ultimaEjecucion", ultimaEjecucionGeocodificacion);
        tarea3.put("trayectosPendientesCoordenadas", trayectosPendientesGeo);
        tarea3.put("totalGeocodificadosHistorico", totalTrayectosGeocodificados);

        estado.put("timestampServidor", LocalDateTime.now().format(fullIsoFormatter));
        estado.put("fechaHoy", hoy.toString());
        estado.put("tarea1Licencias", tarea1);
        estado.put("tarea2Documentos", tarea2);
        tarea3.put("geocoderDisponible", true);
        estado.put("tarea3Geocodificacion", tarea3);
        estado.put("historialReciente", new ArrayList<>(historialEjecuciones));

        return estado;
    }
}
