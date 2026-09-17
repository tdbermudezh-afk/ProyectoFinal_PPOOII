// Paquete de entidades del dominio
package com.PPOOII.proyecto.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

// Entidad JPA que modela la relación de asociación entre un Vehículo y un Documento
// Contiene los atributos complementarios: fechas de expedición, vencimiento y estado
@Entity
@Table(name = "vehiculo_documento")
public class VehiculoDocumento {

    // Identificador único del registro de documento asociado al vehículo
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Relación muchos a uno con Vehículo
    // @JsonBackReference evita ciclos infinitos durante la serialización JSON
    @ManyToOne
    @JoinColumn(name = "vehiculo_id", nullable = false)
    @JsonBackReference
    private Vehiculo vehiculo;

    // Relación muchos a uno con el documento de catálogo parametrizado
    @NotNull(message = "El documento referenciado no puede ser nulo")
    @ManyToOne
    @JoinColumn(name = "documento_id", nullable = false)
    private Documento documento;

    // Fecha en la cual fue expedido o emitido el documento
    @NotNull(message = "La fecha de expedición no puede ser nula")
    @Column(name = "fecha_expedicion", nullable = false)
    private LocalDate fechaExpedicion;

    // Fecha en la cual caduca o vence el documento
    @NotNull(message = "La fecha de vencimiento no puede ser nula")
    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    // Estado del documento para el vehículo: 'Habilitado', 'Vencido' o 'En Verificación'
    // Por requerimiento, el estado inicial al registrar el vehículo es 'En Verificación'
    @NotBlank(message = "El estado no puede estar vacío")
    @Pattern(regexp = "^(Habilitado|Vencido|En Verificación)$", message = "El estado del documento debe ser 'Habilitado', 'Vencido' o 'En Verificación'")
    @Column(nullable = false, length = 20)
    private String estado = "En Verificación";

    @Lob
    @Column(name = "documento_pdf_base64", columnDefinition = "LONGTEXT")
    private String documentoPdfBase64;

    // Constructor vacío por defecto requerido por JPA
    public VehiculoDocumento() {}

    // Métodos Getter y Setter para manipular los atributos de la relación
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Vehiculo getVehiculo() { return vehiculo; }
    public void setVehiculo(Vehiculo vehiculo) { this.vehiculo = vehiculo; }

    public Documento getDocumento() { return documento; }
    public void setDocumento(Documento documento) { this.documento = documento; }

    public LocalDate getFechaExpedicion() { return fechaExpedicion; }
    public void setFechaExpedicion(LocalDate fechaExpedicion) { this.fechaExpedicion = fechaExpedicion; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getDocumentoPdfBase64() {
        return documentoPdfBase64;
    }

    public void setDocumentoPdfBase64(String documentoPdfBase64) {
        this.documentoPdfBase64 = documentoPdfBase64;
    }
}