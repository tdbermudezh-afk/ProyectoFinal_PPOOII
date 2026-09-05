package com.PPOOII.proyecto.controllers;

import com.PPOOII.proyecto.entities.Vehiculo;
import com.PPOOII.proyecto.entities.VehiculoDocumento;
import com.PPOOII.proyecto.exceptions.ResourceNotFoundException;
import com.PPOOII.proyecto.services.VehiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehiculos")
@CrossOrigin(origins = "*")
@Tag(name = "Vehículos", description = "Endpoints para la gestión e inspección de vehículos")
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @Operation(summary = "Obtener todos los vehículos", description = "Retorna una lista completa con todos los vehículos registrados")
    @ApiResponse(responseCode = "200", description = "Lista recuperada exitosamente")
    @GetMapping
    public List<Vehiculo> listarTodos() {
        return vehiculoService.listarTodos();
    }

    @Operation(summary = "Buscar vehículo por ID", description = "Retorna la información del vehículo según su identificador único")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Vehículo encontrado"),
        @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Vehiculo> buscarPorId(@PathVariable int id) {
        Vehiculo vehiculo = vehiculoService.buscarPorId(id);
        return ResponseEntity.ok(vehiculo);
    }

    @Operation(summary = "Buscar vehículo por Placa", description = "Obtiene los detalles del vehículo mediante su número de placa")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Vehículo encontrado"),
        @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    @GetMapping("/placa/{placa}")
    public ResponseEntity<Vehiculo> buscarPorPlaca(@PathVariable String placa) {
        return vehiculoService.buscarPorPlaca(placa)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Vehiculo no encontrado con la placa: " + placa));
    }

    @Operation(summary = "Crear nuevo vehículo", description = "Registra un nuevo vehículo validando que sus atributos cumplan los requisitos estipulados")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Vehículo creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (Error de validación)")
    })
    @PostMapping
    public ResponseEntity<Vehiculo> crear(@Valid @RequestBody Vehiculo vehiculo) {
        Vehiculo nuevo = vehiculoService.guardar(vehiculo);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @Operation(summary = "Eliminar vehículo", description = "Elimina un registro de vehículo según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Vehículo eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        vehiculoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Agregar documento a un vehículo", description = "Asocia un nuevo documento al historial del vehículo especificado por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Documento agregado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    @PostMapping("/{id}/documentos")
    public ResponseEntity<Vehiculo> agregarDocumento(
            @PathVariable int id, 
            @RequestBody VehiculoDocumento documento) {
        
        Vehiculo vehiculoActualizado = vehiculoService.agregarDocumentoAVehiculo(id, documento)
                .orElseThrow(() -> new ResourceNotFoundException("Vehiculo no encontrado con id: " + id));
                
        return ResponseEntity.ok(vehiculoActualizado);
    }
}