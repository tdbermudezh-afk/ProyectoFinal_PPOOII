package com.PPOOII.proyecto.entities; // Define el paquete de entidades del proyecto

import jakarta.persistence.*; // Importa las anotaciones de persistencia JPA
import java.io.Serializable; // Importa Serializable según el Laboratorio 2

@Entity // Marca la clase como una entidad persistente de JPA
@Table(name = "usuarios", uniqueConstraints = {
    @UniqueConstraint(name = "uk_usuario_idpersona", columnNames = "idpersona"),
    @UniqueConstraint(name = "uk_usuario_apikey", columnNames = "apikey")
}) // Mapea la entidad con la tabla usuarios garantizando relación 1:1 estricta por idpersona
public class Usuario implements Serializable { // Implementa Serializable según el Laboratorio 2

    private static final long serialVersionUID = 1L; // Identificador de versión de serialización

    @EmbeddedId // Define la llave primaria compuesta embebida (idPersona, login)
    private UsuarioPK id; // Instancia de la clave primaria compuesta UsuarioPK

    @MapsId("idPersona") // Vincula el atributo idPersona de UsuarioPK con la llave foránea
    @OneToOne(fetch = FetchType.EAGER) // Relación uno a uno con la entidad Persona
    @JoinColumn(name = "idpersona", nullable = false, unique = true) // Columna de llave foránea idpersona única (1:1)
    @com.fasterxml.jackson.annotation.JsonIgnore // Evita recursión infinita al serializar a JSON
    private Persona persona; // Entidad Persona asociada al usuario

    @Column(nullable = false) // Columna obligatoria para la contraseña
    private String password; // Contraseña de acceso del usuario

    @Column(nullable = false, unique = true) // Columna obligatoria y única para la llave de API
    private String apikey; // APIKey asignada al usuario

    public Usuario() {} // Constructor vacío por defecto requerido por JPA

    public UsuarioPK getId() { return id; } // Obtiene la clave primaria compuesta
    public void setId(UsuarioPK id) { this.id = id; } // Establece la clave primaria compuesta

    public Persona getPersona() { return persona; } // Obtiene la persona asociada
    public void setPersona(Persona persona) { this.persona = persona; } // Establece la persona asociada

    public String getPassword() { return password; } // Obtiene el password del usuario
    public void setPassword(String password) { this.password = password; } // Establece el password del usuario

    public String getApikey() { return apikey; } // Obtiene la APIKey del usuario
    public void setApikey(String apikey) { this.apikey = apikey; } // Establece la APIKey del usuario

    @Override // Sobrescribe el método toString
    public String toString() { // Representación textual del objeto Usuario
        return "Usuario [login=" + (id != null ? id.getLogin() : null) + ", apikey=" + apikey + "]"; // Retorna descripción del usuario
    } // Fin del método toString
} // Fin de la clase Usuario
