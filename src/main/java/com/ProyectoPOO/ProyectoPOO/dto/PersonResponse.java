package com.ProyectoPOO.ProyectoPOO.dto;

import com.ProyectoPOO.ProyectoPOO.model.PersonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonResponse {
    private Long id;
    private String identification;
    private String names;
    private String lastNames;
    private String email;
    private PersonType personType;
    private AdminUserData adminUserData;
}

