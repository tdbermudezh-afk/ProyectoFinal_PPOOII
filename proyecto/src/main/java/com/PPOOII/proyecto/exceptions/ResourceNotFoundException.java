// Paquete de excepciones personalizadas
package com.PPOOII.proyecto.exceptions;

// Excepción personalizada de tiempo de ejecución para representar recursos no encontrados (HTTP 404)
public class ResourceNotFoundException extends RuntimeException {

    // Constructor que recibe y propaga el mensaje descriptivo del recurso faltante
    public ResourceNotFoundException(String message) {
        super(message);
    }
}