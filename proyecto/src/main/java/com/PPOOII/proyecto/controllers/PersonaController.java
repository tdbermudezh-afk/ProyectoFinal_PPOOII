package com.PPOOII.proyecto.controllers;

import com.PPOOII.proyecto.entities.Persona;
import com.PPOOII.proyecto.services.PersonaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personas")
public class PersonaController {

    @Autowired
    private PersonaService personaService;

    @PostMapping
    public ResponseEntity<Persona> crearPersona(@Valid @RequestBody Persona persona) {
        Persona nuevaPersona = personaService.crearPersona(persona);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaPersona);
    }

    @GetMapping
    public ResponseEntity<List<Persona>> listarTodas() {
        return ResponseEntity.ok(personaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Persona> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(personaService.obtenerPorId(id));
    }

    @GetMapping("/tipo/{tipoPersona}")
    public ResponseEntity<List<Persona>> listarPorTipo(@PathVariable String tipoPersona) {
        return ResponseEntity.ok(personaService.listarPorTipo(tipoPersona));
    }
}