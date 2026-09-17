// Paquete de excepciones personalizadas y modelos de error
package com.PPOOII.proyecto.exceptions;

import java.time.LocalDateTime;

// Estructura estándar para devolver respuestas de error uniformes en formato JSON
public class ErrorResponse {

    // Código numérico del estado HTTP (por ejemplo 400, 404, 500)
    private int status;

    // Mensaje descriptivo con el detalle del error ocurrido
    private String message;

    // Marca de tiempo exacta en que se generó el error
    private LocalDateTime timestamp;

    // Constructor que inicializa los datos del error y toma la fecha/hora actual
    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // Obtiene el código de estado HTTP
    public int getStatus() { return status; }

    // Obtiene el mensaje del error
    public String getMessage() { return message; }

    // Obtiene la marca de tiempo del error
    public LocalDateTime getTimestamp() { return timestamp; }
}