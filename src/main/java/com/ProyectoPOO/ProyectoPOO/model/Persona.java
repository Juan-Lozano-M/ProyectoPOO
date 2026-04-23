package com.ProyectoPOO.ProyectoPOO.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "personas", uniqueConstraints = {
        @UniqueConstraint(columnNames = "identification")
})
@Check(constraints = "identification_type IN ('CC') AND person_type IN ('C','A')")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String identification;

    @Enumerated(EnumType.STRING)
    @Column(name = "identification_type", nullable = false, length = 2)
    private IdentificationType identificationType;

    @Column(nullable = false)
    private String names;

    @Column(nullable = false)
    private String lastNames;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "person_type", nullable = false, length = 1)
    private PersonType personType;

    @OneToOne(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuario;

    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<VehicleDriver> vehicleAssociations = new HashSet<>();

    @PrePersist
    @PreUpdate
    private void validate() {
        if (identification == null || identification.isBlank()) {
            throw new IllegalArgumentException("La identificacion es obligatoria");
        }
        if (identificationType == null) {
            throw new IllegalArgumentException("El tipo de identificacion es obligatorio");
        }
        if (names == null || names.isBlank()) {
            throw new IllegalArgumentException("Los nombres son obligatorios");
        }
        if (lastNames == null || lastNames.isBlank()) {
            throw new IllegalArgumentException("Los apellidos son obligatorios");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El correo es obligatorio");
        }
        if (personType == null) {
            throw new IllegalArgumentException("El tipo de persona es obligatorio");
        }
    }
}

