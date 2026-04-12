// Controlador REST para Document
package com.ProyectoPOO.ProyectoPOO.controller;

import com.ProyectoPOO.ProyectoPOO.model.Document;
import com.ProyectoPOO.ProyectoPOO.model.DocumentApplicability;
import com.ProyectoPOO.ProyectoPOO.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Marca la clase como controlador REST (retorna JSON por defecto).
@RequestMapping("/api/documents") // Prefijo base para todos los endpoints de documentos.
@RequiredArgsConstructor // Lombok genera constructor con dependencias final.
public class DocumentController {
    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<Document> create(@RequestBody Document d) {
        // @RequestBody convierte el JSON del request en un objeto Document.
        Document saved = documentService.create(d);
        // 201 Created indica creación exitosa de recurso.
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<Document>> listAll() {
        // ResponseEntity.ok(...) retorna HTTP 200 con el cuerpo.
        return ResponseEntity.ok(documentService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getById(@PathVariable Long id) {
        // @PathVariable toma el valor {id} desde la URL.
        return ResponseEntity.ok(documentService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Document> update(@PathVariable Long id, @RequestBody Document d) {
        // Combina id por ruta + datos por body para actualizar registro existente.
        return ResponseEntity.ok(documentService.update(id, d));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentService.delete(id);
        // 204 No Content: operación exitosa sin cuerpo de respuesta.
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Document> findByCode(@RequestParam String code) {
        // @RequestParam lee query params, por ejemplo /search?code=SOAT.
        Document d = documentService.findByCode(code);
        // if corto: si no existe, retorna 404; si existe, 200 con el recurso.
        if (d == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(d);
    }

    @GetMapping("/searchByApplicability")
    public ResponseEntity<List<Document>> findByApplicability(@RequestParam String app) {
        DocumentApplicability applicability;
        try {
            // valueOf convierte String a enum; falla si el valor no coincide exactamente.
            applicability = DocumentApplicability.valueOf(app);
        } catch (Exception e) {
            // 400 Bad Request para parámetros inválidos del cliente.
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(documentService.findByApplicability(applicability));
    }
}
