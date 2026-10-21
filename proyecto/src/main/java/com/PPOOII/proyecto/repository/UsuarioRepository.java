// Paquete de repositorios de acceso a datos JPA
package com.PPOOII.proyecto.repository;

// Importación de la entidad Usuario
import com.PPOOII.proyecto.entities.Usuario;
// Importación de la llave primaria compuesta UsuarioPK
import com.PPOOII.proyecto.entities.UsuarioPK;
// Importación de JpaRepository de Spring Data JPA
import org.springframework.data.jpa.repository.JpaRepository;
// Importación de la anotación @Query para consultas JPQL personalizadas (Laboratorio 2)
import org.springframework.data.jpa.repository.Query;
// Importación de la anotación @Param para enlazar parámetros JPQL (Laboratorio 2)
import org.springframework.data.repository.query.Param;
// Importación de la anotación @Repository de Spring
import org.springframework.stereotype.Repository;

// Importación del contenedor Optional de Java
import java.util.Optional;

// Repositorio JPA para la entidad Usuario con soporte de llave compuesta UsuarioPK (Laboratorio 2)
@Repository("IUsuarioRepository")
public interface UsuarioRepository extends JpaRepository<Usuario, UsuarioPK> {

    // Consulta derivada de Spring Data JPA para buscar un usuario por su login dentro del EmbeddedId
    Optional<Usuario> findByIdLogin(String login);

    // Consulta derivada de Spring Data JPA para buscar el usuario asociado a una persona por idPersona
    Optional<Usuario> findByIdIdPersona(Long idPersona);

    // Consulta JPQL del Laboratorio 2 (pág. 6) que obtiene un Usuario a partir de su login
    @Query("SELECT usr FROM Usuario usr WHERE usr.id.login = :login")
    Usuario findByUsername(@Param("login") String login);

    // Consulta JPQL del Laboratorio 2 (pág. 6) que obtiene un Usuario por login e ID de persona
    @Query("SELECT usr FROM Usuario usr WHERE usr.id.login = :login AND usr.id.idPersona = :persona")
    Usuario getUsuarioANDPersona(@Param("login") String login, @Param("persona") Long persona);

    // Consulta JPQL optimizada con JOIN FETCH para obtener el usuario y su persona asociada mediante la APIKey
    @Query("SELECT u FROM Usuario u JOIN FETCH u.persona WHERE u.apikey = :apikey")
    Optional<Usuario> findByApikey(@Param("apikey") String apikey);

    // Verifica si existe un usuario con el login especificado
    boolean existsByIdLogin(String login);

    // Verifica si existe un usuario con la APIKey especificada
    boolean existsByApikey(String apikey);
}
