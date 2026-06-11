package com.ProyectoPOO.ProyectoPOO.service;

import com.ProyectoPOO.ProyectoPOO.dto.TrayectoRequest;
import com.ProyectoPOO.ProyectoPOO.dto.TrayectoResponse;
import com.ProyectoPOO.ProyectoPOO.model.*;
import com.ProyectoPOO.ProyectoPOO.repository.PersonaRepository;
import com.ProyectoPOO.ProyectoPOO.repository.TrayectoRepository;
import com.ProyectoPOO.ProyectoPOO.repository.VehicleRepository;
import com.ProyectoPOO.ProyectoPOO.repository.VehicleDriverRepository;
import com.ProyectoPOO.ProyectoPOO.repository.VehicleDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar trayectos.
 * Valida que:
 * - El conductor pueda operar (estado PO en la relación conductor-vehículo)
 * - Los documentos del vehículo estén habilitados
 */
@Service
@RequiredArgsConstructor
public class TrayectoService {
    private final TrayectoRepository trayectoRepository;
    private final PersonaRepository personaRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleDriverRepository vehicleDriverRepository;
    private final VehicleDocumentRepository vehicleDocumentRepository;
    
    /**
     * Crea un nuevo trayecto con todas las validaciones necesarias
     */
    @Transactional
    public TrayectoResponse createTrayecto(TrayectoRequest request, String registeredByLogin) {
        // 1. Validar que la persona existe y es un conductor (tipo 'C')
        Persona conductor = personaRepository.findById(request.getPersonaId())
                .orElseThrow(() -> new IllegalArgumentException("Conductor no encontrado"));
        
        if (!conductor.getPersonType().equals(PersonType.C)) {
            throw new IllegalArgumentException("La persona debe ser de tipo conductor (C)");
        }
        
        // 2. Validar que el vehículo existe
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));
        
        // 3. Validar que el conductor pueda operar con este vehículo (estado PO)
        VehicleDriverId driverId = VehicleDriverId.builder()
                .vehicleId(vehicle.getId())
                .personaId(conductor.getId())
                .build();
        
        VehicleDriver driverVehicleRelation = vehicleDriverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "El conductor no está asociado al vehículo"));
        
        if (!driverVehicleRelation.getState().equals(DriverVehicleState.PO)) {
            throw new IllegalArgumentException(
                    "El conductor no puede operar este vehículo. Estado actual: " + 
                    driverVehicleRelation.getState());
        }
        
        // 4. Validar que todos los documentos del vehículo estén HABILITADO
        List<VehicleDocument> vehicleDocuments = vehicleDocumentRepository.findByVehicleId(vehicle.getId());
        boolean allDocumentsEnabled = vehicleDocuments.stream()
                .allMatch(vd -> vd.getState().equals(DocumentState.HABILITADO));
        
        if (!allDocumentsEnabled) {
            throw new IllegalArgumentException(
                    "No todos los documentos del vehículo están habilitados. " +
                    "Un trayecto solo puede realizarse si todos los documentos están en estado HABILITADO");
        }
        
        // 5. Crear el trayecto
        Trayecto trayecto = Trayecto.builder()
                .persona(conductor)
                .vehicle(vehicle)
                .routeCode(request.getRouteCode())
                .location(request.getLocation())
                .stopOrder(request.getStopOrder())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .registeredByLogin(registeredByLogin)
                .createdAt(LocalDateTime.now())
                .build();
        
        Trayecto savedTrayecto = trayectoRepository.save(trayecto);
        
        return mapToResponse(savedTrayecto);
    }
    
    /**
     * Obtiene todos los trayectos de una ruta, ordenados por parada
     */
    public List<TrayectoResponse> getTrayectosByRouteCode(String routeCode) {
        return trayectoRepository.findByRouteCodeOrderByStopOrder(routeCode).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los trayectos del sistema, ordenados por ruta y parada.
     */
    public List<TrayectoResponse> getAllTrayectos() {
        return trayectoRepository.findAll().stream()
                .sorted(java.util.Comparator
                        .comparing(Trayecto::getRouteCode, java.util.Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(Trayecto::getStopOrder, java.util.Comparator.nullsLast(Integer::compareTo)))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los códigos de ruta de un conductor específico
     */
    public List<String> getRouteCodesByPersona(Long personaId) {
        personaRepository.findById(personaId)
                .orElseThrow(() -> new IllegalArgumentException("Conductor no encontrado"));
        return trayectoRepository.findRouteCodesByPersona(personaId);
    }

    /**
     * Obtiene los códigos de ruta agrupados por el número de identificación del conductor.
     * Busca la persona por su número de identificación (identification) y retorna los códigos de ruta.
     */
    public List<String> getRouteCodesByPersonaIdentification(String identification) {
        Persona persona = personaRepository.findByIdentification(identification)
                .orElseThrow(() -> new IllegalArgumentException("Conductor no encontrado por identificación"));
        if (!persona.getPersonType().equals(PersonType.C)) {
            throw new IllegalArgumentException("La persona no es un conductor");
        }
        return trayectoRepository.findRouteCodesByPersona(persona.getId());
    }
    
    /**
     * Obtiene los códigos de ruta de un vehículo específico
     */
    public List<String> getRouteCodesByVehicle(Long vehicleId) {
        vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));
        return trayectoRepository.findRouteCodesByVehicle(vehicleId);
    }

    /**
     * Obtiene los códigos de ruta para un vehículo indicado por su placa y los conductores asociados
     * agrupados por código de ruta.
     */
    public java.util.List<com.ProyectoPOO.ProyectoPOO.dto.RouteDriversResponse> getRoutesAndDriversByVehiclePlate(String plate) {
        Vehicle vehicle = vehicleRepository.findByPlate(plate)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado por placa"));

        java.util.List<String> routeCodes = trayectoRepository.findRouteCodesByVehicle(vehicle.getId());

        java.util.List<com.ProyectoPOO.ProyectoPOO.dto.RouteDriversResponse> result = new java.util.ArrayList<>();

        for (String rc : routeCodes) {
            java.util.List<Trayecto> trayectos = trayectoRepository.findByRouteCodeAndVehicleId(rc, vehicle.getId());
            java.util.List<com.ProyectoPOO.ProyectoPOO.dto.DriverInfo> drivers = new java.util.ArrayList<>();
            for (Trayecto t : trayectos) {
                Persona p = t.getPersona();
                com.ProyectoPOO.ProyectoPOO.dto.DriverInfo di = com.ProyectoPOO.ProyectoPOO.dto.DriverInfo.builder()
                        .personId(p.getId())
                        .identification(p.getIdentification())
                        .names(p.getNames())
                        .lastNames(p.getLastNames())
                        .state(null)
                        .associationDate(null)
                        .build();
                drivers.add(di);
            }
            result.add(com.ProyectoPOO.ProyectoPOO.dto.RouteDriversResponse.builder()
                    .routeCode(rc)
                    .drivers(drivers)
                    .build());
        }

        return result;
    }
    
    /**
     * Obtiene los trayectos sin coordenadas (para rellenar con Google Maps)
     */
    public List<TrayectoResponse> getTrayectosWithoutCoordinates() {
        return trayectoRepository.findTrayectosWithoutCoordinates().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene trayectos donde el vehículo NO está habilitado O el conductor está restringido
     * para operar (estado RO)
     */
    public List<TrayectoResponse> getTrayectosWithDisabledVehiclesOrRestrictedDrivers() {
        List<Trayecto> allTrayectos = trayectoRepository.findAll();
        
        return allTrayectos.stream()
                .filter(trayecto -> {
                    // Verificar si vehículo está deshabilitado (no tiene documentos habilitados)
                    boolean vehicleDisabled = trayecto.getVehicle().getDocuments().stream()
                            .noneMatch(doc -> doc.getState().equals(DocumentState.HABILITADO));
                    
                    // Verificar si conductor está restringido (RO)
                    VehicleDriverId driverId = VehicleDriverId.builder()
                            .vehicleId(trayecto.getVehicle().getId())
                            .personaId(trayecto.getPersona().getId())
                            .build();
                    
                    boolean driverRestricted = false;
                    try {
                        VehicleDriver relation = vehicleDriverRepository.findById(driverId).orElse(null);
                        driverRestricted = relation != null && relation.getState().equals(DriverVehicleState.RO);
                    } catch (Exception e) {
                        // Ignorar si hay error
                    }
                    
                    return vehicleDisabled || driverRestricted;
                })
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Actualiza las coordenadas de un trayecto
     */
    @Transactional
    public TrayectoResponse updateCoordinates(Long trayectoId, Double latitude, Double longitude) {
        Trayecto trayecto = trayectoRepository.findById(trayectoId)
                .orElseThrow(() -> new IllegalArgumentException("Trayecto no encontrado"));
        
        trayecto.setLatitude(latitude);
        trayecto.setLongitude(longitude);
        
        return mapToResponse(trayectoRepository.save(trayecto));
    }
    
    /**
     * Mapea una entidad Trayecto a su DTO de respuesta
     */
    private TrayectoResponse mapToResponse(Trayecto trayecto) {
        return TrayectoResponse.builder()
                .id(trayecto.getId())
                .personaId(trayecto.getPersona().getId())
                .conductorNombre(trayecto.getPersona().getNames())
                .conductorApellido(trayecto.getPersona().getLastNames())
                .vehicleId(trayecto.getVehicle().getId())
                .vehiclePlate(trayecto.getVehicle().getPlate())
                .routeCode(trayecto.getRouteCode())
                .location(trayecto.getLocation())
                .stopOrder(trayecto.getStopOrder())
                .latitude(trayecto.getLatitude())
                .longitude(trayecto.getLongitude())
                .registeredByLogin(trayecto.getRegisteredByLogin())
                .createdAt(trayecto.getCreatedAt())
                .build();
    }
}




