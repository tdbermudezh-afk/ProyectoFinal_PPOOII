package com.PPOOII.proyecto.controllers;

import com.PPOOII.proyecto.entities.VehiculoDocumento;
import com.PPOOII.proyecto.repository.PersonaRepository;
import com.PPOOII.proyecto.repository.VehiculoDocumentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/publico")
public class ConsultasController {

    @Autowired
    private VehiculoDocumentoRepository vehiculoDocumentoRepository;

    @Autowired
    private PersonaRepository personaRepository;

    // 1. Vehículos con documentos actualmente vencidos
    @GetMapping("/vehiculos/documentos-vencidos")
    public ResponseEntity<List<VehiculoDocumento>> obtenerDocumentosVencidos() {
        LocalDate hoy = LocalDate.now();
        List<VehiculoDocumento> vencidos = vehiculoDocumentoRepository.findAll().stream()
                .filter(doc -> doc.getFechaVencimiento() != null && doc.getFechaVencimiento().isBefore(hoy))
                .collect(Collectors.toList());
        return ResponseEntity.ok(vencidos);
    }

    // 2. Vehículos con documentos próximos a vencer (en los próximos N días)
    @GetMapping("/vehiculos/documentos-por-vencer")
    public ResponseEntity<List<VehiculoDocumento>> obtenerDocumentosPorVencer(
            @RequestParam(defaultValue = "30") int dias) {
        
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(dias);

        List<VehiculoDocumento> porVencer = vehiculoDocumentoRepository.findAll().stream()
                .filter(doc -> doc.getFechaVencimiento() != null &&
                        !doc.getFechaVencimiento().isBefore(hoy) &&
                        doc.getFechaVencimiento().isBefore(limite))
                .collect(Collectors.toList());

        return ResponseEntity.ok(porVencer);
    }

    // 3. Conteo total de personas registradas agrupadas por tipo
    @GetMapping("/personas/resumen-tipos")
    public ResponseEntity<Map<String, Object>> obtenerResumenPersonas() {
        long totalAdministrativos = personaRepository.findByTipoPersona("A").size();
        long totalConductores = personaRepository.findByTipoPersona("C").size();
        long totalGeneral = personaRepository.count();

        Map<String, Object> resumen = new HashMap<>();
        resumen.put("totalRegistrados", totalGeneral);
        resumen.put("administrativos", totalAdministrativos);
        resumen.put("conductores", totalConductores);

        return ResponseEntity.ok(resumen);
    }
}