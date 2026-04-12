// Servicio para CRUD de Document
package com.ProyectoPOO.ProyectoPOO.service;

import com.ProyectoPOO.ProyectoPOO.model.Document;
import com.ProyectoPOO.ProyectoPOO.model.DocumentApplicability;
import com.ProyectoPOO.ProyectoPOO.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de dominio para gestionar el CRUD y búsquedas de la entidad {@link Document}.
 */
@Service
@RequiredArgsConstructor // Lombok genera un constructor con los campos final.
public class DocumentService {
    // Repositorio inyectado por constructor para acceso a persistencia.
    private final DocumentRepository documentRepository;

    /**
     * Crea un nuevo documento paramétrico.
     */
    public Document create(Document d) {
        return documentRepository.save(d);
    }

    /**
     * Actualiza un documento existente por id.
     *
     * @throws IllegalArgumentException si el documento no existe.
     */
    public Document update(Long id, Document d) {
        // orElseThrow usa una lambda () -> ... para lanzar excepción si no hay registro.
        Document existing = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado"));

        // Se copian solo los campos permitidos desde el payload recibido.
        existing.setCode(d.getCode());
        existing.setName(d.getName());
        existing.setApplicability(d.getApplicability());
        existing.setMandatory(d.getMandatory());
        existing.setDescription(d.getDescription());

        return documentRepository.save(existing);
    }

    /**
     * Obtiene un documento por su identificador.
     *
     * @throws IllegalArgumentException si el documento no existe.
     */
    public Document getById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado"));
    }

    /**
     * Lista todos los documentos configurados.
     */
    public List<Document> listAll() {
        return documentRepository.findAll();
    }

    /**
     * Elimina un documento por id.
     */
    public void delete(Long id) {
        documentRepository.deleteById(id);
    }

    /**
     * Busca un documento por código; retorna null si no existe.
     */
    public Document findByCode(String code) {
        // Se transforma Optional<Document> a Document para simplificar la respuesta del servicio.
        return documentRepository.findByCode(code).orElse(null);
    }

    /**
     * Lista documentos por aplicabilidad (A, M o AM según el enum).
     */
    public List<Document> findByApplicability(DocumentApplicability app) {
        return documentRepository.findByApplicability(app);
    }
}
