package com.ProyectoPOO.ProyectoPOO.repository;

import com.ProyectoPOO.ProyectoPOO.model.PersonType;
import com.ProyectoPOO.ProyectoPOO.model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PersonaRepository extends JpaRepository<Persona, Long> {
    Optional<Persona> findByIdentification(String identification);
    Optional<Persona> findByEmail(String email);
    boolean existsByIdentification(String identification);
    boolean existsByEmail(String email);

    @Query("select p.personType, count(p) from Persona p group by p.personType")
    List<Object[]> countByType();

    List<Persona> findByPersonType(PersonType personType);
}

