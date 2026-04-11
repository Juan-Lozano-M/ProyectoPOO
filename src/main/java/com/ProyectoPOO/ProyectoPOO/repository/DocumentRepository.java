// Crear repositorio JPA para la entidad Document
package com.ProyectoPOO.ProyectoPOO.repository;

import com.ProyectoPOO.ProyectoPOO.model.Document;
import com.ProyectoPOO.ProyectoPOO.model.DocumentApplicability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    Optional<Document> findByCode(String code);
    List<Document> findByApplicability(DocumentApplicability applicability);
}

