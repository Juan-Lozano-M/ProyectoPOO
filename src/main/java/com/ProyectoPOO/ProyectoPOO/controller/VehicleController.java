// Controlador REST para Vehicle
package com.ProyectoPOO.ProyectoPOO.controller;

import com.ProyectoPOO.ProyectoPOO.dto.AssignVehicleRequest;
import com.ProyectoPOO.ProyectoPOO.dto.ChangeDriverStateRequest;
import com.ProyectoPOO.ProyectoPOO.dto.VehicleDocumentsBatchRequest;
import com.ProyectoPOO.ProyectoPOO.model.*;
import com.ProyectoPOO.ProyectoPOO.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Expone endpoints REST con serialización JSON automática.
@RequestMapping("/api/vehicles") // Ruta base del recurso vehículos.
@RequiredArgsConstructor // Lombok crea constructor para inyectar VehicleService.
public class VehicleController {
    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Vehicle v) {
        try {
            // Convierte body JSON a Vehicle y delega reglas de negocio al servicio.
            Vehicle saved = vehicleService.create(v);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            // Error de validación de entrada/regla de negocio -> 400.
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Vehicle>> listAll() {
        return ResponseEntity.ok(vehicleService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(vehicleService.getById(id));
        } catch (IllegalArgumentException e) {
            // Si el recurso no existe, responde 404 sin cuerpo.
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/byPlate")
    public ResponseEntity<?> getByPlate(@RequestParam String plate) {
        try {
            // @RequestParam toma placa desde query string: /byPlate?plate=AAA999.
            return ResponseEntity.ok(vehicleService.getByPlate(plate));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body("No existe vehículo con la placa: " + plate);
        }
    }

    @GetMapping("/searchByType")
    public ResponseEntity<?> findByType(@RequestParam String type) {
        try {
            // valueOf parsea String a enum VehicleType.
            VehicleType vt = VehicleType.valueOf(type);
            return ResponseEntity.ok(vehicleService.findByType(vt));
        } catch (Exception e) {
            // Si el enum es inválido o hay error de parseo, retorna 400.
            return ResponseEntity.badRequest().body("Tipo de vehículo inválido");
        }
    }

    @GetMapping("/searchByDocumentCode")
    public ResponseEntity<List<Vehicle>> findByDocumentCode(@RequestParam String code) {
        return ResponseEntity.ok(vehicleService.findByDocumentCode(code));
    }

    @GetMapping("/searchByDocumentState")
    public ResponseEntity<?> findByDocumentState(@RequestParam String state) {
        try {
            // Convierte el parámetro textual al enum de estado de documento.
            DocumentState ds = DocumentState.valueOf(state);
            return ResponseEntity.ok(vehicleService.findByDocumentState(ds));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Estado de documento inválido");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Vehicle v) {
        try {
            // Update usa id de URL + datos del body.
            return ResponseEntity.ok(vehicleService.update(id, v));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/documents")
    public ResponseEntity<?> addDocument(@PathVariable Long id, @RequestBody VehicleDocument vd) {
        try {
            // Endpoint de asociación: agrega documento a vehículo existente.
            Vehicle updated = vehicleService.addDocumentToVehicle(id, vd);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/documents/batch")
    public ResponseEntity<?> upsertDocuments(@PathVariable Long id, @RequestBody VehicleDocumentsBatchRequest request) {
        try {
            if (request == null || request.getDocuments() == null || request.getDocuments().isEmpty()) {
                return ResponseEntity.badRequest().body("Debe enviar al menos un documento");
            }
            return ResponseEntity.ok(vehicleService.upsertVehicleDocuments(id, request.getDocuments()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/drivers/{personaId}/vehicles")
    public ResponseEntity<?> assignVehicleToDriver(@PathVariable Long personaId, @RequestBody AssignVehicleRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(vehicleService.assignVehicleToDriver(personaId, request.getVehicleId(), request.getAssociationDate(), request.getState()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/drivers/{personaId}/vehicles/{vehicleId}/state")
    public ResponseEntity<?> changeDriverState(@PathVariable Long personaId,
                                               @PathVariable Long vehicleId,
                                               @RequestBody ChangeDriverStateRequest request) {
        try {
            return ResponseEntity.ok(vehicleService.changeDriverState(personaId, vehicleId, request.getState()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
