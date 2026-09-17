package com.PPOOII.proyecto.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UsuarioPK implements Serializable {

    @Column(name = "idpersona")
    private Long idPersona;

    @Column(length = 100)
    private String login;

    public UsuarioPK() {}

    public UsuarioPK(Long idPersona, String login) {
        this.idPersona = idPersona;
        this.login = login;
    }

    // Getters y Setters
    public Long getIdPersona() { return idPersona; }
    public void setIdPersona(Long idPersona) { this.idPersona = idPersona; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioPK usuarioPK = (UsuarioPK) o;
        return Objects.equals(idPersona, usuarioPK.idPersona) && Objects.equals(login, usuarioPK.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPersona, login);
    }
}