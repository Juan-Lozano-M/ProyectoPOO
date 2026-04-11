# ProyectoPOO - API REST de Vehiculos y Documentos

API REST desarrollada con Spring Boot, JPA y MySQL para gestionar:

- Vehiculos
- Documentos parametricos (catalogo/configuracion)
- Relacion vehiculo-documento con estado y fechas

El proyecto esta orientado al taller de profundizacion de POO II (Entrega 1), usando Postman como cliente para consumir los servicios REST.

## 1) Tecnologias

- Java 17
- Spring Boot
- Spring Data JPA
- MySQL
- Maven Wrapper (`mvnw`, `mvnw.cmd`)
- Lombok
- Jakarta Validation / Hibernate Validator

## 2) Arquitectura (capas)

Estructura por capas en `src/main/java/com/ProyectoPOO/ProyectoPOO/`:

- `controller/`: expone endpoints REST (`VehicleController`, `DocumentController`).
- `service/`: contiene reglas de negocio (`VehicleService`, `DocumentService`).
- `repository/`: acceso a datos con Spring Data JPA.
- `model/`: entidades JPA y enums de dominio.
- `ProyectoPooApplication.java`: clase principal de arranque Spring Boot.

Flujo general:

1. El cliente (Postman) envia solicitud HTTP.
2. El controlador valida entrada basica y delega en servicio.
3. El servicio aplica reglas de negocio.
4. El repositorio persiste/consulta en base de datos.
5. Se retorna respuesta JSON al cliente.

## 3) Modelo de datos

### 3.1 Entidad `Vehicle`

Campos principales:

- `id` (PK)
- `type` (`AUTOMOVIL`, `MOTOCICLETA`)
- `plate` (unica)
- `serviceType` (`PU`, `PR`)
- `fuelType` (`GASOLINA`, `GAS`, `DIESEL`)
- `passengersCapacity` (entero)
- `color` (hexadecimal `#RRGGBB`)
- `model` (entero)
- `brand`
- `line`
- `documents` (relacion 1:N con `VehicleDocument`)

Validaciones destacadas:

- Placa de 6 caracteres exactos.
- Placa por tipo:
  - Automovil: `AAA999`
  - Motocicleta: `AAA99A`
- Color en formato hexadecimal.
- Modelo y capacidad de pasajeros positivos.
- Marca y linea obligatorias.
- No permite crear vehiculo sin documentos asociados.

### 3.2 Entidad `Document` (parametrica)

Campos principales:

- `id` (PK)
- `code` (unico)
- `name`
- `applicability` (`A`, `M`, `AM`)
- `mandatory` (`RA`, `RM`, `RR`)
- `description`

Validaciones destacadas:

- Codigo y nombre obligatorios.
- Aplicabilidad y obligatoriedad obligatorias.
- Coherencia entre `mandatory` y `applicability`.

### 3.3 Entidad `VehicleDocument`

Tabla intermedia entre vehiculo y documento con campos adicionales:

- `id` (PK)
- `vehicle` (FK)
- `document` (FK)
- `issueDate`
- `expiryDate`
- `state` (`HABILITADO`, `VENCIDO`, `EN_VERIFICACION`)

Reglas destacadas:

- Restriccion unica por pareja (`vehicle_id`, `document_id`).
- Estado inicial por defecto: `EN_VERIFICACION`.
- Fechas obligatorias y `expiryDate >= issueDate`.

## 4) Reglas de negocio implementadas

1. No se crea vehiculo sin al menos un documento asociado.
2. Al crear vehiculo, los documentos asociados inician en `EN_VERIFICACION`.
3. Se valida que los documentos asociados existan previamente (por `id`).
4. No se permite asociar el mismo documento dos veces al mismo vehiculo.
5. Se valida aplicabilidad del documento segun tipo de vehiculo.
6. Se validan documentos obligatorios por tipo (`RA/RM/RR`).
7. Al agregar documento posteriormente a un vehiculo, tambien inicia en `EN_VERIFICACION`.

## 5) Endpoints REST

Base URL sugerida en local:

`http://localhost:8080`

### 5.1 Documentos parametricos

- `POST /api/documents` - crear documento (respuesta `201 Created`)
- `GET /api/documents` - listar documentos
- `GET /api/documents/{id}` - obtener por id
- `PUT /api/documents/{id}` - actualizar
- `DELETE /api/documents/{id}` - eliminar
- `GET /api/documents/search?code=SOAT` - buscar por codigo
- `GET /api/documents/searchByApplicability?app=AM` - filtrar por aplicabilidad

Ejemplo `POST /api/documents`:

```json
{
  "code": "SOAT",
  "name": "Seguro Obligatorio",
  "applicability": "AM",
  "mandatory": "RR",
  "description": "Seguro obligatorio para ambos tipos"
}
```

### 5.2 Vehiculos

- `POST /api/vehicles` - crear vehiculo con documentos (respuesta `201 Created`)
- `GET /api/vehicles` - listar vehiculos
- `GET /api/vehicles/{id}` - obtener por id
- `PUT /api/vehicles/{id}` - actualizar
- `DELETE /api/vehicles/{id}` - eliminar

Busquedas:

- `GET /api/vehicles/byPlate?plate=ABC123`
- `GET /api/vehicles/searchByType?type=AUTOMOVIL`
- `GET /api/vehicles/searchByDocumentCode?code=SOAT`
- `GET /api/vehicles/searchByDocumentState?state=EN_VERIFICACION`

Agregar documento a vehiculo:

- `POST /api/vehicles/{id}/documents`

Ejemplo `POST /api/vehicles`:

```json
{
  "type": "AUTOMOVIL",
  "plate": "ABC123",
  "serviceType": "PR",
  "fuelType": "GASOLINA",
  "passengersCapacity": 5,
  "color": "#1A2B3C",
  "model": 2023,
  "brand": "Toyota",
  "line": "Corolla",
  "documents": [
	{
	  "document": { "id": 1 },
	  "issueDate": "2026-01-01",
	  "expiryDate": "2027-01-01"
	}
  ]
}
```

Ejemplo `POST /api/vehicles/{id}/documents`:

```json
{
  "document": { "id": 2 },
  "issueDate": "2026-01-01",
  "expiryDate": "2026-12-31"
}
```

## 6) Configuracion

Archivo: `src/main/resources/application.properties`

Propiedades actuales:

- `spring.datasource.url=jdbc:mysql://localhost:3306/vehiculos_db`
- `spring.datasource.username=root`
- `spring.datasource.password=`
- `spring.jpa.hibernate.ddl-auto=update`
- `spring.jpa.show-sql=true`

Ajusta usuario/password segun tu entorno local MySQL.

## 7) Ejecucion del proyecto

Desde la raiz del proyecto (`C:\Users\Acer\Documents\ProyectoPOO`):

```powershell
.\mvnw.cmd spring-boot:run
```

## 8) Pruebas

Para ejecutar pruebas unitarias:

```powershell
.\mvnw.cmd test
```

Actualmente existe prueba de servicio en `src/test/java/com/ProyectoPOO/ProyectoPOO/service/VehicleServiceTest.java` enfocada en reglas clave como:

- Falla cuando faltan documentos obligatorios.
- Estado inicial `EN_VERIFICACION` al crear vehiculo.

## 9) Errores comunes (ejemplos)

- Placa invalida segun tipo de vehiculo.
- Intentar crear vehiculo sin documentos.
- Documento inexistente al asociar por `id`.
- Documento duplicado para el mismo vehiculo.
- Fechas invalidas (`expiryDate` anterior a `issueDate`).

## 10) Checklist de cumplimiento del taller

- [x] CRUD de vehiculo.
- [x] Estado inicial de documentos en `EN_VERIFICACION` al crear vehiculo.
- [x] Bloqueo de creacion de vehiculo sin documentos.
- [x] Busqueda por placa.
- [x] Busqueda por tipo de vehiculo.
- [x] Busqueda por documento en comun (codigo de documento).
- [x] Busqueda por estado del documento asociado.
- [x] Servicio para agregar documento a un vehiculo.
- [x] CRUD de entidad parametrica de documentos.

## 11) Mejoras sugeridas

