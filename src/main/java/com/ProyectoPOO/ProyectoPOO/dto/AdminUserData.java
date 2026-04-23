package com.ProyectoPOO.ProyectoPOO.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserData {
    private String login;
    private String generatedPassword;
    private String apiKey;
}

