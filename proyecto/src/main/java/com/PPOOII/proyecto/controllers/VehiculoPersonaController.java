// Paquete de controladores REST de la aplicación
package com.PPOOII.proyecto.controllers;

// Importación de la entidad de relación VehiculoPersona
import com.PPOOII.proyecto.entities.VehiculoPersona;
// Importación del servicio VehiculoPersonaService
import com.PPOOII.proyecto.services.VehiculoPersonaService;
// Importación de @Operation de Swagger OpenAPI (Laboratorio 4)
import io.swagger.v3.oas.annotations.Operation;
// Importación de @Content de Swagger OpenAPI (Laboratorio 4)
import io.swagger.v3.oas.annotations.media.Content;
// Importación de @Schema de Swagger OpenAPI (Laboratorio 4)
import io.swagger.v3.oas.annotations.media.Schema;
// Importación de @ApiResponse de Swagger OpenAPI (Laboratorio 4)
import io.swagger.v3.oas.annotations.responses.ApiResponse;
// Importación de @ApiResponses de Swagger OpenAPI (Laboratorio 4)
import io.swagger.v3.oas.annotations.responses.ApiResponses;
// Importación de @Tag de Swagger OpenAPI (Laboratorio 4)
import io.swagger.v3.oas.annotations.tags.Tag;
// Importación de @Autowired para inyección de dependencias
import org.springframework.beans.factory.annotation.Autowired;
// Importación de HttpStatus para códigos de estado HTTP
import org.springframework.http.HttpStatus;
// Importación de ResponseEntity para respuestas HTTP
import org.springframework.http.ResponseEntity;
// Importación de anotaciones web de Spring MVC
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Importación de la interfaz Map de Java
import java.util.Map;

// Controlador REST para gestionar las asignaciones y estados entre conductores y vehículos
@RestController
@RequestMapping("/api/asignaciones")
@CrossOrigin(origins = "*")
@Tag(name = "Asignaciones Conductor-Vehículo", description = "Endpoints para gestionar la relación y estados entre vehículos y conductores")
public class VehiculoPersonaController {

    // Inyección del servicio de negocio de relación Vehículo-Persona
    @Autowired
    private VehiculoPersonaService vehiculoPersonaService;

    // Punto 5: Asociar conductor a vehículo mediante variables de ruta
    @Operation(summary = "Asociar conductor a vehículo", description = "Asigna un vehículo a un conductor específico con un estado operativo inicial.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Conductor asociado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehiculoPersona.class))),
            @ApiResponse(responseCode = "400", description = "Persona no es conductor o datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autorizado (Falta x-api-key o token JWT)", content = @Content)
    })
    @PostMapping("/conductor/{personaId}/vehiculo/{vehiculoId}")
    public ResponseEntity<VehiculoPersona> asociarConductor(
            @PathVariable Long personaId,
            @PathVariable Long vehiculoId,
            @RequestBody(required = false) Map<String, String> body) {

        // Determina el estado inicial enviado en el cuerpo JSON o 'EA' por defecto
        String estadoInicial = (body != null && body.get("estadoInicial") != null) ? body.get("estadoInicial") : "EA";
        // Invoca el servicio para crear la asociación entre conductor y vehículo
        VehiculoPersona nuevaAsignacion = vehiculoPersonaService.asociarConductorAVehiculo(personaId, vehiculoId, estadoInicial);
        // Retorna HTTP 201 Created con la relación creada
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaAsignacion);
    }

    // Punto 6: Cambiar el estado del conductor en relación con el vehículo
    @Operation(summary = "Cambiar estado del conductor", description = "Actualiza el estado de la asignación (PO - Para Operar, EA - En Asignación, RO - Restringido para Operar).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehiculoPersona.class))),
            @ApiResponse(responseCode = "400", description = "Estado inválido o faltante", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autorizado (Falta x-api-key o token JWT)", content = @Content)
    })
    @PutMapping("/{idRelacion}/estado")
    public ResponseEntity<VehiculoPersona> cambiarEstadoConductor(
            @PathVariable Long idRelacion,
            @RequestBody Map<String, String> body) {

        // Extrae el nuevo estado desde la llave 'estado' o 'estadoConductor'
        String nuevoEstado = body.getOrDefault("estado", body.get("estadoConductor"));
        // Verifica que se haya especificado un valor de estado
        if (nuevoEstado == null || nuevoEstado.isBlank()) {
            // Retorna HTTP 400 Bad Request si no se proporcionó el estado
            return ResponseEntity.badRequest().build();
        }

        // Invoca el servicio para actualizar el estado en la base de datos
        VehiculoPersona asignacionActualizada = vehiculoPersonaService.cambiarEstadoConductor(idRelacion, nuevoEstado);
        // Retorna HTTP 200 OK con la asignación actualizada
        return ResponseEntity.ok(asignacionActualizada);
    }
}