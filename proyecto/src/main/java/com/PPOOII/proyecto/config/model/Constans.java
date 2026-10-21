// Paquete que almacena los modelos y constantes de configuración de seguridad JWT (Laboratorio 2)
package com.PPOOII.proyecto.config.model;

// Importación de la utilidad Decoders de JJWT para decodificar claves en Base64
import io.jsonwebtoken.io.Decoders;
// Importación de la utilidad Keys de JJWT para generar llaves criptográficas HMAC-SHA
import io.jsonwebtoken.security.Keys;

// Importación de StandardCharsets para codificación de caracteres UTF-8
import java.nio.charset.StandardCharsets;
// Importación de la interfaz Key de seguridad de Java
import java.security.Key;

// Clase de constantes criptográficas y de configuración para JWT según el Laboratorio 2
public class Constans {

    // Ruta pública del endpoint de autenticación JWT (Spring Security)
    public static final String LOGIN_URL = "/authenticate";
    // Nombre del encabezado estándar HTTP para el envío del token de autorización
    public static final String HEADER_AUTHORIZACION_KEY = "Authorization";
    // Prefijo estándar para los tokens de tipo portador (Bearer)
    public static final String TOKEN_BEARER_PREFIX = "Bearer ";

    // Información del emisor del token JWT según el Laboratorio 2
    public static final String ISSUER_INFO = "https://www.unibague.edu.co/";
    // Clave secreta maestra en Base64 para firmar los tokens con el algoritmo HS512 (Laboratorio 2)
    public static final String SUPER_SECRET_KEY = "ZnJhc2VzbGFyZ2FzcGFyYWNvbG9jYXJjb21vY2xhdmVlbnVucHJveWVjdG9kZWVtZXBsb3BhcmFqd3Rjb25zcHJpbmdzZWN1cml0eQo=";
    // Tiempo de expiración del token configurado a 1 mes (30 días = 2,592,000,000 milisegundos)
    public static final long TOKEN_EXPIRATION_TIME = 2_592_000_000L;

    // Método que obtiene la llave criptográfica decodificando una cadena en formato Base64
    public static Key getSigningKeyB64(String secret) {
        // Decodifica la cadena Base64 a un arreglo de bytes
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        // Genera y retorna la llave HMAC-SHA a partir de los bytes decodificados
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Método que obtiene la llave criptográfica a partir de los bytes UTF-8 de la clave secreta
    public static Key getSigningKey(String secret) {
        // Convierte la cadena secreta en un arreglo de bytes usando codificación UTF-8
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // Genera y retorna la llave HMAC-SHA a partir de los bytes UTF-8
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

