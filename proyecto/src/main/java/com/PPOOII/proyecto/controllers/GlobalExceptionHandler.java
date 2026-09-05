package com.PPOOII.proyecto.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex) {
        ex.printStackTrace(); // Imprime la traza completa en la consola
        return new ResponseEntity<>("Error interno: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}