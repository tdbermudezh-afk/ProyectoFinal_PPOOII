package com.PPOOII.proyecto.controllers;

import com.PPOOII.proyecto.entities.VehiculoDocumento;
import com.PPOOII.proyecto.services.Interfaces.IPersonaService;
import com.PPOOII.proyecto.services.Interfaces.IVehiculoDocumentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Controlador complementario de consultas públicas delegando estrictamente a la capa @Service
@RestController
@RequestMapping("/api/publico")
@CrossOrigin(origins = "*")
public class ConsultasController {

    @Autowired
    private IVehiculoDocumentoService vehiculoDocumentoService;

    @Autowired
    private IPersonaService personaService;

    // 1. Vehículos con documentos actualmente vencidos
    @GetMapping("/vehiculos/documentos-vencidos")
    public ResponseEntity<List<VehiculoDocumento>> obtenerDocumentosVencidos() {
        return ResponseEntity.ok(vehiculoDocumentoService.obtenerDocumentosVencidos());
    }

    // 2. Vehículos con documentos próximos a vencer (en los próximos N días)
    @GetMapping("/vehiculos/documentos-por-vencer")
    public ResponseEntity<List<VehiculoDocumento>> obtenerDocumentosPorVencer(
            @RequestParam(defaultValue = "30") int dias) {
        return ResponseEntity.ok(vehiculoDocumentoService.obtenerDocumentosPorVencer(dias));
    }

    // 3. Conteo total de personas registradas agrupadas por tipo
    @GetMapping("/personas/resumen-tipos")
    public ResponseEntity<Map<String, Object>> obtenerResumenPersonas() {
        return ResponseEntity.ok(personaService.obtenerResumenPersonas());
    }
}
