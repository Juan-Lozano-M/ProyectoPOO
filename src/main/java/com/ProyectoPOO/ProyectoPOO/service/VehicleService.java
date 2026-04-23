// Servicio para Vehicle: CRUD, búsquedas y agregar documentos
package com.ProyectoPOO.ProyectoPOO.service;

import com.ProyectoPOO.ProyectoPOO.dto.DriverInfo;
import com.ProyectoPOO.ProyectoPOO.dto.VehicleDetailResponse;
import com.ProyectoPOO.ProyectoPOO.dto.VehicleDocumentInfo;
import com.ProyectoPOO.ProyectoPOO.dto.VehicleDocumentUpsertItem;
import com.ProyectoPOO.ProyectoPOO.model.Document;
import com.ProyectoPOO.ProyectoPOO.model.DriverVehicleState;
import com.ProyectoPOO.ProyectoPOO.model.DocumentState;
import com.ProyectoPOO.ProyectoPOO.model.PersonType;
import com.ProyectoPOO.ProyectoPOO.model.Persona;
import com.ProyectoPOO.ProyectoPOO.model.Vehicle;
import com.ProyectoPOO.ProyectoPOO.model.VehicleDriver;
import com.ProyectoPOO.ProyectoPOO.model.VehicleDriverId;
import com.ProyectoPOO.ProyectoPOO.model.VehicleDocument;
import com.ProyectoPOO.ProyectoPOO.model.VehicleType;
import com.ProyectoPOO.ProyectoPOO.repository.DocumentRepository;
import com.ProyectoPOO.ProyectoPOO.repository.PersonaRepository;
import com.ProyectoPOO.ProyectoPOO.repository.VehicleDriverRepository;
import com.ProyectoPOO.ProyectoPOO.repository.VehicleDocumentRepository;
import com.ProyectoPOO.ProyectoPOO.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio de dominio para gestionar vehículos, búsquedas y asociación de documentos.
 */
@Service
@RequiredArgsConstructor // Lombok inyecta dependencias finales mediante constructor.
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final DocumentRepository documentRepository;
    private final VehicleDocumentRepository vehicleDocumentRepository;
    private final PersonaRepository personaRepository;
    private final VehicleDriverRepository vehicleDriverRepository;

    /**
     * Crea un vehículo con al menos un documento asociado.
     * Todo documento asociado inicia con estado EN_VERIFICACION.
     */
    @Transactional
    public Vehicle create(Vehicle vehicle) {
        // Regla de negocio: no se permite crear vehículos sin documentos asociados.
        if (vehicle.getDocuments() == null || vehicle.getDocuments().isEmpty()) {
            throw new IllegalArgumentException("No se puede crear un vehículo sin al menos un documento asociado");
        }

        // Valida referencia a Document existente y completa la relación bidireccional.
        for (VehicleDocument vd : vehicle.getDocuments()) {
            if (vd.getDocument() == null || vd.getDocument().getId() == null) {
                throw new IllegalArgumentException("Cada documento asociado debe referenciar un documento existente (id)");
            }

            Long docId = vd.getDocument().getId();
            Document doc = documentRepository.findById(docId)
                    .orElseThrow(() -> new IllegalArgumentException("Documento referenciado no encontrado id=" + docId));

            vd.setDocument(doc);
            vd.setVehicle(vehicle);
            vd.setState(DocumentState.EN_VERIFICACION);
        }

        // Guarda el vehículo con sus asociaciones ya normalizadas.
        return vehicleRepository.save(vehicle);
    }

    /**
     * Obtiene un vehículo por su identificador.
     */
    public Vehicle getById(Long id) {
        // Consulta directa por id y falla explícitamente si no existe.
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));
    }

    /**
     * Busca un vehículo por placa.
     */
    public Vehicle getByPlate(String plate) {
        // Búsqueda exacta por placa para soportar el requerimiento funcional.
        return vehicleRepository.findByPlate(plate)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado por placa"));
    }

    /**
     * Lista vehículos por tipo (AUTOMOVIL o MOTOCICLETA).
     */
    public List<Vehicle> findByType(VehicleType type) {
        // Retorna todos los vehículos que coinciden con el tipo solicitado.
        return vehicleRepository.findByType(type);
    }

    /**
     * Lista vehículos que tienen asociado un documento por código.
     */
    public List<Vehicle> findByDocumentCode(String code) {
        // Consulta vehículos que comparten un mismo código de documento.
        return vehicleRepository.findByDocumentCode(code);
    }

    /**
     * Lista vehículos que tengan al menos un documento en el estado indicado.
     */
    public List<Vehicle> findByDocumentState(DocumentState state) {
        // Usa DISTINCT para evitar duplicados cuando un vehículo tiene varios documentos.
        return vehicleRepository.findDistinctByDocumentsState(state);
    }

    /**
     * Lista todos los vehículos registrados.
     */
    public List<Vehicle> listAll() {
        // Listado global para vista general o administración.
        return vehicleRepository.findAll();
    }

    /**
     * Actualiza datos básicos del vehículo.
     * No reemplaza la colección de documentos (se maneja en un servicio dedicado).
     */
    @Transactional
    public Vehicle update(Long id, Vehicle v) {
        // Busca el vehículo actual en BD; si no existe, detiene la operación.
        Vehicle existing = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));

        // Copia al registro existente los campos básicos enviados en el request.
        existing.setBrand(v.getBrand());
        existing.setColor(v.getColor());
        existing.setFuelType(v.getFuelType());
        existing.setLine(v.getLine());
        existing.setModel(v.getModel());
        existing.setPassengersCapacity(v.getPassengersCapacity());
        existing.setPlate(v.getPlate());
        existing.setServiceType(v.getServiceType());
        existing.setType(v.getType());

        // Persiste los cambios y retorna la entidad ya actualizada.
        return vehicleRepository.save(existing);
    }

    /**
     * Elimina un vehículo por id.
     */
    public void delete(Long id) {
        // Elimina por id; si no existe, el repositorio no realiza cambios.
        vehicleRepository.deleteById(id);
    }

    /**
     * Agrega un documento a un vehículo existente.
     * El estado inicial del documento asociado siempre será EN_VERIFICACION.
     */
    @Transactional
    public Vehicle addDocumentToVehicle(Long vehicleId, VehicleDocument vd) {
        // Valida que el vehículo destino exista antes de asociar documentos.
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));

        if (vd.getDocument() == null || vd.getDocument().getId() == null) {
            throw new IllegalArgumentException("Se debe proporcionar id del documento a asociar");
        }

        Document document = documentRepository.findById(vd.getDocument().getId())
                .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado"));

        // stream().anyMatch(...) evalúa si ya existe asociación para evitar duplicados.
        boolean already = vehicle.getDocuments().stream()
                .anyMatch(existingVd -> existingVd.getDocument() != null
                        && existingVd.getDocument().getId().equals(document.getId()));
        if (already) {
            throw new IllegalArgumentException("El vehículo ya tiene asociado el documento id=" + document.getId());
        }

        // Reglas mínimas de fechas para evitar inconsistencias.
        if (vd.getIssueDate() == null || vd.getExpiryDate() == null) {
            throw new IllegalArgumentException("Las fechas de expedición y vencimiento son obligatorias");
        }
        if (vd.getExpiryDate().isBefore(vd.getIssueDate())) {
            throw new IllegalArgumentException("La fecha de vencimiento no puede ser anterior a la fecha de expedición");
        }

        // Completa relaciones y fuerza estado inicial según regla del taller.
        vd.setDocument(document);
        vd.setVehicle(vehicle);
        vd.setState(DocumentState.EN_VERIFICACION);

        try {
            // Persiste la asociación y luego actualiza la colección del vehículo.
            VehicleDocument saved = vehicleDocumentRepository.save(vd);
            vehicle.getDocuments().add(saved);
            return vehicleRepository.save(vehicle);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            // Se traduce error técnico de BD a mensaje de negocio entendible.
            throw new IllegalArgumentException("No se pudo asociar el documento: conflicto de integridad (posible duplicado)");
        }
    }

    @Transactional
    public Vehicle upsertVehicleDocuments(Long vehicleId, List<VehicleDocumentUpsertItem> items) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehiculo no encontrado"));

        for (VehicleDocumentUpsertItem item : items) {
            if (item.getDocumentId() == null) {
                throw new IllegalArgumentException("documentId es obligatorio en cada item");
            }
            Document document = documentRepository.findById(item.getDocumentId())
                    .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado id=" + item.getDocumentId()));
            if (item.getIssueDate() == null || item.getExpiryDate() == null) {
                throw new IllegalArgumentException("issueDate y expiryDate son obligatorias");
            }

            Optional<VehicleDocument> existingRelation = vehicle.getDocuments().stream()
                    .filter(vd -> vd.getDocument() != null && vd.getDocument().getId().equals(item.getDocumentId()))
                    .findFirst();

            VehicleDocument vd = existingRelation.orElseGet(() -> VehicleDocument.builder().vehicle(vehicle).document(document).build());
            vd.setVehicle(vehicle);
            vd.setDocument(document);
            vd.setIssueDate(item.getIssueDate());
            vd.setExpiryDate(item.getExpiryDate());
            vd.setPdfBase64(item.getPdfBase64());

            if (vd.getExpiryDate().isBefore(LocalDate.now())) {
                vd.setState(DocumentState.VENCIDO);
            } else {
                vd.setState(DocumentState.HABILITADO);
            }

            VehicleDocument saved = vehicleDocumentRepository.save(vd);
            vehicle.getDocuments().add(saved);
        }

        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public VehicleDriver assignVehicleToDriver(Long personaId, Long vehicleId, LocalDate associationDate, DriverVehicleState state) {
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada"));
        if (persona.getPersonType() != PersonType.C) {
            throw new IllegalArgumentException("Solo se pueden asociar personas tipo conductor");
        }

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehiculo no encontrado"));

        VehicleDriverId id = VehicleDriverId.builder().personaId(personaId).vehicleId(vehicleId).build();
        VehicleDriver relation = vehicleDriverRepository.findById(id)
                .orElseGet(() -> VehicleDriver.builder()
                        .id(id)
                        .persona(persona)
                        .vehicle(vehicle)
                        .build());

        relation.setAssociationDate(associationDate == null ? LocalDate.now() : associationDate);
        relation.setState(state == null ? DriverVehicleState.EA : state);

        return vehicleDriverRepository.save(relation);
    }

    @Transactional
    public VehicleDriver changeDriverState(Long personaId, Long vehicleId, DriverVehicleState state) {
        if (state == null) {
            throw new IllegalArgumentException("El estado del conductor es obligatorio");
        }
        VehicleDriverId id = VehicleDriverId.builder().personaId(personaId).vehicleId(vehicleId).build();
        VehicleDriver relation = vehicleDriverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Asociacion conductor-vehiculo no encontrada"));
        relation.setState(state);
        return vehicleDriverRepository.save(relation);
    }

    public List<Vehicle> getVehiclesWithExpiredDocuments() {
        return vehicleRepository.findWithExpiredDocuments(LocalDate.now());
    }

    public List<Persona> getDriversThatCanOperate() {
        return vehicleDriverRepository.findByState(DriverVehicleState.PO).stream()
                .map(VehicleDriver::getPersona)
                .distinct()
                .collect(Collectors.toList());
    }

    public VehicleDetailResponse getVehicleDetailsByPlate(String plate) {
        Vehicle vehicle = getByPlate(plate);
        List<DriverInfo> drivers = vehicle.getDrivers().stream().map(d -> DriverInfo.builder()
                .personId(d.getPersona().getId())
                .identification(d.getPersona().getIdentification())
                .names(d.getPersona().getNames())
                .lastNames(d.getPersona().getLastNames())
                .state(d.getState())
                .associationDate(d.getAssociationDate())
                .build()).toList();

        List<VehicleDocumentInfo> documents = vehicle.getDocuments().stream().map(vd -> VehicleDocumentInfo.builder()
                .documentId(vd.getDocument().getId())
                .code(vd.getDocument().getCode())
                .name(vd.getDocument().getName())
                .issueDate(vd.getIssueDate())
                .expiryDate(vd.getExpiryDate())
                .state(vd.getState())
                .build()).toList();

        return VehicleDetailResponse.builder()
                .vehicle(vehicle)
                .drivers(drivers)
                .documents(documents)
                .build();
    }

    public List<Vehicle> getVehiclesWithDocumentsExpiringInDays(Integer days) {
        int safeDays = days == null || days < 0 ? 0 : days;
        return vehicleRepository.findWithDocumentsExpiringBetween(LocalDate.now(), LocalDate.now().plusDays(safeDays));
    }
}
