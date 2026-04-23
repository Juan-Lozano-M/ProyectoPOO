package com.ProyectoPOO.ProyectoPOO.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDriverId implements Serializable {

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "persona_id")
    private Long personaId;
}

