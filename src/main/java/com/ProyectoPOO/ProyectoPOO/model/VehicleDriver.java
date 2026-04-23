package com.ProyectoPOO.ProyectoPOO.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;

import java.time.LocalDate;

@Entity
@Table(name = "vehicle_drivers")
@Check(constraints = "state IN ('PO','EA','RO')")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDriver {

    @EmbeddedId
    private VehicleDriverId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("vehicleId")
    @JoinColumn(name = "vehicle_id", nullable = false)
    @JsonBackReference(value = "vehicle-driver")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("personaId")
    @JoinColumn(name = "persona_id", nullable = false)
    @JsonBackReference(value = "persona-driver")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Persona persona;

    @Column(nullable = false)
    private LocalDate associationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 2)
    private DriverVehicleState state;

    @PrePersist
    @PreUpdate
    private void validate() {
        if (vehicle == null) {
            throw new IllegalArgumentException("El vehiculo es obligatorio en la asociacion");
        }
        if (persona == null) {
            throw new IllegalArgumentException("La persona es obligatoria en la asociacion");
        }
        if (associationDate == null) {
            associationDate = LocalDate.now();
        }
        if (state == null) {
            state = DriverVehicleState.EA;
        }
    }
}

