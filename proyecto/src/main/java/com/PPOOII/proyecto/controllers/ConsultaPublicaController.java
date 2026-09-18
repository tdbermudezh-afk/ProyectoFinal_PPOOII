package com.PPOOII.proyecto.controllers;

import com.PPOOII.proyecto.entities.Persona;
import com.PPOOII.proyecto.entities.Vehiculo;
import com.PPOOII.proyecto.repository.PersonaRepository;
import com.PPOOII.proyecto.repository.VehiculoDocumentoRepository;
import com.PPOOII.proyecto.repository.VehiculoPersonaRepository;
import com.PPOOII.proyecto.repository.VehiculoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/public") // Ruta exclusiva para servicios sin token
@CrossOrigin(origins = "*")
@Tag(name = "Consultas Públicas", description = "Servicios de consulta (Punto 7 de la rúbrica) que no requieren autenticación (x-api-key)")
public class ConsultaPublicaController {

    @Autowired
    private VehiculoDocumentoRepository vehiculoDocumentoRepository;

    @Autowired
    private VehiculoPersonaRepository vehiculoPersonaRepository;

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Autowired
    private PersonaRepository personaRepository;

    // 1. Consultar vehículos con documentos vencidos
    @Operation(summary = "Vehículos con documentos vencidos")
    @GetMapping("/vehiculos/documentos-vencidos")
    public ResponseEntity<List<Vehiculo>> obtenerVehiculosDocVencidos() {
        return ResponseEntity.ok(vehiculoDocumentoRepository.findVehiculosConDocumentosVencidos());
    }

    // 2. Consultar todos los conductores que puedan operar
    @Operation(summary = "Conductores autorizados para operar (PO)")
    @GetMapping("/conductores/autorizados")
    public ResponseEntity<List<Persona>> obtenerConductoresAutorizados() {
        return ResponseEntity.ok(vehiculoPersonaRepository.findConductoresQuePuedenOperar());
    }

    // 3. Consultar vehículo por placa relacionando conductores y documentos
    @Operation(summary = "Detalle completo de un vehículo por su placa")
    @GetMapping("/vehiculos/placa/{placa}/detalles")
    public ResponseEntity<Vehiculo> obtenerDetallesVehiculoPorPlaca(@PathVariable String placa) {
        Optional<Vehiculo> vehiculo = vehiculoRepository.findByPlaca(placa);
        return vehiculo.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        // Nota: Como tienes @JsonBackReference y @ManyToOne configurados, 
        // la relación de documentos y personas debería viajar automáticamente en el JSON del Vehículo.
    }

    // 4. Consultar vehículos con documentos por vencer (param: dias)
    @Operation(summary = "Vehículos con documentos por vencer en un lapso de tiempo")
    @GetMapping("/vehiculos/documentos-por-vencer")
    public ResponseEntity<List<Vehiculo>> obtenerVehiculosDocPorVencer(
            @RequestParam(defaultValue = "30") int diasFaltantes) {
        
        LocalDate fechaLimite = LocalDate.now().plusDays(diasFaltantes);
        return ResponseEntity.ok(vehiculoDocumentoRepository.findVehiculosConDocumentosPorVencer(fechaLimite));
    }

    // 5. Consultar el total de personas agrupadas por tipo
    @Operation(summary = "Estadística de personas por tipo")
    @GetMapping("/personas/agrupadas-por-tipo")
    public ResponseEntity<Map<String, Long>> obtenerTotalPersonasPorTipo() {
        List<Object[]> resultados = personaRepository.countPersonasByTipo();
        Map<String, Long> respuesta = new HashMap<>();
        
        for (Object[] fila : resultados) {
            String tipo = fila[0] != null ? fila[0].toString() : "Desconocido";
            Long total = fila[1] != null ? ((Number) fila[1]).longValue() : 0L;
            respuesta.put(tipo, total);
        }
        
        return ResponseEntity.ok(respuesta);
    }
}