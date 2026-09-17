package com.PPOOII.proyecto.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @EmbeddedId
    private UsuarioPK id;

    @MapsId("idPersona")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idpersona", nullable = false)
    private Persona persona;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String apikey;

    public Usuario() {}

    public UsuarioPK getId() { return id; }
    public void setId(UsuarioPK id) { this.id = id; }

    public Persona getPersona() { return persona; }
    public void setPersona(Persona persona) { this.persona = persona; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getApikey() { return apikey; }
    public void setApikey(String apikey) { this.apikey = apikey; }
}