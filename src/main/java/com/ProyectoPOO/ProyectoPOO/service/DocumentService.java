// Servicio para CRUD de Document
package com.ProyectoPOO.ProyectoPOO.service;

import com.ProyectoPOO.ProyectoPOO.model.Document;
import com.ProyectoPOO.ProyectoPOO.model.DocumentApplicability;
import com.ProyectoPOO.ProyectoPOO.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final DocumentRepository documentRepository;

    public Document create(Document d) {
        return documentRepository.save(d);
    }

    public Document update(Long id, Document d) {
        Document existing = documentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Documento no encontrado"));
        existing.setCode(d.getCode());
        existing.setName(d.getName());
        existing.setApplicability(d.getApplicability());
        existing.setMandatory(d.getMandatory());
        existing.setDescription(d.getDescription());
        return documentRepository.save(existing);
    }

    public Document getById(Long id) {
        return documentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Documento no encontrado"));
    }

    public List<Document> listAll() {
        return documentRepository.findAll();
    }

    public void delete(Long id) {
        documentRepository.deleteById(id);
    }

    public Document findByCode(String code) {
        return documentRepository.findByCode(code).orElse(null);
    }

    public List<Document> findByApplicability(DocumentApplicability app) {
        return documentRepository.findByApplicability(app);
    }
}

