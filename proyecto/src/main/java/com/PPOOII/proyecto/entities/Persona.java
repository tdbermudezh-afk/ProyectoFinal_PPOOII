package com.PPOOII.proyecto.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "personas")
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La identificación es obligatoria")
    @Column(nullable = false, unique = true)
    private String identificacion;

    @NotBlank(message = "El tipo de identificación es obligatorio")
    @Pattern(regexp = "^(CC|CE|PASAPORTE)$", message = "Tipo de identificación inválido (Permitidos: CC, CE, PASAPORTE)")
    @Column(name = "tipo_identificacion", nullable = false)
    private String tipoIdentificacion;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String nombres;

    @NotBlank(message = "El apellido es obligatorio")
    @Column(nullable = false)
    private String apellidos;

    @Email(message = "Formato de correo electrónico inválido")
    @NotBlank(message = "El correo es obligatorio")
    @Column(nullable = false, unique = true)
    private String correo;

    @NotBlank(message = "El tipo de persona es obligatorio")
    @Pattern(regexp = "^[CA]$", message = "Tipo de persona inválido (C - Conductor, A - Administrativo)")
    @Column(name = "tipo_persona", nullable = false, length = 1)
    private String tipoPersona;

    public Persona() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIdentificacion() { return identificacion; }
    public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }

    public String getTipoIdentificacion() { return tipoIdentificacion; }
    public void setTipoIdentificacion(String tipoIdentificacion) { this.tipoIdentificacion = tipoIdentificacion; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTipoPersona() { return tipoPersona; }
    public void setTipoPersona(String tipoPersona) { this.tipoPersona = tipoPersona; }
}