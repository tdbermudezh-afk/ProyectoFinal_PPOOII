package com.PPOOII.proyecto.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Entity
@Table(name = "vehiculos")
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "La placa no puede estar vacia")
    @Size(min = 6, max = 10, message = "La placa debe tener entre 6 y 10 caracteres")
    private String placa;

    @NotBlank(message = "El tipo de vehiculo no puede estar vacio")
    private String tipoVehiculo;

    @NotBlank(message = "La marca no puede estar vacia")
    private String marca;

    @NotBlank(message = "El modelo no puede estar vacio")
    private String modelo;

    private String tipoServicio;
    private String tipoCombustible;
    private Integer capacidadPasajeros;
    private String color;
    private String linea;

    @OneToMany(mappedBy = "vehiculo", cascade = CascadeType.ALL)
    private List<VehiculoDocumento> documentos;

    public Vehiculo() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public String getTipoVehiculo() { return tipoVehiculo; }
    public void setTipoVehiculo(String tipoVehiculo) { this.tipoVehiculo = tipoVehiculo; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

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