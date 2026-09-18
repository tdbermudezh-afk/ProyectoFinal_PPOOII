package com.PPOOII.proyecto.controllers;

import com.PPOOII.proyecto.entities.Vehiculo;
import com.PPOOII.proyecto.entities.VehiculoDocumento;
import com.PPOOII.proyecto.exceptions.ResourceNotFoundException;
import com.PPOOII.proyecto.services.VehiculoService;
import com.PPOOII.proyecto.services.VehiculoDocumentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehiculos")
@CrossOrigin(origins = "*")
@Tag(name = "Vehículos", description = "Endpoints para la gestión e inspección de vehículos")
public class VehiculoController {

    // Inyección de dependencias centralizada
    private final VehiculoService vehiculoService;
    private final VehiculoDocumentoService vehiculoDocumentoService;

    // Un único constructor para ambas inyecciones
    public VehiculoController(VehiculoService vehiculoService, VehiculoDocumentoService vehiculoDocumentoService) {
        this.vehiculoService = vehiculoService;
        this.vehiculoDocumentoService = vehiculoDocumentoService;
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
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con la placa: " + placa));
    }

    @Operation(summary = "Crear nuevo vehículo", description = "Registra un nuevo vehículo validando que sus atributos cumplan los requisitos estipulados y contenga documentos asociados")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Vehículo creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (Error de validación)")
    })
    @PostMapping
    public ResponseEntity<Vehiculo> crear(@Valid @RequestBody Vehiculo vehiculo) {
        Vehiculo nuevo = vehiculoService.guardar(vehiculo);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @Operation(summary = "Actualizar vehículo", description = "Actualiza la información de un vehículo existente según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Vehículo actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Vehiculo> actualizar(@PathVariable int id, @Valid @RequestBody Vehiculo vehiculoDetalles) {
        Vehiculo vehiculoActualizado = vehiculoService.actualizar(id, vehiculoDetalles)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con el ID: " + id));
        return ResponseEntity.ok(vehiculoActualizado);
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

    @Operation(summary = "Buscar vehículos por tipo de vehículo", description = "Retorna una lista de vehículos según su tipo (Automóvil / Motocicleta)")
    @ApiResponse(responseCode = "200", description = "Lista de vehículos recuperada exitosamente")
    @GetMapping("/tipo/{tipoVehiculo}")
    public ResponseEntity<List<Vehiculo>> buscarPorTipoVehiculo(@PathVariable String tipoVehiculo) {
        return ResponseEntity.ok(vehiculoService.buscarPorTipoVehiculo(tipoVehiculo));
    }

    @Operation(summary = "Buscar vehículos por tipo de documento en común", description = "Retorna vehículos que tengan en común el documento parametrizado especificado por su ID")
    @ApiResponse(responseCode = "200", description = "Lista de vehículos recuperada exitosamente")
    @GetMapping("/documento/{documentoId}")
    public ResponseEntity<List<Vehiculo>> buscarPorTipoDocumento(@PathVariable int documentoId) {
        return ResponseEntity.ok(vehiculoService.buscarPorTipoDocumento(documentoId));
    }

    @Operation(summary = "Buscar vehículos por estado del documento asociado", description = "Retorna vehículos según el estado de sus documentos (Habilitado, Vencido, En Verificación)")
    @ApiResponse(responseCode = "200", description = "Lista de vehículos recuperada exitosamente")
    @GetMapping("/estado-documento/{estado}")
    public ResponseEntity<List<Vehiculo>> buscarPorEstadoDocumento(@PathVariable String estado) {
        return ResponseEntity.ok(vehiculoService.buscarPorEstadoDocumento(estado));
    }

    @Operation(summary = "Agregar documento a un vehículo", description = "Asocia un nuevo documento al historial del vehículo especificado por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Documento agregado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos del documento inválidos"),
        @ApiResponse(responseCode = "404", description = "Vehículo no encontrado")
    })
    @PostMapping("/{id}/documentos")
    public ResponseEntity<Vehiculo> agregarDocumento(
            @PathVariable int id, 
            @Valid @RequestBody VehiculoDocumento documento) {
        
        Vehiculo vehiculoActualizado = vehiculoService.agregarDocumentoAVehiculo(id, documento)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con el ID: " + id));
                
        return ResponseEntity.ok(vehiculoActualizado);
    }

    // NUEVO ENDPOINT REQUERIDO POR LA RÚBRICA: Cargue de documentos PDF en Base64
    @Operation(summary = "Cargar documentos en Base64", description = "Procesa un listado de documentos incluyendo su archivo PDF codificado en Base64")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Documentos cargados exitosamente"),
        @ApiResponse(responseCode = "400", description = "Estructura del payload inválida")
    })
    @PostMapping("/documentos/upload")
    public ResponseEntity<List<VehiculoDocumento>> cargarDocumentosPdf(@RequestBody List<Map<String, Object>> payload) {
        List<VehiculoDocumento> documentosGuardados = vehiculoDocumentoService.cargarDocumentos(payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(documentosGuardados);
    }
}