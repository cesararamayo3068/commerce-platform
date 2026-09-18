# commerce-platform

Plataforma de comercio electrónico (shopping cart) construida con un stack moderno y aislado por completo del entorno de trabajo: todos sus recursos Docker, puertos y datos son propios del proyecto.

## Stack

- Java 21
- Spring Boot 3.5
- Maven (Maven Wrapper incluido)
- Spring Web, Spring Data JPA / Hibernate
- PostgreSQL 17
- Flyway
- Bean Validation
- Actuator
- OpenAPI / Swagger (springdoc)
- Docker / Docker Compose

## Entidades

| Entidad | Tabla | Notas |
|---|---|---|
| `User` | `users` | Cliente del sistema; `dni` obligatorio y único; `vip` identifica clientes VIP. |
| `Product` | `products` | Producto del catálogo; `price` como `BigDecimal` (NUMERIC(12,2)); `active` para el catálogo. |
| `Cart` | `carts` | Carrito de un usuario; `status` enum (`ACTIVE`, `CHECKED_OUT`, `CANCELLED`) persistido como STRING. |
| `CartItem` | `cart_items` | Producto dentro de un carrito; `quantity > 0`; `UNIQUE(cart_id, product_id)`. |

## Product Catalog API

Base path: `/api/products`

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/products` | Crea un producto (`active=true` por defecto). |
| `GET` | `/api/products/{id}` | Obtiene un producto por ID. |
| `GET` | `/api/products` | Listado paginado; opcionalmente `?active=true` filtra por activos. |
| `PUT` | `/api/products/{id}` | Actualiza un producto existente. |
| `DELETE` | `/api/products/{id}` | Baja lógica: cambia `active=false`, no borra el registro. |

### Paginación y filtro

Paginación estándar de Spring Data: `?page=0&size=20&sort=name,asc`.

```bash
curl "http://localhost:18080/api/products?active=true&page=0&size=20&sort=name,asc"
```

La respuesta paginada usa `PagedModel` (estructura JSON estable):

```json
{
  "content": [ { "id": 1, "name": "Teclado", "price": 99.99, "active": true } ],
  "page": { "size": 20, "number": 0, "totalElements": 42, "totalPages": 3 }
}
```

### Soft-delete

`DELETE /api/products/{id}` no borra el registro: cambia `active` a `false`. El producto deja de aparecer en `?active=true` pero se conserva en la base.

### Ejemplos

```bash
# Crear
curl -X POST http://localhost:18080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Teclado mecanico", "description": "Switch red", "price": 99.99}'

# Obtener por id
curl http://localhost:18080/api/products/1

# Listar activos (paginado)
curl "http://localhost:18080/api/products?active=true&page=0&size=20"

# Actualizar
curl -X PUT http://localhost:18080/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{"name": "Teclado RGB", "description": "Switch red", "price": 129.00, "active": true}'

# Baja lógica
curl -X DELETE http://localhost:18080/api/products/1
```

### Errores

Respuesta consistente `{timestamp, status, error, message, path}`; en validaciones se agrega `fieldErrors` con el detalle por campo. `404` para producto inexistente, `400` para body o parámetros inválidos (incluidos paginación/sort), `500` para errores internos (sin stack traces).

## Cart API

Base path: `/api/carts`

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/carts` | Crea un carrito `ACTIVE` para un usuario, sin items. |
| `GET` | `/api/carts/{id}` | Obtiene un carrito con sus items, subtotales y total. |
| `GET` | `/api/carts` | Listado paginado; opcionalmente `?userId=` y/o `?status=` filtran. |
| `POST` | `/api/carts/{cartId}/items` | Agrega un producto; si ya está en el carrito, incrementa la cantidad. |
| `PUT` | `/api/carts/{cartId}/items/{productId}` | Fija la cantidad al valor exacto indicado (no es un incremento). |
| `DELETE` | `/api/carts/{cartId}/items/{productId}` | Elimina el item por completo (no decrementa la cantidad). |
| `DELETE` | `/api/carts/{id}` | Cancelación lógica: `status = CANCELLED`. |

### Estados del carrito

`ACTIVE` → `CANCELLED` (cancelación) o `CHECKED_OUT` (reservado para checkout en etapas posteriores).

Solo un carrito `ACTIVE` puede modificarse. Intentar agregar/eliminar items o cambiar cantidades en un carrito `CANCELLED` o `CHECKED_OUT` devuelve `409 Conflict`.

### Reglas de contenido

- Un producto solo puede aparecer una vez por carrito (`UNIQUE(cart_id, product_id)`); agregar un producto ya presente incrementa la cantidad existente.
- `quantity` debe ser mayor que 0.
- Solo se agregan productos activos (`active=true`).
- Eliminar un item lo quita por completo; no decrementa la cantidad.

### Cálculo de totales

- Subtotal del item = `product.price * quantity`, calculado con `BigDecimal`.
- Total del carrito = suma de los subtotales de los items (sin descuentos en esta etapa).
- El total se calcula a partir de los items y no se persiste.

### Ejemplos

```bash
# Crear carrito
curl -X POST http://localhost:18080/api/carts \
  -H "Content-Type: application/json" \
  -d '{"userId": 1}'

# Obtener carrito
curl http://localhost:18080/api/carts/1

# Listar carritos activos de un usuario (paginado)
curl "http://localhost:18080/api/carts?userId=1&status=ACTIVE&page=0&size=20"

# Agregar producto (si ya está, incrementa cantidad)
curl -X POST http://localhost:18080/api/carts/1/items \
  -H "Content-Type: application/json" \
  -d '{"productId": 5, "quantity": 2}'

# Fijar cantidad exacta
curl -X PUT http://localhost:18080/api/carts/1/items/5 \
  -H "Content-Type: application/json" \
  -d '{"quantity": 4}'

# Eliminar item
curl -X DELETE http://localhost:18080/api/carts/1/items/5

# Cancelar carrito (baja lógica)
curl -X DELETE http://localhost:18080/api/carts/1
```

### Errores

Mismo formato `ApiError` que Products. `404` para carrito/usuario/producto/item inexistente, `400` para body o parámetros inválidos, `409` para intentos de modificar un carrito no `ACTIVE` o agregar un producto inactivo.

## Flyway

Las migraciones viven en `backend/src/main/resources/db/migration/`. `V1__create_initial_domain.sql` crea el schema inicial (`users`, `products`, `carts`, `cart_items`). Flyway es la autoridad del schema: Hibernate corre con `ddl-auto: validate` y nunca crea ni modifica tablas.

## Docker Compose

El proyecto crea **exclusivamente** sus propios recursos:

| Recurso | Nombre |
|---|---|
| Proyecto Compose | `portfolio-commerce` |
| Contenedor | `portfolio-commerce-postgres` |
| Contenedor | `portfolio-commerce-backend` |
| Volumen de datos | `portfolio-commerce-postgres-data` |
| Red | `portfolio-commerce-network` |

No se tocan recursos externos al proyecto (ni bind mounts hacia carpetas de Windows).

### Requisitos

- Docker Desktop (con Docker Compose v2) en Windows.
- No se necesita Java, Maven, Node ni PostgreSQL instalados globalmente.

### Levantar

```bash
# 1. Crear .env a partir de la plantilla y cambiar POSTGRES_PASSWORD
copy .env.example .env        # PowerShell/CMD
cp .env.example .env          # Git Bash

# 2. Validar la configuracion
docker compose config

# 3. Construir y levantar
docker compose up -d --build
```

El backend espera a que PostgreSQL esté `healthy` y se conecta internamente por `postgres:5432` (nunca por `localhost:55432`).

### Puertos

| Servicio | Host | Container |
|---|---|---|
| PostgreSQL | `55432` | `5432` |
| Backend | `18080` | `8080` |

### Health

```bash
curl http://localhost:18080/actuator/health
```

### Detener / eliminar

```bash
docker compose stop        # detiene, conserva datos
docker compose down        # elimina containers y red de ESTE proyecto
docker compose down -v     # ademas elimina el volumen de datos de ESTE proyecto
```

No usar comandos globales (`docker system prune`, `docker volume prune`, etc.): podrían afectar recursos de otros proyectos.

## Swagger UI

Con el backend levantado:

- Swagger UI: http://localhost:18080/swagger-ui.html
- OpenAPI JSON: http://localhost:18080/v3/api-docs

## Tests

La suite corre sin Docker ni PostgreSQL externo:

```bash
cd backend
./mvnw test
```

- `ProductServiceTest` — lógica de negocio (Mockito).
- `ProductControllerTest` — endpoints, validaciones y errores (MockMvc).
- `EntityValidationTest` — validación de beans del dominio.
- `CommerceBackendApplicationTests` — smoke test de contexto.

Las migraciones Flyway y la validación del schema (`ddl-auto: validate`) se ejercitan al arrancar la aplicación, por ejemplo con Docker Compose.

## Estructura

```
commerce-platform/
├── backend/          # API Spring Boot 3.5 / Java 21
│   ├── src/main/java/com/portfolio/commerce/
│   │   ├── CommerceBackendApplication.java
│   │   ├── domain/                        # modelo de dominio
│   │   │   ├── AuditableEntity.java       # base auditable (createdAt/updatedAt)
│   │   │   ├── user/                      # User + UserRepository
│   │   │   ├── product/                   # Product + ProductRepository
│   │   │   └── cart/                      # CartStatus, Cart, CartItem + repositories
│   │   ├── service/                       # ProductService, CartService + excepciones
│   │   └── web/                           # capa API
│   │       ├── ProductController.java
│   │       ├── CartController.java
│   │       ├── dto/                       # DTOs de Product y Cart
│   │       └── error/                     # ApiError + GlobalExceptionHandler
│   ├── src/main/resources/application.yml      # config por env vars
│   ├── src/main/resources/db/migration/        # V1__create_initial_domain.sql
│   ├── src/test/                               # tests unitarios (sin PostgreSQL externo)
│   ├── Dockerfile                              # multi-stage (build + runtime)
│   ├── pom.xml                                 # Maven (Java 21, Boot 3.5.16)
│   ├── mvnw / mvnw.cmd / .mvn/                 # Maven Wrapper
├── frontend/         # reservado: SPA Angular
├── compose.yaml      # stack: postgres + backend
├── .env.example      # plantilla de variables de entorno
├── .gitignore
└── README.md
```
r
├── compose.yaml      # stack: postgres + backend
├── .env.example      # plantilla de variables de entorno
├── .gitignore
└── README.md
```
