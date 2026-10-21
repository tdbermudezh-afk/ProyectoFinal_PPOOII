// Paquete que almacena los modelos de petición y respuesta para autenticación JWT (Laboratorio 2)
package com.PPOOII.proyecto.config.model;

// Importación de la interfaz Serializable de Java (Laboratorio 2)
import java.io.Serializable;

// Modelo inmutable que encapsula la respuesta con el token JWT generado según el Laboratorio 2
public class JwtResponse implements Serializable {

    // Identificador de versión para la serialización de la clase (Laboratorio 2)
    private static final long serialVersionUID = -8091879091924046844L;

    // Atributo que almacena la cadena del token JWT firmado
    private final String jwttoken;

    // Constructor que recibe el token JWT generado
    public JwtResponse(String jwttoken) {
        // Inicializa el atributo inmutable jwttoken
        this.jwttoken = jwttoken;
    }

    // Retorna el token JWT bajo la propiedad JSON 'token' (estándar del Laboratorio 2)
    public String getToken() {
        // Devuelve la cadena del token JWT
        return this.jwttoken;
    }
}

