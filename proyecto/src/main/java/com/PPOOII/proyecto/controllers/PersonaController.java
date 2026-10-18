// Paquete de controladores REST de la aplicación
package com.PPOOII.proyecto.controllers;

// Importación de la entidad Persona
import com.PPOOII.proyecto.entities.Persona;
// Importación del servicio PersonaService
import com.PPOOII.proyecto.services.PersonaService;
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
// Importación de @Valid para validación de campos de entrada
import jakarta.validation.Valid;
// Importación de @Autowired para inyección de dependencias
import org.springframework.beans.factory.annotation.Autowired;
// Importación de HttpStatus para códigos de estado HTTP
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RestController;

// Importación de la interfaz List de Java
import java.util.List;

// Controlador REST para la gestión de Personas (Conductores y Administrativos) con soporte CORS y Swagger
@RestController
@RequestMapping("/api/personas")
@CrossOrigin(origins = "*")
@Tag(name = "Personas (Conductores y Administrativos)", description = "Endpoints para crear, actualizar y consultar personas; genera automáticamente el Usuario para tipo Administrativo ('A')")
public class PersonaController {

    // Inyección del servicio de negocio de Personas
    @Autowired
    private PersonaService personaService;

    // Endpoint POST /api/personas para registrar un nuevo conductor o administrativo
    @Operation(summary = "Crear una nueva persona", description = "Registra una persona (Conductor 'C' con licencia PDF Base64 obligatoria, o Administrativo 'A' generando automáticamente su Usuario, password y APIKey).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Persona creada exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Persona.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, identificación/correo duplicado o falta licencia PDF Base64 en conductor", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Persona> crearPersona(@Valid @RequestBody Persona persona) {
        // Invoca el servicio para persistir la persona y generar su usuario si es administrativa
        Persona nuevaPersona = personaService.crearPersona(persona);
        // Retorna HTTP 201 Created con la entidad creada
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaPersona);
    }

    // Endpoint GET /api/personas para listar todas las personas registradas
    @Operation(summary = "Listar todas las personas", description = "Obtiene el listado completo de personas registradas en el sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de personas obtenido con éxito", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Persona.class)))
    })
    @GetMapping
    public ResponseEntity<List<Persona>> listarTodas() {
        // Retorna HTTP 200 OK con la lista de todas las personas
        return ResponseEntity.ok(personaService.listarTodas());
    }

    // Endpoint GET /api/personas/{id} para consultar una persona por su ID primario
    @Operation(summary = "Obtener persona por ID", description = "Busca y retorna una persona específica mediante su identificador único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Persona encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Persona.class))),
            @ApiResponse(responseCode = "400", description = "Persona no encontrada con el ID proporcionado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Persona> obtenerPorId(@PathVariable Long id) {
        // Retorna HTTP 200 OK con la persona encontrada
        return ResponseEntity.ok(personaService.obtenerPorId(id));
    }

    // Endpoint GET /api/personas/tipo/{tipoPersona} para filtrar por Conductor ('C') o Administrativo ('A')
    @Operation(summary = "Listar personas por tipo", description = "Filtra las personas registradas según su tipo ('C' para Conductor, 'A' para Administrativo).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado filtrado obtenido con éxito", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Persona.class)))
    })
    @GetMapping("/tipo/{tipoPersona}")
    public ResponseEntity<List<Persona>> listarPorTipo(@PathVariable String tipoPersona) {
        // Retorna HTTP 200 OK con la lista filtrada por tipo de persona
        return ResponseEntity.ok(personaService.listarPorTipo(tipoPersona));
    }

    // Endpoint PUT /api/personas/{id} para actualizar los datos de una persona existente
    @Operation(summary = "Actualizar una persona existente", description = "Actualiza los datos de una persona por su ID; si cambia a Administrativo ('A'), genera su Usuario.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Persona actualizada exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Persona.class))),
            @ApiResponse(responseCode = "400", description = "Error de validación o persona no encontrada", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<Persona> actualizarPersona(@PathVariable Long id, @Valid @RequestBody Persona persona) {
        // Invoca el servicio para aplicar los cambios sobre la persona
        Persona personaActualizada = personaService.actualizarPersona(id, persona);
        // Retorna HTTP 200 OK con la persona actualizada
        return ResponseEntity.ok(personaActualizada);
    }
}