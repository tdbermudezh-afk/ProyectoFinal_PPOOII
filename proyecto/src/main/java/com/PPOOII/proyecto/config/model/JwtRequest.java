// Paquete que almacena los modelos de petición y respuesta para autenticación JWT (Laboratorio 2)
package com.PPOOII.proyecto.config.model;

// Importación de @JsonAlias para aceptar tanto 'username' como 'login' en el cuerpo JSON
import com.fasterxml.jackson.annotation.JsonAlias;
// Importación de la interfaz Serializable de Java (Laboratorio 2)
import java.io.Serializable;

// Modelo que representa el cuerpo de la petición de autenticación JWT según el Laboratorio 2
public class JwtRequest implements Serializable {

    // Identificador de versión para la serialización de la clase (Laboratorio 2)
    private static final long serialVersionUID = 5926468583005150707L;

    // Nombre de usuario (login nemotécnico); acepta tanto 'username' como 'login' en el JSON
    @JsonAlias({"login", "username"})
    private String username;

    // Contraseña del usuario administrativo
    private String password;

    // Constructor vacío por defecto requerido para el mapeo JSON de Jackson
    public JwtRequest() {
    }

    // Constructor con parámetros para inicializar el usuario y la contraseña
    public JwtRequest(String username, String password) {
        // Asigna el nombre de usuario
        this.setUsername(username);
        // Asigna la contraseña
        this.setPassword(password);
    }

    // Obtiene el nombre de usuario (login)
    public String getUsername() {
        // Retorna el valor del atributo username
        return this.username;
    }

    // Establece el nombre de usuario (login)
    public void setUsername(String username) {
        // Asigna el valor al atributo username
        this.username = username;
    }

    // Obtiene la contraseña enviada en la petición
    public String getPassword() {
        // Retorna el valor del atributo password
        return this.password;
    }

    // Establece la contraseña de la petición
    public void setPassword(String password) {
        // Asigna el valor al atributo password
        this.password = password;
    }
}

