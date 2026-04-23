package com.ProyectoPOO.ProyectoPOO.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @EmbeddedId
    private UsuarioId id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId("personaId")
    @JoinColumn(name = "persona_id", nullable = false, unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Persona persona;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 120)
    private String apikey;

    @Column(name = "token_value", length = 220)
    private String tokenValue;

    @PrePersist
    @PreUpdate
    private void validate() {
        if (id == null || id.getLogin() == null || id.getLogin().isBlank()) {
            throw new IllegalArgumentException("El login es obligatorio");
        }
        if (persona == null) {
            throw new IllegalArgumentException("La persona es obligatoria");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contrasena es obligatoria");
        }
        if (apikey == null || apikey.isBlank()) {
            throw new IllegalArgumentException("El apikey es obligatorio");
        }
    }
}

