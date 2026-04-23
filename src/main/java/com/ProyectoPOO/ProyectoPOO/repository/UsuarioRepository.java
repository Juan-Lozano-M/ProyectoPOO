package com.ProyectoPOO.ProyectoPOO.repository;

import com.ProyectoPOO.ProyectoPOO.model.Usuario;
import com.ProyectoPOO.ProyectoPOO.model.UsuarioId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, UsuarioId> {
    @EntityGraph(attributePaths = "persona")
    Optional<Usuario> findByIdLogin(String login);
    boolean existsByIdLogin(String login);
    Optional<Usuario> findByApikey(String apikey);

    @EntityGraph(attributePaths = "persona")
    Optional<Usuario> findByTokenValue(String tokenValue);
}

