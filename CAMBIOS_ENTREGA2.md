Q# Cambios Implementados - Entrega 2

Este documento resume lo que se implemento en el proyecto para cubrir los requerimientos de la Entrega 2.

## 1. Modelo de datos y entidades nuevas

Se agregaron nuevas entidades para gestionar personas, usuarios administrativos y la relacion conductor-vehiculo:

- `Persona`
  - Campos: `id`, `identification`, `identificationType`, `names`, `lastNames`, `email`, `personType`.
  - Validaciones de negocio en `@PrePersist/@PreUpdate`.
  - Restriccion de valores para tipo de identificacion y tipo de persona.

- `Usuario` + `UsuarioId` (PK compuesta)
  - PK compuesta por `persona_id` + `login`.
  - Relacion 1 a 1 con `Persona`.
  - Campos: `password`, `apikey`, `tokenValue`.

- Relacion `Vehicle` <-> `Persona` mediante `VehicleDriver` + `VehicleDriverId`
  - Permite asociar uno o varios conductores a un vehiculo.
  - Campos extra de relacion: `associationDate`, `state` (`PO`, `EA`, `RO`).
  - Se valida que solo personas tipo conductor (`C`) puedan asociarse como conductores.

- Extension de `VehicleDocument`
  - Se agrego campo BLOB (`pdf_blob`) para almacenar el PDF.
  - Se habilito exposicion en Base64 por JSON con `pdfBase64`.

### Enums nuevos

- `IdentificationType` (`CC`)
- `PersonType` (`C`, `A`)
- `DriverVehicleState` (`PO`, `EA`, `RO`)

## 2. Repositorios nuevos y consultas

Se crearon/ajustaron repositorios para soportar los nuevos requerimientos:

- `PersonaRepository`
  - Consulta de conteo agrupado por tipo de persona.
- `UsuarioRepository`
  - Busqueda por login, apiKey y token.
- `VehicleDriverRepository`
  - Busqueda por estado y por persona asociada.
- `VehicleRepository`
  - Consultas para:
    - vehiculos con documentos vencidos.
    - vehiculos con documentos por vencer en rango de fechas.

## 3. Servicios nuevos y cambios de negocio

### `PersonService`

- CRUD de persona (POST, GET, PUT).
- Generacion automatica de usuario para personas tipo administrativo (`A`):
  - Login nemotecnico: primera letra nombre + primera letra apellido + identificacion.
  - Password generado automaticamente.
  - APIKey generada automaticamente.

### `UserService`

- Cambio de password por login.
- Regeneracion de APIKey por login.
- Autenticacion (`login/password`) para generar token.
- Validacion de cabeceras de seguridad (`Authorization` + `X-API-KEY`).

### `VehicleService` (extendido)

- Cargue/actualizacion masiva de documentos de vehiculo (uno o varios) con Base64.
- Asociar vehiculo a conductor.
- Cambiar estado del conductor frente a un vehiculo.
- Consultas publicas:
  - vehiculos con documentos vencidos.
  - conductores que pueden operar.
  - detalle de vehiculo por placa (incluye conductores y documentos).
  - vehiculos con documentos por vencer en N dias.

### `PasswordCodec`

- Componente para hash y validacion de passwords.

## 4. Controladores nuevos y endpoints

### Nuevos controladores

- `PersonController`
- `UserController`
- `AuthController`
- `PublicQueryController`

### Controlador actualizado

- `VehicleController`
  - Nuevos endpoints para:
    - carga masiva de documentos.
    - asociacion conductor-vehiculo.
    - cambio de estado de conductor.

## 5. Seguridad aplicada

Se implemento seguridad en capa MVC usando interceptor:

- `ApiSecurityInterceptor`
- `PublicEndpoint` (anotacion para exponer rutas publicas)
- `WebMvcSecurityConfig`

### Regla de acceso

- Todo `/api/**` queda protegido por defecto.
- Se exige:
  - `Authorization: Bearer <token>`
  - `X-API-KEY: <apikey>`
- Rutas marcadas con `@PublicEndpoint` quedan sin token.

## 6. Endpoints publicos implementados

- `GET /api/public/vehicles/expired-documents`
- `GET /api/public/drivers/can-operate`
- `GET /api/public/vehicles/by-plate?plate=...`
- `GET /api/public/vehicles/expiring-documents?days=...`
- `GET /api/public/persons/count-by-type`
- `POST /api/auth/login`

## 7. Documentacion actualizada

- Se actualizo `README.md` con seccion de Entrega 2 y ejemplos de endpoints.

## 8. Correcciones tecnicas realizadas

- Se corrigio `pom.xml` por error de parseo XML causado por caracteres invalidos.

## 9. Pruebas y validacion

- Se ejecuto compilacion/pruebas con Maven Wrapper:
  - `mvnw.cmd -q test`
- Resultado: ejecucion correcta de pruebas existentes y nuevas pruebas unitarias basicas (`PersonServiceTest`).

## 10. Archivos principales agregados/modificados

### Agregados (resumen)

- `src/main/java/com/ProyectoPOO/ProyectoPOO/model/Persona.java`
- `src/main/java/com/ProyectoPOO/ProyectoPOO/model/Usuario.java`
- `src/main/java/com/ProyectoPOO/ProyectoPOO/model/UsuarioId.java`
- `src/main/java/com/ProyectoPOO/ProyectoPOO/model/VehicleDriver.java`
- `src/main/java/com/ProyectoPOO/ProyectoPOO/model/VehicleDriverId.java`
- `src/main/java/com/ProyectoPOO/ProyectoPOO/service/PersonService.java`
- `src/main/java/com/ProyectoPOO/ProyectoPOO/service/UserService.java`
- `src/main/java/com/ProyectoPOO/ProyectoPOO/service/PasswordCodec.java`
- `src/main/java/com/ProyectoPOO/ProyectoPOO/controller/PersonController.java`
- `src/main/java/com/ProyectoPOO/ProyectoPOO/controller/UserController.java`
- `src/main/java/com/ProyectoPOO/ProyectoPOO/controller/AuthController.java`
- `src/main/java/com/ProyectoPOO/ProyectoPOO/controller/PublicQueryController.java`
- `src/main/java/com/ProyectoPOO/ProyectoPOO/security/ApiSecurityInterceptor.java`
- `src/main/java/com/ProyectoPOO/ProyectoPOO/security/PublicEndpoint.java`
- `src/main/java/com/ProyectoPOO/ProyectoPOO/config/WebMvcSecurityConfig.java`

### Modificados (resumen)

- `src/main/java/com/ProyectoPOO/ProyectoPOO/controller/VehicleController.java`
- `src/main/java/com/ProyectoPOO/ProyectoPOO/service/VehicleService.java`
- `src/main/java/com/ProyectoPOO/ProyectoPOO/model/Vehicle.java`
- `src/main/java/com/ProyectoPOO/ProyectoPOO/model/VehicleDocument.java`
- `src/main/java/com/ProyectoPOO/ProyectoPOO/repository/VehicleRepository.java`
- `pom.xml`
- `README.md`

---

Si deseas, en el siguiente paso te puedo generar tambien una version de este documento orientada para entrega academica (formato formal con objetivo, alcance, diagrama de endpoints y evidencias por requerimiento).

