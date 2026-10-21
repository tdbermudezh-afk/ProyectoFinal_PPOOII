// Paquete de entidades persistentes del proyecto
package com.PPOOII.proyecto.entities;

// Importación de la anotación @Column de Jakarta Persistence
import jakarta.persistence.Column;
// Importación de la anotación @Embeddable para llaves primarias compuestas (Laboratorio 2)
import jakarta.persistence.Embeddable;
// Importación de la interfaz Serializable requerida por JPA para llaves compuestas (Laboratorio 2)
import java.io.Serializable;
// Importación de la clase utilitaria Objects para equals y hashCode
import java.util.Objects;

// Clase que representa la llave primaria compuesta (idpersona + login) de la entidad Usuario (Laboratorio 2, pág. 3)
@Embeddable
public class UsuarioPK implements Serializable {

    // Identificador de versión de serialización según el Laboratorio 2 (pág. 3)
    private static final long serialVersionUID = 1L;

    // Columna que almacena el ID de la persona asociada en la llave compuesta
    @Column(name = "idpersona")
    private Long idPersona;

    // Columna que almacena el login nemotécnico del usuario en la llave compuesta
    @Column(length = 100)
    private String login;

    // Constructor vacío por defecto requerido por JPA
    public UsuarioPK() {}

    // Constructor con parámetros para inicializar la llave compuesta
    public UsuarioPK(Long idPersona, String login) {
        // Asigna el ID de la persona
        this.idPersona = idPersona;
        // Asigna el login del usuario
        this.login = login;
    }

    // Obtiene el ID de la persona de la llave compuesta
    public Long getIdPersona() { return idPersona; }
    // Establece el ID de la persona en la llave compuesta
    public void setIdPersona(Long idPersona) { this.idPersona = idPersona; }

    // Obtiene el login del usuario de la llave compuesta
    public String getLogin() { return login; }
    // Establece el login del usuario en la llave compuesta
    public void setLogin(String login) { this.login = login; }

    // Compara dos instancias de UsuarioPK por sus atributos idPersona y login
    @Override
    public boolean equals(Object o) {
        // Si es la misma referencia de memoria, retorna verdadero
        if (this == o) return true;
        // Si el objeto es nulo o de otra clase, retorna falso
        if (o == null || getClass() != o.getClass()) return false;
        // Realiza el casting a UsuarioPK
        UsuarioPK usuarioPK = (UsuarioPK) o;
        // Compara igualdad tanto en idPersona como en login
        return Objects.equals(idPersona, usuarioPK.idPersona) && Objects.equals(login, usuarioPK.login);
    }

    // Calcula el código hash de la llave compuesta a partir de idPersona y login
    @Override
    public int hashCode() {
        // Genera el hash combinando ambos campos de la llave primaria
        return Objects.hash(idPersona, login);
    }

    // Representación en cadena de texto de la llave compuesta UsuarioPK (Laboratorio 1 y 2)
    @Override
    public String toString() {
        // Retorna los valores de idPersona y login formateados
        return "UsuarioPK [idPersona=" + idPersona + ", login=" + login + "]";
    }
}