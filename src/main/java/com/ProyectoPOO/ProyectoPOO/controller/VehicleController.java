// Controlador REST para Vehicle
package com.ProyectoPOO.ProyectoPOO.controller;

import com.ProyectoPOO.ProyectoPOO.model.*;
import com.ProyectoPOO.ProyectoPOO.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {
    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Vehicle v) {
        try {
            Vehicle saved = vehicleService.create(v);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
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
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/byPlate")
    public ResponseEntity<?> getByPlate(@RequestParam String plate) {
        try {
            return ResponseEntity.ok(vehicleService.getByPlate(plate));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body("No existe vehículo con la placa: " + plate);
        }
    }

    @GetMapping("/searchByType")
    public ResponseEntity<?> findByType(@RequestParam String type) {
        try {
            VehicleType vt = VehicleType.valueOf(type);
            return ResponseEntity.ok(vehicleService.findByType(vt));
        } catch (Exception e) {
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
            DocumentState ds = DocumentState.valueOf(state);
            return ResponseEntity.ok(vehicleService.findByDocumentState(ds));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Estado de documento inválido");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Vehicle v) {
        try {
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
            Vehicle updated = vehicleService.addDocumentToVehicle(id, vd);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
