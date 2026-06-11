package com.ProyectoPOO.ProyectoPOO.repository;

import com.ProyectoPOO.ProyectoPOO.model.Trayecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrayectoRepository extends JpaRepository<Trayecto, Long> {
    
    // Buscar trayectos por código de ruta
    List<Trayecto> findByRouteCodeOrderByStopOrder(String routeCode);
    
    // Buscar trayectos por persona (conductor)
    @Query("SELECT DISTINCT t.routeCode FROM Trayecto t WHERE t.persona.id = :personaId ORDER BY t.routeCode")
    List<String> findRouteCodesByPersona(@Param("personaId") Long personaId);
    
    // Buscar trayectos por vehículo
    @Query("SELECT DISTINCT t.routeCode FROM Trayecto t WHERE t.vehicle.id = :vehicleId ORDER BY t.routeCode")
    List<String> findRouteCodesByVehicle(@Param("vehicleId") Long vehicleId);
    
    // Buscar trayectos sin coordenadas
    @Query("SELECT t FROM Trayecto t WHERE t.latitude IS NULL OR t.longitude IS NULL")
    List<Trayecto> findTrayectosWithoutCoordinates();
    
    // Buscar trayectos por ruta y persona
    List<Trayecto> findByRouteCodeAndPersonaId(String routeCode, Long personaId);
    
    // Buscar trayectos por ruta y vehículo
    List<Trayecto> findByRouteCodeAndVehicleId(String routeCode, Long vehicleId);
}

