package com.ProyectoPOO.ProyectoPOO.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "trayectos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trayecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign Key a Persona (solo conductores - tipo 'C')
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Persona persona;

    // Foreign Key a Vehicle
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Vehicle vehicle;

    // Código de ruta que agrupa varios trayectos
    @Column(nullable = false, length = 50)
    private String routeCode;

    // Ubicación de la parada
    @Column(nullable = false, columnDefinition = "TEXT")
    private String location;

    // Orden de la parada: 0 = inicial, > 0 = paradas, máximo 5 intermedias
    @Column(nullable = false)
    private Integer stopOrder;

    // Coordenadas geográficas
    @Column
    private Double latitude;

    @Column
    private Double longitude;

    // Login del usuario que registra el trayecto
    @Column(nullable = false, length = 50)
    private String registeredByLogin;

    // Timestamp de creación
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        validate();
    }

    @PreUpdate
    private void preUpdate() {
        validate();
    }

    private void validate() {
        if (persona == null) {
            throw new IllegalArgumentException("La persona (conductor) es obligatoria");
        }
        if (vehicle == null) {
            throw new IllegalArgumentException("El vehículo es obligatorio");
        }
        if (routeCode == null || routeCode.isBlank()) {
            throw new IllegalArgumentException("El código de ruta es obligatorio");
        }
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("La ubicación es obligatoria");
        }
        if (stopOrder == null || stopOrder < 0) {
            throw new IllegalArgumentException("El orden de parada debe ser >= 0");
        }
        if (stopOrder > 5 && stopOrder > 0) {
            // Paradas intermedias no pueden ser más de 5 (0 = inicial, 1-5 = intermedias, 6+ = final)
            if (stopOrder != 99) { // 99 es una parada final placeholder
                throw new IllegalArgumentException("Máximo 5 paradas intermedias permitidas (orden 1-5)");
            }
        }
        if (registeredByLogin == null || registeredByLogin.isBlank()) {
            throw new IllegalArgumentException("El login del usuario que registra es obligatorio");
        }
    }
}

