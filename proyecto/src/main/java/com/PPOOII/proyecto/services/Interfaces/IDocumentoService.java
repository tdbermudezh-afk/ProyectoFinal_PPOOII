package com.PPOOII.proyecto.services.Interfaces; // Define el paquete de interfaces de servicio según el Laboratorio 1

import com.PPOOII.proyecto.entities.Documento; // Importa la entidad paramétrica Documento
import java.util.List; // Importa la interfaz List
import java.util.Optional; // Importa la clase Optional

// Interfaz que define los métodos CRUD para la entidad paramétrica Documento según el Laboratorio 1
public interface IDocumentoService { // Declaración de la interfaz IDocumentoService

    // MÉTODOS CRUD DE DOCUMENTO
    List<Documento> listarTodos(); // Retorna todos los documentos del catálogo

    Optional<Documento> buscarPorId(int id); // Busca un documento paramétrico por su ID

    Documento guardar(Documento documento); // Registra un nuevo documento en el catálogo

    Optional<Documento> actualizar(int id, Documento documentoDetalles); // Actualiza un documento paramétrico existente

    boolean eliminar(int id); // Elimina un documento paramétrico por su ID
} // Fin de la interfaz IDocumentoService

