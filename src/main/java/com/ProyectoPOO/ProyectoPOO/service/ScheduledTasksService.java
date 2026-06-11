package com.ProyectoPOO.ProyectoPOO.service;

import com.ProyectoPOO.ProyectoPOO.model.DocumentState;
import com.ProyectoPOO.ProyectoPOO.model.DriverVehicleState;
import com.ProyectoPOO.ProyectoPOO.model.Persona;
import com.ProyectoPOO.ProyectoPOO.model.PersonType;
import com.ProyectoPOO.ProyectoPOO.model.Trayecto;
import com.ProyectoPOO.ProyectoPOO.model.VehicleDocument;
import com.ProyectoPOO.ProyectoPOO.model.VehicleDriver;
import com.ProyectoPOO.ProyectoPOO.repository.PersonaRepository;
import com.ProyectoPOO.ProyectoPOO.repository.TrayectoRepository;
import com.ProyectoPOO.ProyectoPOO.repository.VehicleDocumentRepository;
import com.ProyectoPOO.ProyectoPOO.repository.VehicleDriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio con tareas programadas para:
 * 1. Validar vigencia de licencias de conducción (cada 2 minutos)
 * 2. Validar vigencia de documentos de vehículos (cada 2 minutos)
 * 3. Obtener coordenadas de Google Maps para trayectos sin ellas (cada 90 segundos)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasksService {

    private final PersonaRepository personaRepository;
    private final VehicleDocumentRepository vehicleDocumentRepository;
    private final VehicleDriverRepository vehicleDriverRepository;
    private final TrayectoRepository trayectoRepository;
    private final GoogleMapsService googleMapsService;

    /**
     * Tarea programada cada 2 minutos: Verifica vigencia de licencias de conducción
     * Si la licencia está vencida, cambia el estado del conductor a RO (Restringido para Operar)
     */
    @Scheduled(fixedDelay = 120000) // 120,000 ms = 2 minutos
    @Transactional
    public void validateDriverLicenses() {
        log.info("Iniciando validación de licencias de conducción...");
        try {
            List<Persona> conductores = personaRepository.findByPersonType(PersonType.C);
            
            for (Persona conductor : conductores) {
                // Verificar si la licencia está vencida
                if (conductor.getDrivingLicenseExpiry() != null && 
                    conductor.getDrivingLicenseExpiry().isBefore(LocalDate.now())) {
                    
                    log.info("Licencia vencida para conductor: {}", conductor.getId());
                    
                    // Obtener todas las relaciones conductor-vehículo
                    List<VehicleDriver> driverVehicleRelations = vehicleDriverRepository
                            .findByPersona_Id(conductor.getId());
                    
                    // Cambiar estado a RO para todas las relaciones
                    for (VehicleDriver relation : driverVehicleRelations) {
                        relation.setState(DriverVehicleState.RO);
                        vehicleDriverRepository.save(relation);
                        
                        log.info("Conductor {} restringido para vehículo {}. Motivo: Licencia vencida",
                                conductor.getId(), relation.getVehicle().getId());
                    }
                }
            }
            log.info("Validación de licencias completada");
        } catch (Exception e) {
            log.error("Error validando licencias: ", e);
        }
    }

    /**
     * Tarea programada cada 2 minutos: Verifica vigencia de documentos del vehículo
     * Si un documento está vencido, cambia su estado a VENCIDO
     */
    @Scheduled(fixedDelay = 120000) // 120,000 ms = 2 minutos
    @Transactional
    public void validateVehicleDocuments() {
        log.info("Iniciando validación de documentos de vehículos...");
        try {
            List<VehicleDocument> documents = vehicleDocumentRepository.findAll();
            
            for (VehicleDocument doc : documents) {
                // Verificar si el documento está vencido
                if (doc.getExpiryDate() != null && 
                    doc.getExpiryDate().isBefore(LocalDate.now()) &&
                    !doc.getState().equals(DocumentState.VENCIDO)) {
                    
                    log.info("Documento vencido: ID={}, Vehículo={}", 
                            doc.getId(), doc.getVehicle().getId());
                    
                    // Cambiar estado a VENCIDO
                    doc.setState(DocumentState.VENCIDO);
                    vehicleDocumentRepository.save(doc);
                }
            }
            log.info("Validación de documentos completada");
        } catch (Exception e) {
            log.error("Error validando documentos: ", e);
        }
    }

    /**
     * Tarea programada cada 90 segundos: Obtiene coordenadas de Google Maps
     * para trayectos que no tienen latitud/longitud asociadas
     */
    @Scheduled(fixedDelay = 90000) // 90,000 ms = 90 segundos
    @Transactional
    public void fillMissingCoordinates() {
        log.info("Iniciando búsqueda de coordenadas con Google Maps...");
        try {
            // Recuperar todos los trayectos y filtrar aquellos que necesitan corrección
            List<Trayecto> todos = trayectoRepository.findAll();

            for (Trayecto trayecto : todos) {
                try {
                    Double lat = trayecto.getLatitude();
                    Double lon = trayecto.getLongitude();

                    boolean missing = (lat == null || lon == null);
                    boolean outOfRange = (lat != null && (lat < -90.0 || lat > 90.0)) ||
                                         (lon != null && (lon < -180.0 || lon > 180.0));
                    boolean zeroBoth = (lat != null && lon != null && lat == 0.0 && lon == 0.0);

                    if (!missing && !outOfRange && !zeroBoth) {
                        // Coordenadas parecen válidas, saltar
                        continue;
                    }

                    // Intentar obtener coordenadas válidas desde Google Maps usando la ubicación
                    if (trayecto.getLocation() == null || trayecto.getLocation().isBlank()) {
                        log.warn("Trayecto {} tiene ubicación vacía, no se puede obtener coordenadas", trayecto.getId());
                        continue;
                    }

                    var coordinates = googleMapsService.getCoordinates(trayecto.getLocation());

                    if (coordinates != null && coordinates.getLatitude() != null && coordinates.getLongitude() != null) {
                        trayecto.setLatitude(coordinates.getLatitude());
                        trayecto.setLongitude(coordinates.getLongitude());
                        trayectoRepository.save(trayecto);

                        log.info("Coordenadas corregidas para trayecto: ID={}, Ubicación={}, Lat={}, Long={}",
                                trayecto.getId(), trayecto.getLocation(),
                                trayecto.getLatitude(), trayecto.getLongitude());
                    } else {
                        log.warn("Google Maps no devolvió coordenadas para trayecto {} (ubicación={})",
                                trayecto.getId(), trayecto.getLocation());
                    }
                } catch (Exception e) {
                    log.warn("No se pudieron obtener/corregir coordenadas para trayecto {}: {}", 
                            trayecto.getId(), e.getMessage());
                }
            }
            log.info("Búsqueda de coordenadas completada");
        } catch (Exception e) {
            log.error("Error obteniendo coordenadas: ", e);
        }
    }
}


