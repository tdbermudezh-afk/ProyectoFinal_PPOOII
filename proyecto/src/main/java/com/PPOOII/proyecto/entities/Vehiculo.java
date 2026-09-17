// Paquete de entidades del dominio
package com.PPOOII.proyecto.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

// Entidad JPA que representa la tabla 'vehiculo' en la base de datos MySQL
@Entity
@Table(name = "vehiculo")
public class Vehiculo {

    // Identificador único del vehículo (Clave primaria autoincremental)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Placa del vehículo: debe tener exactamente 6 caracteres y ser única
    @NotBlank(message = "La placa no puede estar vacía")
    @Size(min = 6, max = 6, message = "La placa debe tener exactamente 6 caracteres")
    @Column(nullable = false, unique = true, length = 6)
    private String placa;

    // Tipo de vehículo: restringido a 'Automóvil' o 'Motocicleta'
    @NotBlank(message = "El tipo de vehículo no puede estar vacío")
    @Pattern(regexp = "^(Automóvil|Motocicleta)$", message = "El tipo de vehículo debe ser 'Automóvil' o 'Motocicleta'")
    @Column(name = "tipo_vehiculo", nullable = false, length = 20)
    private String tipoVehiculo;

    // Marca del fabricante (ejemplo: Toyota, Mazda, Yamaha)
    @NotBlank(message = "La marca no puede estar vacía")
    @Column(nullable = false, length = 50)
    private String marca;

    // Modelo de fabricación: numérico y de tipo entero (ejemplo: 2022)
    @NotNull(message = "El modelo no puede estar vacío y debe ser numérico")
    @Min(value = 1900, message = "El modelo debe ser un año numérico entero válido")
    @Column(nullable = false)
    private Integer modelo;

    // Tipo de servicio: restringido a 'Pu' (Público) o 'Pr' (Privado)
    @NotBlank(message = "El tipo de servicio no puede estar vacío")
    @Pattern(regexp = "^(Pu|Pr)$", message = "El tipo de servicio debe ser 'Pu' (Público) o 'Pr' (Privado)")
    @Column(name = "tipo_servicio", nullable = false, length = 2)
    private String tipoServicio;

    // Tipo de combustible: restringido a Gasolina, Gas o Disel
    @NotBlank(message = "El tipo de combustible no puede estar vacío")
    @Pattern(regexp = "^(Gasolina|Gas|Disel|Diesel)$", message = "El tipo de combustible debe ser 'Gasolina', 'Gas' o 'Disel'")
    @Column(name = "tipo_combustible", nullable = false, length = 20)
    private String tipoCombustible;

    // Capacidad máxima de pasajeros: número entero positivo
    @NotNull(message = "La capacidad de pasajeros no puede estar vacía")
    @Min(value = 1, message = "La capacidad de pasajeros debe ser al menos 1")
    @Column(name = "capacidad_pasajeros", nullable = false)
    private Integer capacidadPasajeros;

    // Código de color del vehículo en formato hexadecimal (ejemplo: #FF0000)
    @NotBlank(message = "El color no puede estar vacío")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "El color debe tener formato hexadecimal válido (ejemplo: #FF0000)")
    @Column(nullable = false, length = 7)
    private String color;

    // Línea o referencia específica del modelo (ejemplo: Fortuner SW, Corolla, MT-09)
    @NotBlank(message = "La línea no puede estar vacía")
    @Column(nullable = false, length = 50)
    private String linea;

    // Relación uno a muchos con la tabla intermedia 'vehiculo_documento'
    // CascadeType.ALL permite guardar o eliminar los documentos asociados junto con el vehículo
    @OneToMany(mappedBy = "vehiculo", cascade = CascadeType.ALL)
    private List<VehiculoDocumento> documentos;

    // Constructor vacío por defecto requerido por JPA
    public Vehiculo() {
    }

    // Métodos Getter y Setter para acceder y modificar las propiedades del vehículo
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public String getTipoVehiculo() { return tipoVehiculo; }
    public void setTipoVehiculo(String tipoVehiculo) { this.tipoVehiculo = tipoVehiculo; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public Integer getModelo() { return modelo; }
    public void setModelo(Integer modelo) { this.modelo = modelo; }

    public String getTipoServicio() { return tipoServicio; }
    public void setTipoServicio(String tipoServicio) { this.tipoServicio = tipoServicio; }

    public String getTipoCombustible() { return tipoCombustible; }
    public void setTipoCombustible(String tipoCombustible) { this.tipoCombustible = tipoCombustible; }

    public Integer getCapacidadPasajeros() { return capacidadPasajeros; }
    public void setCapacidadPasajeros(Integer capacidadPasajeros) { this.capacidadPasajeros = capacidadPasajeros; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getLinea() { return linea; }
    public void setLinea(String linea) { this.linea = linea; }

    public List<VehiculoDocumento> getDocumentos() { return documentos; }
    public void setDocumentos(List<VehiculoDocumento> documentos) { this.documentos = documentos; }
}