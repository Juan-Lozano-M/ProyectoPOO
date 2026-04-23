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
public class UsuarioId implements Serializable {

    @Column(name = "persona_id")
    private Long personaId;

    @Column(name = "login", length = 80)
    private String login;
}

