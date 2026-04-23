package com.ProyectoPOO.ProyectoPOO.controller;

import com.ProyectoPOO.ProyectoPOO.security.PublicEndpoint;
import com.ProyectoPOO.ProyectoPOO.service.PersonService;
import com.ProyectoPOO.ProyectoPOO.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@PublicEndpoint
public class PublicQueryController {

    private final VehicleService vehicleService;
    private final PersonService personService;

    @GetMapping("/vehicles/expired-documents")
    public ResponseEntity<?> vehiclesWithExpiredDocuments() {
        return ResponseEntity.ok(vehicleService.getVehiclesWithExpiredDocuments());
    }

    @GetMapping("/drivers/can-operate")
    public ResponseEntity<?> driversCanOperate() {
        return ResponseEntity.ok(vehicleService.getDriversThatCanOperate());
    }

    @GetMapping("/vehicles/by-plate")
    public ResponseEntity<?> vehicleByPlate(@RequestParam String plate) {
        try {
            return ResponseEntity.ok(vehicleService.getVehicleDetailsByPlate(plate));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        }
    }

    @GetMapping("/vehicles/expiring-documents")
    public ResponseEntity<?> vehiclesWithExpiringDocuments(@RequestParam Integer days) {
        return ResponseEntity.ok(vehicleService.getVehiclesWithDocumentsExpiringInDays(days));
    }

    @GetMapping("/persons/count-by-type")
    public ResponseEntity<?> totalPersonsByType() {
        return ResponseEntity.ok(personService.countByType());
    }
}

