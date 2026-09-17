// Paquete de lógica de negocio y servicios
package com.PPOOII.proyecto.services;

import com.PPOOII.proyecto.entities.Documento;
import com.PPOOII.proyecto.repository.DocumentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// Componente de servicio de Spring para la gestión de la entidad paramétrica 'Documento'
@Service
public class DocumentoService {

    // Repositorio JPA para realizar operaciones de persistencia sobre la tabla 'documento'
    private final DocumentoRepository documentoRepository;

    // Inyección de dependencias del repositorio mediante constructor
    public DocumentoService(DocumentoRepository documentoRepository) {
        this.documentoRepository = documentoRepository;
    }

    // Consulta general: Obtiene la lista completa de todos los tipos de documentos registrados
    public List<Documento> listarTodos() {
        return documentoRepository.findAll();
    }

    // Consulta individual: Busca un tipo de documento por su identificador primario (ID)
    public Optional<Documento> buscarPorId(int id) {
        return documentoRepository.findById(id);
    }

    // Operación Crear (POST): Inserta un nuevo documento validando que el código no exista previamente
    public Documento guardar(Documento documento) {
        // Validación de código único en el catálogo de documentos
        if (documentoRepository.existsByCodigo(documento.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un documento parametrizado con el código: " + documento.getCodigo());
        }
        return documentoRepository.save(documento);
    }

    // Operación Actualizar (PUT): Modifica las propiedades de un tipo de documento existente
    public Optional<Documento> actualizar(int id, Documento documentoDetalles) {
        return documentoRepository.findById(id).map(documentoExistente -> {
            // Si el código cambia, verifica que el nuevo código no esté ocupado por otro registro
            if (documentoDetalles.getCodigo() != null
                    && !documentoDetalles.getCodigo().equalsIgnoreCase(documentoExistente.getCodigo())
                    && documentoRepository.existsByCodigo(documentoDetalles.getCodigo())) {
                throw new IllegalArgumentException("Ya existe un documento parametrizado con el código: " + documentoDetalles.getCodigo());
            }

            // Actualización de los atributos del documento parametrizado
            if (documentoDetalles.getCodigo() != null) {
                documentoExistente.setCodigo(documentoDetalles.getCodigo());
            }
            if (documentoDetalles.getNombre() != null) {
                documentoExistente.setNombre(documentoDetalles.getNombre());
            }
            if (documentoDetalles.getAplicaA() != null) {
                documentoExistente.setAplicaA(documentoDetalles.getAplicaA());
            }
            if (documentoDetalles.getObligatorio() != null) {
                documentoExistente.setObligatorio(documentoDetalles.getObligatorio());
            }
            if (documentoDetalles.getDescripcion() != null) {
                documentoExistente.setDescripcion(documentoDetalles.getDescripcion());
            }

            // Persiste los cambios en la base de datos
            return documentoRepository.save(documentoExistente);
        });
    }

    // Operación Eliminar (DELETE): Borra un tipo de documento según su ID y retorna si fue exitoso
    public boolean eliminar(int id) {
        return documentoRepository.findById(id).map(documento -> {
            documentoRepository.delete(documento);
            return true;
        }).orElse(false);
    }
}