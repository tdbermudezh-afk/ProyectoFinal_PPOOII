// Paquete de controladores REST de la aplicación
package com.PPOOII.proyecto.controllers;

import com.PPOOII.proyecto.entities.Documento;
import com.PPOOII.proyecto.exceptions.ResourceNotFoundException;
import com.PPOOII.proyecto.services.DocumentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controlador REST que implementa el CRUD completo de la entidad paramétrica de Documentos
@RestController
@RequestMapping("/api/documentos")
@CrossOrigin(origins = "*")
@Tag(name = "Documentos", description = "Endpoints para la gestión de documentos del sistema")
public class DocumentoController {

    // Inyección de la capa de servicios para la entidad Documento
    private final DocumentoService documentoService;

    // Inyección de dependencias mediante el constructor
    public DocumentoController(DocumentoService documentoService) {
        this.documentoService = documentoService;
    }

    // Endpoint GET: Consulta y retorna el catálogo completo de tipos de documentos
    @Operation(summary = "Obtener todos los documentos", description = "Retorna una lista completa de todos los documentos registrados")
    @ApiResponse(responseCode = "200", description = "Lista recuperada exitosamente")
    @GetMapping
    public List<Documento> listarTodos() {
        return documentoService.listarTodos();
    }

    // Endpoint GET por ID: Consulta un tipo de documento específico según su identificador único
    @Operation(summary = "Buscar documento por ID", description = "Retorna la información de un documento según su identificador único")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Documento encontrado"),
        @ApiResponse(responseCode = "404", description = "Documento no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Documento> buscarPorId(@PathVariable int id) {
        Documento documento = documentoService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado con el ID: " + id));
        return ResponseEntity.ok(documento);
    }

    // Endpoint POST: Registra un nuevo tipo de documento en el sistema validando sus reglas
    @Operation(summary = "Crear nuevo documento", description = "Registra un nuevo tipo de documento en el sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Documento creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (Error de validación)")
    })
    @PostMapping
    public ResponseEntity<Documento> crear(@Valid @RequestBody Documento documento) {
        Documento nuevo = documentoService.guardar(documento);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    // Endpoint PUT: Actualiza la información de un tipo de documento existente (completa el CRUD)
    @Operation(summary = "Actualizar documento", description = "Actualiza la información de un documento parametrizado según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Documento actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Documento no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Documento> actualizar(@PathVariable int id, @Valid @RequestBody Documento documento) {
        Documento documentoActualizado = documentoService.actualizar(id, documento)
                .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado con el ID: " + id));
        return ResponseEntity.ok(documentoActualizado);
    }

    // Endpoint DELETE: Elimina un tipo de documento del sistema por su identificador único
    @Operation(summary = "Eliminar documento", description = "Elimina un registro de documento según su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Documento eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Documento no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        boolean eliminado = documentoService.eliminar(id);
        if (!eliminado) {
            throw new ResourceNotFoundException("Documento no encontrado con el ID: " + id);
        }
        return ResponseEntity.noContent().build();
    }
}