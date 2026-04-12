package com.ProyectoPOO.ProyectoPOO.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
// `plate` = placa; debe ser unica en todo el sistema.
@Table(name = "vehicles", uniqueConstraints = {@UniqueConstraint(columnNames = {"plate"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // `type` = tipo de vehiculo; se guarda como texto para facilitar lectura en BD.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType type;

    // `plate` = placa; se restringe a 6 caracteres por regla de negocio.
    @Column(length = 6, nullable = false, unique = true)
    private String plate;

    // `serviceType` = tipo de servicio (publico/privado).
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceType serviceType;

    // `fuelType` = tipo de combustible (gasolina/gas/disel).
    @Enumerated(EnumType.STRING)
    // `passengersCapacity` = capacidad de pasajeros.
    @Column(nullable = false)
    private FuelType fuelType;

    @Column(nullable = false)
    private Integer passengersCapacity;

    // `color` = color; se valida en formato hexadecimal #RRGGBB en validate().
    // `model` = modelo (anio/modelo numerico).
    @Column(nullable = false)
    private String color;

    // `brand` = marca.
    @Column(nullable = false)
    private Integer model;

    // `line` = linea.
    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String line;

    // `documents` = documentos asociados; relacion 1:N con cascada para persistir/eliminar en conjunto.
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    @JsonManagedReference
    private Set<VehicleDocument> documents = new HashSet<>();

    // Estos hooks de JPA ejecutan validate() automáticamente antes de INSERT y UPDATE.
    @PrePersist
    @PreUpdate
    private void validate() {
        // 1) Operador || (OR) con short-circuit: si plate es null, no evalúa plate.length().
        if (plate == null || plate.length() != 6) {
            throw new IllegalArgumentException("La placa debe tener exactamente 6 caracteres");
        }

        // 2) if / else if: selecciona la regla según el enum VehicleType.
        //    String.matches(regex) valida formato completo de la cadena.
        if (type == VehicleType.AUTOMOVIL) {
            if (!plate.matches("^[A-Za-z]{3}\\d{3}$")) {
                throw new IllegalArgumentException("Placa inválida para AUTOMOVIL: debe ser AAA999");
            }
        } else if (type == VehicleType.MOTOCICLETA) {
            if (!plate.matches("^[A-Za-z]{3}\\d{2}[A-Za-z]$")) {
                throw new IllegalArgumentException("Placa inválida para MOTOCICLETA: debe ser AAA99A");
            }
        }

        // 3) ! niega el resultado de matches: entra al error cuando el formato no coincide.
        if (color == null || !color.matches("^#[A-Fa-f0-9]{6}$")) {
            throw new IllegalArgumentException("El color debe estar en formato hexadecimal #RRGGBB");
        }

        // 4) Validación numérica: modelo y capacidad deben representar valores útiles (> 0).
        if (model == null || model <= 0) {
            throw new IllegalArgumentException("Modelo debe ser un entero positivo");
        }
        if (passengersCapacity == null || passengersCapacity <= 0) {
            throw new IllegalArgumentException("La capacidad de pasajeros debe ser un entero positivo");
        }

        // 5) isBlank() valida cadenas vacías o solo espacios.
        if (brand == null || brand.isBlank()) {
            throw new IllegalArgumentException("La marca es obligatoria");
        }
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("La línea es obligatoria");
        }

        // 6) Regla de negocio del taller: todo vehículo debe tener al menos un documento asociado.
        if (documents == null || documents.isEmpty()) {
            throw new IllegalArgumentException("No se puede crear un vehículo sin al menos un documento asociado");
        }

        // 7) for-each: recorre cada VehicleDocument y sincroniza la referencia inversa (vd -> this).
        for (VehicleDocument vd : documents) {
            vd.setVehicle(this);
        }
    }
}
