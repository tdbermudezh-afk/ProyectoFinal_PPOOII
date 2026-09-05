package com.PPOOII.proyecto.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "documento")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "aplica_a", nullable = false, length = 2)
    private String aplicaA;

    @Column(nullable = false, length = 2)
    private String obligatorio;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @NotBlank(message = "El nombre del documento no puede estar vacío")
    private String nombre1;

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