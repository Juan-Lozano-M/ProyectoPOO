package com.ProyectoPOO.ProyectoPOO.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "ProyectoPOO API",
                version = "v4",
                description = "API REST para la gestion de personas, usuarios, vehiculos, documentos, "
                        + "trayectos y cargue masivo de trayectos via Excel.",
                contact = @Contact(name = "ProyectoPOO")
        ),
        servers = {
                @Server(url = "/", description = "Servidor por defecto")
        },
        security = {
                @SecurityRequirement(name = "bearerAuth"),
                @SecurityRequirement(name = "apiKeyAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Token JWT obtenido en /api/auth/login. Enviar como 'Authorization: Bearer {token}'."
)
@SecurityScheme(
        name = "apiKeyAuth",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "X-API-KEY",
        description = "API Key del usuario, enviada en el header 'X-API-KEY' (o 'APIKey')."
)
public class OpenApiConfig {
}
