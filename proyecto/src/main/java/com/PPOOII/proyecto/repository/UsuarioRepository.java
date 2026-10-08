package com.PPOOII.proyecto.repository;

import com.PPOOII.proyecto.entities.Usuario;
import com.PPOOII.proyecto.entities.UsuarioPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UsuarioPK> {

    Optional<Usuario> findByIdLogin(String login);

    @Query("SELECT u FROM Usuario u JOIN FETCH u.persona WHERE u.apikey = :apikey")
    Optional<Usuario> findByApikey(@Param("apikey") String apikey);

    boolean existsByIdLogin(String login);

    boolean existsByApikey(String apikey);
}
