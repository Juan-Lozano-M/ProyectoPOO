package com.ProyectoPOO.ProyectoPOO.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Servicio para integración con Google Maps API.
 * Obtiene las coordenadas (latitud/longitud) de una dirección.
 * NOTA: Para usar este servicio, necesitas:
 * 1. Una API Key de Google Maps
 * 2. Agregar la propiedad en application.properties: google.maps.api.key=TU_API_KEY
 */
@Service
@Slf4j
public class GoogleMapsService {

    private final String googleMapsApiKey;

    private final RestTemplate restTemplate;

    public GoogleMapsService(RestTemplate restTemplate, @Value("${google.maps.api.key:}") String googleMapsApiKey) {
        this.restTemplate = restTemplate;
        this.googleMapsApiKey = googleMapsApiKey;
    }

    /**
     * Obtiene las coordenadas de una ubicación usando Google Maps Geocoding API
     * 
     * @param location Descripción de la ubicación (ej: "Conservatorio del Tolima, Ibagué, Tolima")
     * @return Objeto con latitud y longitud, o null si no se encontraron coordenadas
     */
    public Coordinates getCoordinates(String location) {
        try {
            // Si no hay API key configurada, retornar null
            if (googleMapsApiKey == null || googleMapsApiKey.isBlank()) {
                log.warn("Google Maps API Key no está configurada");
                return null;
            }

            // Construcción de la URL para Google Maps Geocoding API
            String url = String.format(
                    "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s",
                    location.replace(" ", "+"),
                    googleMapsApiKey
            );

            // Realizar la solicitud HTTP
            GeocodeResponse response = restTemplate.getForObject(url, GeocodeResponse.class);

            if (response != null && response.getResults() != null && response.getResults().length > 0) {
                Location loc = response.getResults()[0].getGeometry().getLocation();
                return Coordinates.builder()
                        .latitude(loc.getLat())
                        .longitude(loc.getLng())
                        .build();
            }

            log.warn("No se encontraron coordenadas para: {}", location);
            return null;

        } catch (Exception e) {
            log.error("Error consultando Google Maps API: ", e);
            return null;
        }
    }

    // ==================== Clases DTO para Google Maps API ====================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeocodeResponse {
        private Result[] results;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Result {
        private Geometry geometry;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Geometry {
        private Location location;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        private Double lat;
        private Double lng;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Coordinates {
        private Double latitude;
        private Double longitude;
    }

    /**
     * Obtiene una ruta siguiendo calles entre una lista de puntos (waypoints) usando
     * Google Directions API. Retorna la lista de puntos que forman la ruta (polyline
     * decodificada). Si no se puede obtener la ruta retorna null.
     *
     * @param waypoints Lista de coordenadas ordenadas (origin -> intermediates -> destination)
     */
    public java.util.List<Coordinates> getRoutePolyline(java.util.List<Coordinates> waypoints) {
        try {
            if (googleMapsApiKey == null || googleMapsApiKey.isBlank()) {
                log.warn("Google Maps API Key no está configurada");
                return null;
            }

            if (waypoints == null || waypoints.size() < 2) {
                return null; // No hay suficiente información para crear una ruta
            }

            Coordinates origin = waypoints.get(0);
            Coordinates destination = waypoints.get(waypoints.size() - 1);

            StringBuilder url = new StringBuilder();
            url.append("https://maps.googleapis.com/maps/api/directions/json?");
            url.append("origin=").append(origin.getLatitude()).append(",").append(origin.getLongitude());
            url.append("&destination=").append(destination.getLatitude()).append(",").append(destination.getLongitude());

            // if there are intermediate waypoints, add them
            if (waypoints.size() > 2) {
                url.append("&waypoints=");
                // Use via: to try to keep route through intermediate points
                for (int i = 1; i < waypoints.size() - 1; i++) {
                    Coordinates wp = waypoints.get(i);
                    if (i > 1) url.append("|");
                    url.append("via:").append(wp.getLatitude()).append(",").append(wp.getLongitude());
                }
            }

            url.append("&mode=driving");
            url.append("&key=").append(googleMapsApiKey);

            DirectionsResponse response = restTemplate.getForObject(url.toString(), DirectionsResponse.class);

            if (response != null && response.getRoutes() != null && response.getRoutes().length > 0) {
                String encoded = response.getRoutes()[0].getOverview_polyline().getPoints();
                if (encoded != null && !encoded.isBlank()) {
                    return decodePolyline(encoded);
                }
            }

            log.warn("No se pudo obtener ruta de Google Directions para los waypoints proporcionados");
            return null;
        } catch (Exception e) {
            log.error("Error consultando Google Directions API: ", e);
            return null;
        }
    }

    // Decodificador de polyline (algoritmo oficial de Google)
    private java.util.List<Coordinates> decodePolyline(String encoded) {
        java.util.List<Coordinates> path = new java.util.ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0) ? ~(result >> 1) : (result >> 1);
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0) ? ~(result >> 1) : (result >> 1);
            lng += dlng;

            Coordinates c = Coordinates.builder()
                    .latitude(lat / 1e5)
                    .longitude(lng / 1e5)
                    .build();
            path.add(c);
        }

        return path;
    }

    // DTOs mínimos para Directions API
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DirectionsResponse {
        private DirRoute[] routes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DirRoute {
        private OverviewPolyline overview_polyline;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverviewPolyline {
        private String points;
    }
}





