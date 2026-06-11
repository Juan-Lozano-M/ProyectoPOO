package com.ProyectoPOO.ProyectoPOO.controller;

import com.ProyectoPOO.ProyectoPOO.dto.TrayectoRequest;
import com.ProyectoPOO.ProyectoPOO.dto.TrayectoResponse;
import com.ProyectoPOO.ProyectoPOO.service.GoogleMapsService;
import com.ProyectoPOO.ProyectoPOO.service.TrayectoService;
import com.ProyectoPOO.ProyectoPOO.service.UserService;
import com.ProyectoPOO.ProyectoPOO.security.PublicEndpoint;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

/**
 * Controlador protegido para trayectos.
 * Requiere: Token JWT + APIKey en headers
 */
@RestController
@RequestMapping("/api/trayectos")
@RequiredArgsConstructor
public class TrayectoController {

    private final TrayectoService trayectoService;
    private final UserService userService;
    private final GoogleMapsService googleMapsService;

    @PublicEndpoint
    @GetMapping("/public")
    public ResponseEntity<List<TrayectoResponse>> getAllTrayectosPublic() {
        List<TrayectoResponse> trayectos = trayectoService.getAllTrayectos();
        return ResponseEntity.ok(trayectos);
    }
    /**
     * Crea un nuevo trayecto
     * POST /api/trayectos
     * Headers requeridos: Authorization (Bearer token), APIKey
     */
    @PostMapping
    public ResponseEntity<?> createTrayecto(
            @RequestBody TrayectoRequest request,
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(hidden = true) @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {
        try {
            // Validar seguridad (token + apikey) y obtener usuario
            var usuario = userService.validateSecurityHeaders(authHeader, apiKey);
            String userLogin = usuario.getId().getLogin();

            // Crear trayecto
            TrayectoResponse response = trayectoService.createTrayecto(request, userLogin);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }

    /**
     * Obtiene trayectos de una ruta específica
     * GET /api/trayectos/rutas/{routeCode}
     * Headers requeridos: Authorization (Bearer token), APIKey
     */
    @GetMapping("/rutas/{routeCode}")
    public ResponseEntity<?> getTrayectosByRoute(
            @PathVariable String routeCode,
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(hidden = true) @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {
        try {
            // Validar seguridad
            userService.validateSecurityHeaders(authHeader, apiKey);
            
            List<TrayectoResponse> trayectos = trayectoService.getTrayectosByRouteCode(routeCode);
            return ResponseEntity.ok(trayectos);
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }

    /**
     * Obtiene códigos de ruta de un conductor
     * GET /api/trayectos/conductores/{personaId}/rutas
     * Headers requeridos: Authorization (Bearer token), APIKey
     */
    @GetMapping("/conductores/{personaId}/rutas")
    public ResponseEntity<?> getRoutesByConductor(
            @PathVariable Long personaId,
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(hidden = true) @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {
        try {
            // Validar seguridad
            userService.validateSecurityHeaders(authHeader, apiKey);
            
            List<String> routeCodes = trayectoService.getRouteCodesByPersona(personaId);
            return ResponseEntity.ok(routeCodes);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }

    /**
     * Obtiene códigos de ruta de un vehículo
     * GET /api/trayectos/vehiculos/{vehicleId}/rutas
     * Headers requeridos: Authorization (Bearer token), APIKey
     */
    @GetMapping("/vehiculos/{vehicleId}/rutas")
    public ResponseEntity<?> getRoutesByVehicle(
            @PathVariable Long vehicleId,
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(hidden = true) @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {
        try {
            // Validar seguridad
            userService.validateSecurityHeaders(authHeader, apiKey);
            
            List<String> routeCodes = trayectoService.getRouteCodesByVehicle(vehicleId);
            return ResponseEntity.ok(routeCodes);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }

    /**
     * Obtiene códigos de ruta a partir del número de identificación del conductor (protegido)
     */
    @GetMapping("/conductores/identification/{identification}/rutas")
    public ResponseEntity<?> getRoutesByConductorIdentification(
            @PathVariable String identification,
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(hidden = true) @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {
        try {
            userService.validateSecurityHeaders(authHeader, apiKey);
            return ResponseEntity.ok(trayectoService.getRouteCodesByPersonaIdentification(identification));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }

    /**
     * Obtiene rutas agrupadas por código y sus conductores a partir de la placa del vehículo (protegido)
     */
    @GetMapping("/vehiculos/plate/{plate}/rutas")
    public ResponseEntity<?> getRoutesAndDriversByVehiclePlate(
            @PathVariable String plate,
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(hidden = true) @RequestHeader(value = "X-API-KEY", required = false) String apiKey) {
        try {
            userService.validateSecurityHeaders(authHeader, apiKey);
            return ResponseEntity.ok(trayectoService.getRoutesAndDriversByVehiclePlate(plate));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }

    /**
     * Obtiene la geometría de una ruta siguiendo calles mediante Google Directions.
     */
    @PublicEndpoint
    @GetMapping("/public/routes/{routeCode}/path")
    public ResponseEntity<List<GoogleMapsService.Coordinates>> getRoutePath(@PathVariable String routeCode) {
        List<TrayectoResponse> stops = trayectoService.getTrayectosByRouteCode(routeCode);

        List<GoogleMapsService.Coordinates> waypoints = stops.stream()
                .filter(s -> s.getLatitude() != null && s.getLongitude() != null)
                .map(s -> GoogleMapsService.Coordinates.builder()
                        .latitude(s.getLatitude())
                        .longitude(s.getLongitude())
                        .build())
                .toList();

        if (waypoints.size() < 2) {
            return ResponseEntity.ok(waypoints);
        }

        List<GoogleMapsService.Coordinates> path = googleMapsService.getRoutePolyline(waypoints);

        if (path == null || path.isEmpty()) {
            // Fallback a segmentos directos entre paradas
            return ResponseEntity.ok(waypoints);
        }

        return ResponseEntity.ok(path);
    }
}



