package com.PPOOII.proyecto.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

@Entity
@Table(name = "vehiculo_personas")
public class VehiculoPersona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehiculo_id", nullable = false)
    private Vehiculo vehiculo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @Column(name = "fecha_asociacion", nullable = false)
    private LocalDate fechaAsociacion;

    @Pattern(regexp = "^(PO|EA|RO)$", message = "Estado inválido (PO: Puede Operar, EA: Espera de Aprobación, RO: Restringido para Operar)")
    @Column(name = "estado_conductor", nullable = false, length = 2)
    private String estadoConductor;

    public VehiculoPersona() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Vehiculo getVehiculo() { return vehiculo; }
    public void setVehiculo(Vehiculo vehiculo) { this.vehiculo = vehiculo; }

    public Persona getPersona() { return persona; }
    public void setPersona(Persona persona) { this.persona = persona; }

    public LocalDate getFechaAsociacion() { return fechaAsociacion; }
    public void setFechaAsociacion(LocalDate fechaAsociacion) { this.fechaAsociacion = fechaAsociacion; }

    public String getEstadoConductor() { return estadoConductor; }
    public void setEstadoConductor(String estadoConductor) { this.estadoConductor = estadoConductor; }
}