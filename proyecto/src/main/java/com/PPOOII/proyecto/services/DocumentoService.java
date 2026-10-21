// Paquete de lógica de negocio y servicios de la aplicación
package com.PPOOII.proyecto.services;

// Importación de la entidad paramétrica Documento
import com.PPOOII.proyecto.entities.Documento;
// Importación del repositorio JPA de Documento
import com.PPOOII.proyecto.repository.DocumentoRepository;
// Importación de la interfaz de servicio IDocumentoService (Laboratorio 1)
import com.PPOOII.proyecto.services.Interfaces.IDocumentoService;
// Importación del gestor de logs Log4j2 (Laboratorio 1)
import org.apache.logging.log4j.LogManager;
// Importación de la interfaz Logger de Log4j2 (Laboratorio 1)
import org.apache.logging.log4j.Logger;
// Importación de la anotación @Service de Spring
import org.springframework.stereotype.Service;

// Importación de la estructura List de Java
import java.util.List;
// Importación del contenedor Optional de Java
import java.util.Optional;

// Componente de servicio de Spring para la gestión de la entidad paramétrica 'Documento'
@Service("DocumentoService")
public class DocumentoService implements IDocumentoService {

    // Instancia del logger de auditoría según el estándar del Laboratorio 1
    private static final Logger logger = LogManager.getLogger(DocumentoService.class);

    // Repositorio JPA para realizar operaciones de persistencia sobre la tabla 'documento'
    private final DocumentoRepository documentoRepository;

    // Inyección de dependencias del repositorio mediante constructor
    public DocumentoService(DocumentoRepository documentoRepository) {
        // Asigna la instancia del repositorio de documentos
        this.documentoRepository = documentoRepository;
    }

    // Consulta general: Obtiene la lista completa de todos los tipos de documentos registrados
    @Override
    public List<Documento> listarTodos() {
        // Registra en el log la consulta del catálogo de documentos
        logger.info("Consultando el catálogo completo de tipos de documentos.");
        // Retorna todos los documentos parametrizados
        return documentoRepository.findAll();
    }

    // Consulta individual: Busca un tipo de documento por su identificador primario (ID)
    @Override
    public Optional<Documento> buscarPorId(int id) {
        // Registra en el log la búsqueda por ID de documento
        logger.info("Buscando documento parametrizado con ID: {}", id);
        // Retorna el resultado envuelto en Optional
        return documentoRepository.findById(id);
    }

    // Operación Crear (POST): Inserta un nuevo documento validando que el código no exista previamente
    @Override
    public Documento guardar(Documento documento) {
        // Validación de código único en el catálogo de documentos
        if (documentoRepository.existsByCodigo(documento.getCodigo())) {
            // Lanza excepción si el código ya se encuentra registrado
            throw new IllegalArgumentException("Ya existe un documento parametrizado con el código: " + documento.getCodigo());
        }
        // Registra en el log la creación del nuevo documento parametrizado
        logger.info("Guardando nuevo documento parametrizado con código: {}", documento.getCodigo());
        // Guarda y retorna la entidad persistida
        return documentoRepository.save(documento);
    }

    // Operación Actualizar (PUT): Modifica las propiedades de un tipo de documento existente
    @Override
    public Optional<Documento> actualizar(int id, Documento documentoDetalles) {
        // Busca el documento por su ID y actualiza sus campos si existe
        return documentoRepository.findById(id).map(documentoExistente -> {
            // Si el código cambia, verifica que el nuevo código no esté ocupado por otro registro
            if (documentoDetalles.getCodigo() != null
                    && !documentoDetalles.getCodigo().equalsIgnoreCase(documentoExistente.getCodigo())
                    && documentoRepository.existsByCodigo(documentoDetalles.getCodigo())) {
                // Lanza excepción cuando el nuevo código ya existe en otro registro
                throw new IllegalArgumentException("Ya existe un documento parametrizado con el código: " + documentoDetalles.getCodigo());
            }

            // Actualización del código si fue proporcionado
            if (documentoDetalles.getCodigo() != null) {
                // Asigna el nuevo código al documento existente
                documentoExistente.setCodigo(documentoDetalles.getCodigo());
            }
            // Actualización del nombre si fue proporcionado
            if (documentoDetalles.getNombre() != null) {
                // Asigna el nuevo nombre al documento existente
                documentoExistente.setNombre(documentoDetalles.getNombre());
            }
            // Actualización de la aplicabilidad ('A', 'M', 'AM') si fue proporcionada
            if (documentoDetalles.getAplicaA() != null) {
                // Asigna la nueva aplicabilidad al documento existente
                documentoExistente.setAplicaA(documentoDetalles.getAplicaA());
            }
            // Actualización del indicador de obligatoriedad si fue proporcionado
            if (documentoDetalles.getObligatorio() != null) {
                // Asigna el nuevo indicador de obligatoriedad
                documentoExistente.setObligatorio(documentoDetalles.getObligatorio());
            }
            // Actualización de la descripción si fue proporcionada
            if (documentoDetalles.getDescripcion() != null) {
                // Asigna la nueva descripción al documento existente
                documentoExistente.setDescripcion(documentoDetalles.getDescripcion());
            }

            // Registra en el log la actualización del documento parametrizado
            logger.info("Actualizando documento parametrizado con ID: {}", id);
            // Persiste los cambios en la base de datos
            return documentoRepository.save(documentoExistente);
        });
    }

    // Operación Eliminar (DELETE): Borra un tipo de documento según su ID y retorna si fue exitoso
    @Override
    public boolean eliminar(int id) {
        // Busca el documento por su ID y lo elimina si existe
        return documentoRepository.findById(id).map(documento -> {
            // Registra en el log la eliminación del documento
            logger.info("Eliminando documento parametrizado con ID: {}", id);
            // Ejecuta el borrado de la entidad en el repositorio
            documentoRepository.delete(documento);
            // Retorna true indicando éxito
            return true;
        // Retorna false si el documento con ese ID no existía
        }).orElse(false);
    }
}