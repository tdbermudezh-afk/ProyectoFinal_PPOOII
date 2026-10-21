// Paquete de configuración de seguridad y componentes de infraestructura de la aplicación
package com.PPOOII.proyecto.config;

// Importación de las constantes y métodos de firma JWT desde Constans (Laboratorio 2)
import static com.PPOOII.proyecto.config.model.Constans.SUPER_SECRET_KEY;
// Importación de la constante de tiempo de expiración del token (1 mes)
import static com.PPOOII.proyecto.config.model.Constans.TOKEN_EXPIRATION_TIME;
// Importación del método generador de la llave criptográfica
import static com.PPOOII.proyecto.config.model.Constans.getSigningKey;

// Importación del constructor de tokens Jwts de la librería io.jsonwebtoken
import io.jsonwebtoken.Jwts;
// Importación de la enumeración de algoritmos de firma criptográfica
import io.jsonwebtoken.SignatureAlgorithm;
// Importación de la anotación @Configuration de Spring
import org.springframework.context.annotation.Configuration;
// Importación de la interfaz GrantedAuthority de Spring Security
import org.springframework.security.core.GrantedAuthority;
// Importación de la utilidad AuthorityUtils para construir listas de roles
import org.springframework.security.core.authority.AuthorityUtils;

// Importación de la clase Date para definir fecha de emisión y expiración del JWT
import java.util.Date;
// Importación de la interfaz List de Java
import java.util.List;
// Importación de Collectors para transformar flujos de autoridades
import java.util.stream.Collectors;

// Clase de configuración encargada de la generación y firma de tokens JWT según el Laboratorio 2
@Configuration
public class JWTAuthtenticationConfig {

    // Genera un token JWT firmado con HS512 para el usuario autenticado con vigencia de 1 mes
    public String getJWTToken(String username) {
        // Define la lista de roles/autoridades otorgadas al usuario autenticado
        List<GrantedAuthority> grantedAuthorities = AuthorityUtils
                // Otorga el rol ROLE_USER conforme al estándar del Laboratorio 2
                .commaSeparatedStringToAuthorityList("ROLE_USER");

        // Construye el token JWT incluyendo claims, sujeto, fecha de emisión, fecha de expiración y firma
        String token = Jwts
                // Inicia el builder de JJWT
                .builder()
                // Asigna el identificador del emisor del proyecto
                .setId("PPOOII_JWT")
                // Establece el login del usuario como sujeto (subject) del token
                .setSubject(username)
                // Incorpora las autoridades/roles dentro de los claims del payload
                .claim("authorities",
                        // Convierte la lista de GrantedAuthority a una lista de cadenas de texto
                        grantedAuthorities.stream()
                                // Extrae el nombre de cada autoridad
                                .map(GrantedAuthority::getAuthority)
                                // Recolecta los nombres en una lista
                                .collect(Collectors.toList()))
                // Registra la fecha y hora exacta de creación del token
                .setIssuedAt(new Date(System.currentTimeMillis()))
                // Configura la fecha de expiración sumando 1 mes (TOKEN_EXPIRATION_TIME = 2,592,000,000 ms)
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION_TIME))
                // Firma digitalmente el token usando la clave secreta y el algoritmo HMAC-SHA512
                .signWith(getSigningKey(SUPER_SECRET_KEY), SignatureAlgorithm.HS512)
                // Compacta el token en su representación final Base64URL de 3 partes (Header.Payload.Signature)
                .compact();

        // Retorna el token precedido por el prefijo reglamentario 'Bearer '
        return "Bearer " + token;
    }
}

