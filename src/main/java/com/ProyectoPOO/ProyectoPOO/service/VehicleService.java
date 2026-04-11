// Servicio para Vehicle: CRUD, búsquedas y agregar documentos
package com.ProyectoPOO.ProyectoPOO.service;

import com.ProyectoPOO.ProyectoPOO.model.*;
import com.ProyectoPOO.ProyectoPOO.repository.DocumentRepository;
import com.ProyectoPOO.ProyectoPOO.repository.VehicleDocumentRepository;
import com.ProyectoPOO.ProyectoPOO.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final DocumentRepository documentRepository;
    private final VehicleDocumentRepository vehicleDocumentRepository;

    @Transactional
    public Vehicle create(Vehicle vehicle) {
        if (vehicle.getDocuments() == null || vehicle.getDocuments().isEmpty()) {
            throw new IllegalArgumentException("No se puede crear un vehículo sin al menos un documento asociado");
        }

        // Asociar documentos existentes y forzar estado EN_VERIFICACION
        for (VehicleDocument vd : vehicle.getDocuments()) {
            if (vd.getDocument() == null || vd.getDocument().getId() == null) {
                throw new IllegalArgumentException("Cada documento asociado debe referenciar un documento existente (id)");
            }
            Long docId = vd.getDocument().getId();
            Document doc = documentRepository.findById(docId).orElseThrow(() -> new IllegalArgumentException("Documento referenciado no encontrado id=" + docId));
            vd.setDocument(doc);
            vd.setVehicle(vehicle);
            vd.setState(DocumentState.EN_VERIFICACION);
        }

        return vehicleRepository.save(vehicle);
    }

    public Vehicle getById(Long id) {
        return vehicleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));
    }

    public Vehicle getByPlate(String plate) {
        return vehicleRepository.findByPlate(plate).orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado por placa"));
    }

    public List<Vehicle> findByType(VehicleType type) {
        return vehicleRepository.findByType(type);
    }

    public List<Vehicle> findByDocumentCode(String code) {
        return vehicleRepository.findByDocumentCode(code);
    }

    public List<Vehicle> findByDocumentState(DocumentState state) {
        return vehicleRepository.findDistinctByDocumentsState(state);
    }

    public List<Vehicle> listAll() {
        return vehicleRepository.findAll();
    }

    @Transactional
    public Vehicle update(Long id, Vehicle v) {
        Vehicle existing = vehicleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));
        existing.setBrand(v.getBrand());
        existing.setColor(v.getColor());
        existing.setFuelType(v.getFuelType());
        existing.setLine(v.getLine());
        existing.setModel(v.getModel());
        existing.setPassengersCapacity(v.getPassengersCapacity());
        existing.setPlate(v.getPlate());
        existing.setServiceType(v.getServiceType());
        existing.setType(v.getType());
        // No reemplazar documentos por defecto aquí; operación para agregar documentos existe
        return vehicleRepository.save(existing);
    }

    public void delete(Long id) {
        vehicleRepository.deleteById(id);
    }

    @Transactional
    public Vehicle addDocumentToVehicle(Long vehicleId, VehicleDocument vd) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));
        if (vd.getDocument() == null || vd.getDocument().getId() == null) {
            throw new IllegalArgumentException("Se debe proporcionar id del documento a asociar");
        }
        Document document = documentRepository.findById(vd.getDocument().getId()).orElseThrow(() -> new IllegalArgumentException("Documento no encontrado"));

        // Validar que no esté ya asociado
        boolean already = vehicle.getDocuments().stream()
                .anyMatch(existingVd -> existingVd.getDocument() != null && existingVd.getDocument().getId().equals(document.getId()));
        if (already) {
            throw new IllegalArgumentException("El vehículo ya tiene asociado el documento id=" + document.getId());
        }

        // Validar fechas
        if (vd.getIssueDate() == null || vd.getExpiryDate() == null) {
            throw new IllegalArgumentException("Las fechas de expedición y vencimiento son obligatorias");
        }
        if (vd.getExpiryDate().isBefore(vd.getIssueDate())) {
            throw new IllegalArgumentException("La fecha de vencimiento no puede ser anterior a la fecha de expedición");
        }

        vd.setDocument(document);
        vd.setVehicle(vehicle);
        vd.setState(DocumentState.EN_VERIFICACION);
        try {
            VehicleDocument saved = vehicleDocumentRepository.save(vd);
            vehicle.getDocuments().add(saved);
            return vehicleRepository.save(vehicle);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("No se pudo asociar el documento: conflicto de integridad (posible duplicado)");
        }
    }
}
