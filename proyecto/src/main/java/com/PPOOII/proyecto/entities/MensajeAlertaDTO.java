package com.PPOOII.proyecto.entities;

import java.io.Serializable;
import java.time.LocalDateTime;

public class MensajeAlertaDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String titulo;
    private String contenido;
    private String placaVehiculo;
    private String tipoEvento; // INFO, ALERTA, VENCIMIENTO, MANTENIMIENTO
    private String fechaHora;

    public MensajeAlertaDTO() {
        this.fechaHora = LocalDateTime.now().toString();
    }

    public MensajeAlertaDTO(String id, String titulo, String contenido, String placaVehiculo, String tipoEvento) {
        this.id = id;
        this.titulo = titulo;
        this.contenido = contenido;
        this.placaVehiculo = placaVehiculo;
        this.tipoEvento = tipoEvento;
        this.fechaHora = LocalDateTime.now().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getPlacaVehiculo() {
        return placaVehiculo;
    }

    public void setPlacaVehiculo(String placaVehiculo) {
        this.placaVehiculo = placaVehiculo;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    @Override
    public String toString() {
        return "MensajeAlertaDTO{" +
                "id='" + id + '\'' +
                ", titulo='" + titulo + '\'' +
                ", contenido='" + contenido + '\'' +
                ", placaVehiculo='" + placaVehiculo + '\'' +
                ", tipoEvento='" + tipoEvento + '\'' +
                ", fechaHora='" + fechaHora + '\'' +
                '}';
    }
}

