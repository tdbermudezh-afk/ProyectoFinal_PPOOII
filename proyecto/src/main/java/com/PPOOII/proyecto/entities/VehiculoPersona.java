package com.PPOOII.proyecto.entities; // Define el paquete de entidades del proyecto

import com.fasterxml.jackson.annotation.JsonBackReference; // Importa JsonBackReference para evitar recursión en JSON
import jakarta.persistence.*; // Importa las anotaciones de persistencia JPA
import jakarta.validation.constraints.Pattern; // Importa Pattern para validar el estado del conductor
import org.hibernate.annotations.Check;
import java.io.Serializable; // Importa Serializable según el Laboratorio 1
import java.time.LocalDate; // Importa LocalDate para la fecha de asociación

@Entity // Marca la clase como entidad persistente de JPA
@Table(name = "vehiculo_personas") // Mapea la tabla vehiculo_personas
@Check(constraints = "estado_conductor IN ('PO', 'EA', 'RO')")
public class VehiculoPersona implements Serializable { // Implementa Serializable según el Laboratorio 1

    private static final long serialVersionUID = 1L; // Identificador de versión de serialización

    @Id // Define la llave primaria de la relación
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configura el autoincremento en BD
    private Long id; // Identificador único de la asignación

    @ManyToOne(fetch = FetchType.EAGER) // Relación muchos a uno hacia Vehiculo
    @JoinColumn(name = "vehiculo_id", nullable = false) // Columna de llave foránea vehiculo_id
    @JsonBackReference // Evita ciclos infinitos al serializar Vehiculo
    private Vehiculo vehiculo; // Vehículo asociado al conductor

    @ManyToOne(fetch = FetchType.EAGER) // Relación muchos a uno hacia Persona
    @JoinColumn(name = "persona_id", nullable = false) // Columna de llave foránea persona_id
    private Persona persona; // Conductor asociado al vehículo

    @Column(name = "fecha_asociacion", nullable = false) // Columna de fecha de asociación
    private LocalDate fechaAsociacion; // Fecha en que se registró la asociación

    @Pattern(regexp = "^(PO|EA|RO)$", message = "Estado inválido (PO: Puede Operar, EA: Espera de Aprobación, RO: Restringido para Operar)") // Restricción de chequeo
    @Column(name = "estado_conductor", nullable = false, length = 2) // Columna estado_conductor
    private String estadoConductor; // Estado del conductor ('PO', 'EA', 'RO')

    public VehiculoPersona() {} // Constructor vacío requerido por JPA

    // Métodos Getter y Setter
    public Long getId() { return id; } // Obtiene el ID de la relación
    public void setId(Long id) { this.id = id; } // Establece el ID de la relación

    public Vehiculo getVehiculo() { return vehiculo; } // Obtiene el vehículo asociado
    public void setVehiculo(Vehiculo vehiculo) { this.vehiculo = vehiculo; } // Establece el vehículo asociado

    public Persona getPersona() { return persona; } // Obtiene la persona conductora
    public void setPersona(Persona persona) { this.persona = persona; } // Establece la persona conductora

    public LocalDate getFechaAsociacion() { return fechaAsociacion; } // Obtiene la fecha de asociación
    public void setFechaAsociacion(LocalDate fechaAsociacion) { this.fechaAsociacion = fechaAsociacion; } // Establece la fecha de asociación

    public String getEstadoConductor() { return estadoConductor; } // Obtiene el estado del conductor
    public void setEstadoConductor(String estadoConductor) { this.estadoConductor = estadoConductor; } // Establece el estado del conductor

    @Transient
    public Integer getVehiculoId() { return vehiculo != null ? vehiculo.getId() : null; }
    @Transient
    public String getVehiculoPlaca() { return vehiculo != null ? vehiculo.getPlaca() : null; }
    @Transient
    public String getVehiculoDescripcion() {
        return vehiculo != null ? (vehiculo.getPlaca() + " - " + vehiculo.getMarca() + " " + vehiculo.getLinea()) : null;
    }

    @Override // Sobrescribe el método toString según el Laboratorio 1
    public String toString() { // Representación textual de VehiculoPersona
        return "VehiculoPersona [id=" + this.id + ", estadoConductor=" + this.estadoConductor + ", fechaAsociacion=" + this.fechaAsociacion + "]"; // Retorna descripción
    } // Fin del método toString
} // Fin de la clase VehiculoPersona
