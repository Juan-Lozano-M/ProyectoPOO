package com.ProyectoPOO.ProyectoPOO.dto;

import com.ProyectoPOO.ProyectoPOO.model.IdentificationType;
import com.ProyectoPOO.ProyectoPOO.model.PersonType;
import lombok.Data;

@Data
public class PersonUpsertRequest {
    private String identification;
    private IdentificationType identificationType;
    private String names;
    private String lastNames;
    private String email;
    private PersonType personType;
}

