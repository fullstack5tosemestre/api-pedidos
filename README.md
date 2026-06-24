# API Pedidos — SmartLogix

Microservicio REST responsable de la gestión de pedidos de SmartLogix. Se comunica con `api-inventario` para enriquecer los pedidos con información de productos. Construido con Spring Boot 3.3.4, Spring Data JPA, MySQL y Liquibase. Puerto: **8081**.

Swagger UI: `http://localhost:8081/swagger-ui.html`

---

## Responsabilidad

Este microservicio gestiona exclusivamente el dominio de pedidos:

- **Pedidos** (`/api/v1/orders`): creación, consulta por estado, actualización de estado y eliminación.
- Cada pedido contiene una lista de productos con cantidades.
- Se comunica con `api-inventario` (a través del gateway) para resolver los detalles de los productos al consultar pedidos.

---

## Arquitectura

```
api-pedidos
├── Controller (REST)
│   └── OrderController       /api/v1/orders
├── Service (lógica)
│   └── OrderService          Lógica de negocio + llamadas a api-inventario
├── Repository (JPA)
│   └── OrderRepository
├── Model (entidades JPA)
│   ├── Order                 Pedido principal
│   └── ProductQuantity       Ítem embebido (producto + cantidad)
├── DTO (transferencia)
│   ├── OrderResponseDTO      Respuesta enriquecida con datos del producto
│   ├── ProductDTO            Datos del producto (de api-inventario)
│   ├── BranchDTO
│   └── WarehouseDTO
└── config/
    └── RestTemplateConfig    Configuración del cliente HTTP
```

### Comunicación inter-servicio

`api-pedidos` consulta a `api-inventario` a través del API Gateway para obtener los detalles de los productos incluidos en cada pedido. La URL base se configura con la variable de entorno `INVENTORY_API_BASE_URL` (por defecto: `http://localhost:9090/api/v1/products`).

### Patrones de diseño aplicados

**1. Strategy**
`OrderService` aplica estrategias diferentes de procesamiento según el estado del pedido (PENDIENTE, EN_PROCESO, ENTREGADO, CANCELADO). El método `updateStatus` valida las transiciones de estado permitidas, encapsulando el comportamiento variable en función del estado actual.

**2. DTO (Data Transfer Object)**
Se usa un conjunto de DTOs (`OrderResponseDTO`, `ProductDTO`, `BranchDTO`) para desacoplar la representación externa de los datos de las entidades internas JPA. Los DTOs permiten componer respuestas enriquecidas (pedido + detalle de producto) sin exponer la estructura de la base de datos ni crear dependencias de entidades entre microservicios.

---

## Estructura de directorios

```
api-pedidos/
├── src/
│   ├── main/
│   │   ├── java/com/smartlogix/pedidos/
│   │   │   ├── PedidosApplication.java
│   │   │   ├── config/
│   │   │   │   └── RestTemplateConfig.java
│   │   │   ├── controller/
│   │   │   │   └── OrderController.java
│   │   │   ├── dto/
│   │   │   │   ├── OrderResponseDTO.java
│   │   │   │   ├── OrderProductDTO.java
│   │   │   │   ├── ProductDTO.java
│   │   │   │   ├── BranchDTO.java
│   │   │   │   └── WarehouseDTO.java
│   │   │   ├── model/
│   │   │   │   ├── Order.java
│   │   │   │   └── ProductQuantity.java
│   │   │   ├── repository/
│   │   │   │   └── OrderRepository.java
│   │   │   └── service/
│   │   │       └── OrderService.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/changelog/
│   └── test/
│       ├── java/
│       └── resources/application-test.properties
├── Dockerfile
├── pom.xml
└── README.md
```

---

## Modelos de datos

### Order (Pedido)

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | Identificador autoincremental |
| customerName | String | Nombre del cliente |
| status | String | Estado: PENDIENTE / EN_PROCESO / ENTREGADO / CANCELADO |
| createdAt | LocalDateTime | Fecha y hora de creación |
| productList | List\<ProductQuantity\> | Lista de productos con cantidades |

### ProductQuantity (ítem embebido)

| Campo | Tipo | Descripción |
|-------|------|-------------|
| productId | Long | ID del producto en api-inventario |
| quantity | int | Cantidad solicitada |

---

## Endpoints REST

### `/api/v1/orders`

| Método | Ruta | Descripción | Respuesta |
|--------|------|-------------|-----------|
| GET | `/api/v1/orders` | Listar todos los pedidos (enriquecidos) | 200 / 204 |
| GET | `/api/v1/orders/{id}` | Obtener pedido por ID | 200 / 404 |
| GET | `/api/v1/orders/status/{status}` | Filtrar pedidos por estado | 200 |
| POST | `/api/v1/orders` | Crear nuevo pedido | 201 / 400 |
| PATCH | `/api/v1/orders/{id}/status?status=ENTREGADO` | Actualizar estado del pedido | 200 / 404 |
| DELETE | `/api/v1/orders/{id}` | Eliminar pedido | 204 / 404 |

**Ejemplo POST `/api/v1/orders`:**

```json
{
  "customerName": "Juan Pérez",
  "status": "PENDIENTE",
  "createdAt": "2026-06-23T10:30:00",
  "productList": [
    { "productId": 1, "quantity": 2 },
    { "productId": 3, "quantity": 1 }
  ]
}
```

**Respuesta 201:**

```json
{
  "id": 10,
  "customerName": "Juan Pérez",
  "status": "PENDIENTE",
  "createdAt": "2026-06-23T10:30:00",
  "productList": [
    { "productId": 1, "quantity": 2 },
    { "productId": 3, "quantity": 1 }
  ]
}
```

**Ejemplo GET `/api/v1/orders/10` (respuesta enriquecida con datos de producto):**

```json
{
  "id": 10,
  "customerName": "Juan Pérez",
  "status": "PENDIENTE",
  "createdAt": "2026-06-23T10:30:00",
  "products": [
    {
      "productId": 1,
      "quantity": 2,
      "product": {
        "id": 1,
        "name": "Monitor LG 27\"",
        "sku": "MON-LG-27",
        "stock": 8
      }
    }
  ]
}
```

---

## Dependencias principales (`pom.xml`)

| Artefacto | Versión | Propósito |
|-----------|---------|-----------|
| spring-boot-starter-parent | 3.3.4 | BOM de Spring Boot |
| spring-boot-starter-web | — | API REST con Spring MVC |
| spring-boot-starter-webflux | — | WebClient para llamadas a api-inventario |
| spring-boot-starter-data-jpa | — | Persistencia con Hibernate |
| mysql-connector-j | — | Driver MySQL |
| liquibase-core | — | Migraciones de base de datos |
| lombok | — | Reducción de código boilerplate |
| springdoc-openapi-starter-webmvc-ui | 2.5.0 | Swagger UI |
| h2 (test) | — | Base de datos en memoria para pruebas |

---

## Instalación y ejecución

**Requisitos previos:** Java 17, Maven 3.8+, MySQL 8.

```bash
# 1. Compilar
./mvnw clean package -DskipTests

# 2. Ejecutar
./mvnw spring-boot:run
```

Variables de entorno disponibles:

| Variable | Por defecto | Descripción |
|----------|-------------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/db_pedidos?...` | URL MySQL |
| `SPRING_DATASOURCE_USERNAME` | `root` | Usuario MySQL |
| `SPRING_DATASOURCE_PASSWORD` | *(vacío)* | Contraseña MySQL |
| `INVENTORY_API_BASE_URL` | `http://localhost:9090/api/v1/products` | URL de api-inventario (vía gateway) |

### Con Docker

```bash
docker build -t smartlogix-pedidos .
docker run -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host:3306/db_pedidos?createDatabaseIfNotExist=true \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=root \
  -e INVENTORY_API_BASE_URL=http://api-gateway:9090/api/v1/products \
  smartlogix-pedidos
```

---

## Pruebas

```bash
# Ejecutar pruebas unitarias (usan H2 en memoria)
./mvnw test

# Reportes en:
# target/surefire-reports/
```

La cobertura objetivo es ≥ 60% sobre las funcionalidades del servicio.

---

## Persistencia

Las migraciones son gestionadas por **Liquibase** (`src/main/resources/db/changelog/db.changelog-master.yaml`). Las pruebas utilizan el perfil `test` con H2 en memoria (`src/test/resources/application-test.properties`).

---

## Estrategia de branching

| Rama | Propósito |
|------|-----------|
| `main` | Código en producción |
| `develop` | Integración de cambios |
| `feature/*` | Nuevas funcionalidades |
| `fix/*` | Corrección de bugs |
