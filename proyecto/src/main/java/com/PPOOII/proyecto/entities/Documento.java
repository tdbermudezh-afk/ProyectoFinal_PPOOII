package com.PPOOII.proyecto.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "documento")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "El código del documento no puede estar vacío")
    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @NotBlank(message = "El nombre del documento no puede estar vacío")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El campo aplicaA no puede estar vacío")
    @Pattern(regexp = "^(A|M|AM)$", message = "El campo aplicaA debe ser 'A' (Automóvil), 'M' (Motocicleta) o 'AM' (Ambos)")
    @Column(name = "aplica_a", nullable = false, length = 2)
    private String aplicaA;

    @NotBlank(message = "El campo obligatorio no puede estar vacío")
    @Pattern(regexp = "^(RA|RM|RR)$", message = "El campo obligatorio debe ser 'RA' (Automóvil), 'RM' (Motocicleta) o 'RR' (Requerido para ambos)")
    @Column(nullable = false, length = 2)
    private String obligatorio;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    public Documento() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getAplicaA() { return aplicaA; }
    public void setAplicaA(String aplicaA) { this.aplicaA = aplicaA; }

    public String getObligatorio() { return obligatorio; }
    public void setObligatorio(String obligatorio) { this.obligatorio = obligatorio; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}