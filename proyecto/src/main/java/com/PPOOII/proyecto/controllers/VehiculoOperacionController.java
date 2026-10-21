// Paquete de controladores REST de la aplicación
package com.PPOOII.proyecto.controllers;

// Importación de la entidad VehiculoDocumento
import com.PPOOII.proyecto.entities.VehiculoDocumento;
// Importación de la entidad VehiculoPersona
import com.PPOOII.proyecto.entities.VehiculoPersona;
// Importación del servicio VehiculoDocumentoService
import com.PPOOII.proyecto.services.VehiculoDocumentoService;
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
// Importación de MediaType para soporte de multipart/form-data
import org.springframework.http.MediaType;
// Importación de ResponseEntity para respuestas HTTP
import org.springframework.http.ResponseEntity;
// Importación de anotaciones web de Spring MVC
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
// Importación de MultipartFile para recepción de archivos PDF
import org.springframework.web.multipart.MultipartFile;

// Importación de la interfaz List de Java
import java.util.List;
// Importación de la interfaz Map de Java
import java.util.Map;

// Controlador REST protegido para operaciones de cargue de documentos PDF Base64 y gestión de conductores
@RestController
@RequestMapping("/api/operaciones")
@CrossOrigin(origins = "*")
@Tag(name = "Operaciones Vehiculares (Documentos PDF Base64 y Conductores)", description = "Endpoints protegidos para cargue de documentos en formato PDF Base64 y asociación/estados de conductores")
public class VehiculoOperacionController {

    // Inyección del servicio de documentos de vehículos
    @Autowired
    private VehiculoDocumentoService vehiculoDocumentoService;

    // Inyección del servicio de asociación entre vehículos y personas (conductores)
    @Autowired
    private VehiculoPersonaService vehiculoPersonaService;

    // 1. Carga masiva/individual de documentos PDF Base64 (uno o varios documentos a la vez)
    @Operation(summary = "Cargar documentos PDF Base64 a vehículos", description = "Recibe una lista de documentos con su representación obligatoria en PDF Base64 ('documentoPdfBase64'), fechas y estado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Documentos PDF Base64 cargados exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehiculoDocumento.class))),
            @ApiResponse(responseCode = "400", description = "Falta el archivo en Base64, incompatibilidad de tipo de vehículo o fechas inválidas", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autorizado (Falta x-api-key o token JWT)", content = @Content)
    })
    @PostMapping("/documentos/cargar")
    public ResponseEntity<List<VehiculoDocumento>> cargarDocumentos(@RequestBody List<Map<String, Object>> documentosPayload) {
        // Procesa y guarda la lista de documentos PDF Base64 mediante el servicio
        List<VehiculoDocumento> resultado = vehiculoDocumentoService.cargarDocumentos(documentosPayload);
        // Retorna HTTP 201 Created con los documentos almacenados
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    // 1.B. Subir archivo físico .pdf y convertirlo automáticamente a Base64 en columna LONGBLOB
    @Operation(summary = "Subir archivo PDF de documento vehicular (conversión automática a Base64)", description = "Permite subir un archivo en formato PDF, realiza la conversión automática a Base64 y lo guarda dentro del campo LONGBLOB.")
    @PostMapping(value = "/documentos/cargar-archivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VehiculoDocumento> cargarArchivoPdf(
            @RequestParam("vehiculoId") Integer vehiculoId,
            @RequestParam("documentoId") Integer documentoId,
            @RequestParam("fechaExpedicion") String fechaExpedicion,
            @RequestParam("fechaVencimiento") String fechaVencimiento,
            @RequestParam(value = "estado", required = false, defaultValue = "En Verificación") String estado,
            @RequestParam("file") MultipartFile file) {
        VehiculoDocumento guardado = vehiculoDocumentoService.cargarArchivoPdf(
                vehiculoId, documentoId, fechaExpedicion, fechaVencimiento, estado, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    // 2. Asociar vehículos a un conductor específico
    @Operation(summary = "Asociar conductor a un vehículo (por parámetros)", description = "Vincula una persona de tipo Conductor ('C') a un vehículo con un estado operativo inicial ('PO', 'EA' o 'RO').")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Conductor asociado exitosamente al vehículo", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehiculoPersona.class))),
            @ApiResponse(responseCode = "400", description = "La persona no es conductor o datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autorizado (Falta x-api-key o token JWT)", content = @Content)
    })
    @PostMapping("/conductores/asociar")
    public ResponseEntity<VehiculoPersona> asociarConductor(
            @RequestParam Long personaId,
            @RequestParam Long vehiculoId,
            @RequestParam(required = false, defaultValue = "EA") String estadoConductor) {

        // Invoca el servicio para registrar la asociación entre el conductor y el vehículo
        VehiculoPersona vp = vehiculoPersonaService.asociarConductorAVehiculo(personaId, vehiculoId, estadoConductor);
        // Retorna HTTP 201 Created con la relación creada
        return ResponseEntity.status(HttpStatus.CREATED).body(vp);
    }

    // 3. Cambiar estado del conductor (PO, EA, RO)
    @Operation(summary = "Cambiar estado operativo del conductor", description = "Actualiza el estado del conductor respecto al vehículo ('PO' Puede Operar, 'EA' Espera de Aprobación, 'RO' Restringido para Operar).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado del conductor actualizado con éxito", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehiculoPersona.class))),
            @ApiResponse(responseCode = "400", description = "Estado no permitido o relación no encontrada", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autorizado (Falta x-api-key o token JWT)", content = @Content)
    })
    @PutMapping("/conductores/relacion/{idRelacion}/estado")
    public ResponseEntity<VehiculoPersona> cambiarEstadoConductor(
            @PathVariable Long idRelacion,
            @RequestBody Map<String, String> payload) {

        // Obtiene el nuevo estado desde la llave 'estadoConductor' (o 'estado' como alternativa)
        String nuevoEstado = payload.getOrDefault("estadoConductor", payload.get("estado"));
        // Invoca el servicio para validar y actualizar el estado de la relación
        VehiculoPersona vpActualizado = vehiculoPersonaService.cambiarEstadoConductor(idRelacion, nuevoEstado);
        // Retorna HTTP 200 OK con la relación actualizada
        return ResponseEntity.ok(vpActualizado);
    }

    // 4. Listar todas las relaciones Conductor-Vehículo activas e históricas
    @Operation(summary = "Listar todas las relaciones Conductor-Vehículo", description = "Retorna todas las asociaciones registradas en vehiculo_personas con su estado operativo actual ('PO', 'EA', 'RO').")
    @GetMapping("/conductores/relaciones")
    public ResponseEntity<List<VehiculoPersona>> listarRelacionesConductores() {
        return ResponseEntity.ok(vehiculoPersonaService.listarTodas());
    }

    // 5. Cambiar el estado de un documento vehicular ('Habilitado', 'Vencido', 'En Verificación')
    @Operation(summary = "Cambiar estado de un documento vehicular", description = "Actualiza el estado de un registro en vehiculo_documento ('Habilitado', 'Vencido', 'En Verificación').")
    @PutMapping("/documentos/{id}/estado")
    public ResponseEntity<VehiculoDocumento> cambiarEstadoDocumento(
            @PathVariable int id,
            @RequestBody Map<String, String> payload) {
        String nuevoEstado = payload.getOrDefault("estado", payload.get("nuevoEstado"));
        return ResponseEntity.ok(vehiculoDocumentoService.cambiarEstadoDocumento(id, nuevoEstado));
    }
}
