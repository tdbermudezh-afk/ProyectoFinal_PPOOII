package com.PPOOII.proyecto.services;

import com.PPOOII.proyecto.entities.Documento;
import com.PPOOII.proyecto.repository.DocumentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DocumentoService {

    @Autowired
    private DocumentoRepository documentoRepository;

    public List<Documento> listarTodos() {
        return documentoRepository.findAll();
    }

    public Optional<Documento> buscarPorId(int id) {
        return documentoRepository.findById(id);
    }

    public Documento guardar(Documento documento) {
        return documentoRepository.save(documento);
    }
}