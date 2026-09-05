package com.PPOOII.proyecto.services;

import com.PPOOII.proyecto.entities.Documento;
import com.PPOOII.proyecto.repository.DocumentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DocumentoService {

    private final DocumentoRepository documentoRepository;

    public DocumentoService(DocumentoRepository documentoRepository) {
        this.documentoRepository = documentoRepository;
    }

    public List<Documento> listarTodos() {
        return documentoRepository.findAll();
    }

    public Optional<Documento> buscarPorId(int id) {
        return documentoRepository.findById(id);
    }

    public Documento guardar(Documento documento) {
        return documentoRepository.save(documento);
    }

    public Optional<Documento> actualizar(int id, Documento documentoDetalles) {
        return documentoRepository.findById(id).map(documentoExistente -> {
            documentoExistente.setCodigo(documentoDetalles.getCodigo());
            documentoExistente.setNombre(documentoDetalles.getNombre());
            documentoExistente.setAplicaA(documentoDetalles.getAplicaA());
            documentoExistente.setObligatorio(documentoDetalles.getObligatorio());
            documentoExistente.setDescripcion(documentoDetalles.getDescripcion());
            return documentoRepository.save(documentoExistente);
        });
    }

    public boolean eliminar(int id) {
        return documentoRepository.findById(id).map(documento -> {
            documentoRepository.delete(documento);
            return true;
        }).orElse(false);
    }
}