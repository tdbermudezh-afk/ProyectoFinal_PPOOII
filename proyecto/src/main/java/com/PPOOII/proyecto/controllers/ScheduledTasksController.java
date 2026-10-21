package com.PPOOII.proyecto.controllers;

import com.PPOOII.proyecto.Component.ScheduledTasks;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

// Controlador REST para monitoreo, auditoría y ejecución bajo demanda de las Tareas Programadas (@Scheduled) de la Entrega 3
@RestController
@RequestMapping("/api/tareas-programadas")
@CrossOrigin(origins = "*")
@Tag(name = "Monitor de Tareas Programadas (@Scheduled)", description = "Servicios para consultar el estado en tiempo real y disparar bajo demanda las tareas automáticas de fondo (Entrega 3)")
public class ScheduledTasksController {

    @Autowired
    private ScheduledTasks scheduledTasks;

    @Operation(summary = "Consultar estado en vivo de las tareas programadas", description = "Retorna métricas en tiempo real, última ejecución, historial reciente de eventos y conteos de conductores, documentos y coordenadas.")
    @GetMapping("/estado")
    public ResponseEntity<Map<String, Object>> obtenerEstadoTareas() {
        return ResponseEntity.ok(scheduledTasks.obtenerEstadoConsolidado());
    }

    @Operation(summary = "Ejecutar tarea programada bajo demanda", description = "Dispara inmediatamente una de las tareas programadas ('licencias', 'documentos', 'geocodificacion' o 'todas') y retorna el reporte detallado.")
    @PostMapping("/ejecutar/{tarea}")
    public ResponseEntity<Map<String, Object>> ejecutarTareaManual(@PathVariable String tarea) {
        String tipo = tarea.trim().toLowerCase();
        Map<String, Object> respuesta = new LinkedHashMap<>();

        switch (tipo) {
            case "licencias":
            case "conductores":
            case "tarea1":
                respuesta.put("resultado", scheduledTasks.ejecutarVerificarLicenciasManual());
                break;
            case "documentos":
            case "vehiculos":
            case "tarea2":
                respuesta.put("resultado", scheduledTasks.ejecutarVerificarDocumentosManual());
                break;
            case "geocodificacion":
            case "trayectos":
            case "mapa":
            case "tarea3":
                respuesta.put("resultado", scheduledTasks.ejecutarGeocodificacionManual());
                break;
            case "todas":
            case "all":
                Map<String, Object> todas = new LinkedHashMap<>();
                todas.put("tarea1Licencias", scheduledTasks.ejecutarVerificarLicenciasManual());
                todas.put("tarea2Documentos", scheduledTasks.ejecutarVerificarDocumentosManual());
                todas.put("tarea3Geocodificacion", scheduledTasks.ejecutarGeocodificacionManual());
                respuesta.put("resultado", todas);
                break;
            default:
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Tipo de tarea desconocido: '" + tarea + "'. Valores permitidos: 'licencias', 'documentos', 'geocodificacion', 'todas'."
                ));
        }

        respuesta.put("estadoActualizado", scheduledTasks.obtenerEstadoConsolidado());
        return ResponseEntity.ok(respuesta);
    }
}
