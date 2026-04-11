package com.ProyectoPOO.ProyectoPOO.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "vehicles", uniqueConstraints = {@UniqueConstraint(columnNames = {"plate"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType type;

    @Column(length = 6, nullable = false, unique = true)
    private String plate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceType serviceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FuelType fuelType;

    @Column(nullable = false)
    private Integer passengersCapacity;

    @Column(nullable = false)
    private String color; // se validará como #RRGGBB en lifecycle callbacks

    @Column(nullable = false)
    private Integer model;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String line;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    @JsonManagedReference
    private Set<VehicleDocument> documents = new HashSet<>();

    @PrePersist
    @PreUpdate
    private void validate() {
        // placa debe tener 6 caracteres exactos
        if (plate == null || plate.length() != 6) {
            throw new IllegalArgumentException("La placa debe tener exactamente 6 caracteres");
        }

        // validar según tipo
        if (type == VehicleType.AUTOMOVIL) {
            if (!plate.matches("^[A-Za-z]{3}\\d{3}$")) {
                throw new IllegalArgumentException("Placa inválida para AUTOMOVIL: debe ser AAA999");
            }
        } else if (type == VehicleType.MOTOCICLETA) {
            if (!plate.matches("^[A-Za-z]{3}\\d{2}[A-Za-z]$")) {
                throw new IllegalArgumentException("Placa inválida para MOTOCICLETA: debe ser AAA99A");
            }
        }

        // color hex #RRGGBB
        if (color == null || !color.matches("^#[A-Fa-f0-9]{6}$")) {
            throw new IllegalArgumentException("El color debe estar en formato hexadecimal #RRGGBB");
        }

        // model debe ser entero positivo
        if (model == null || model <= 0) {
            throw new IllegalArgumentException("Modelo debe ser un entero positivo");
        }

        // passengers debe ser entero positivo
        if (passengersCapacity == null || passengersCapacity <= 0) {
            throw new IllegalArgumentException("La capacidad de pasajeros debe ser un entero positivo");
        }

        // brand y line
        if (brand == null || brand.isBlank()) {
            throw new IllegalArgumentException("La marca es obligatoria");
        }
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("La línea es obligatoria");
        }

        // asegurar que tenga al menos un documento asociado si se va a persistir (regla de negocio)
        if (documents == null || documents.isEmpty()) {
            throw new IllegalArgumentException("No se puede crear un vehículo sin al menos un documento asociado");
        }

        // asegurarse que cada VehicleDocument tenga su referencia al vehículo
        for (VehicleDocument vd : documents) {
            vd.setVehicle(this);
        }
    }
}
