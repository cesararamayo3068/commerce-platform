# Commerce Platform

Plataforma de comercio electrónico (catálogo + carrito de compras) construida con un stack moderno: backend Spring Boot con API REST documentada y frontend Angular de una sola página. Todo el proyecto corre aislado en Docker, sin depender de Java, Maven, Node ni Angular CLI instalados globalmente.

## Stack

**Backend**

- Java 21
- Spring Boot 3.5
- Maven (Maven Wrapper incluido)
- Spring Web, Spring Data JPA / Hibernate
- PostgreSQL 17
- Flyway
- Bean Validation
- Actuator
- OpenAPI / Swagger (springdoc)
- JUnit 5 / Mockito

**Frontend**

- Angular 21 (standalone components)
- TypeScript estricto
- Angular Router
- HttpClient
- Reactive Forms
- Signals (estado del carrito)
- SCSS propio (sin librerías de UI externas)
- Vitest (stack de testing oficial de Angular)

## Arquitectura

```
commerce-platform/
├── backend/          # API REST Spring Boot
│   ├── src/main/java/com/portfolio/commerce/
│   │   ├── domain/        # modelo de dominio (User, Product, Cart, CartItem)
│   │   ├── service/       # lógica de negocio (ProductService, CartService)
│   │   └── web/           # capa API (controllers, DTOs, errores, CORS)
│   ├── src/main/resources/db/migration/   # V1__create_initial_domain.sql
│   ├── src/test/          # tests unitarios (sin PostgreSQL externo)
│   ├── Dockerfile         # multi-stage (build + runtime)
│   └── pom.xml
├── frontend/         # SPA Angular
│   ├── src/app/
│   │   ├── core/          # configuración, modelos, servicios, utilidades
│   │   ├── shared/        # navbar, footer, toasts, product card, loading, empty state
│   │   └── features/      # products (catálogo, administración) y cart
│   ├── Dockerfile         # Node 22, dev server en 0.0.0.0:4200
│   └── package.json
├── compose.yaml      # stack: postgres + backend + frontend
├── .env.example      # plantilla de variables de entorno
└── README.md
```

El backend expone una API REST con contrato estable (DTOs explícitos, errores consistentes `ApiError`, paginación `PagedModel`/HAL). El frontend consume esa API real y centraliza la configuración (URL base y usuario demo) en `frontend/src/app/core/config/environment.ts`.

## Funcionalidades

**Catálogo de productos**

- Listado paginado de productos activos (`/products`)
- Cards con nombre, descripción, precio y estado
- Acción *Add to cart* directa desde el catálogo

**Administración de productos** (`/products/manage`)

- Listado paginado con estado y fecha de actualización
- Crear producto
- Editar producto
- Desactivar producto (baja lógica)

**Carrito de compras** (`/cart`)

- Crear carrito
- Ver carrito con items, subtotales y total
- Agregar productos
- Modificar cantidades
- Eliminar productos
- Cancelar carrito

El estado del carrito vive en `CartService` (signals). Solo el `cartId` se persiste en `localStorage` para sobrevivir al refresh; los objetos completos nunca se guardan. Si el carrito persistido fue cancelado o ya no existe, el estado local se limpia y se puede crear uno nuevo.

## Requisitos

- Docker Desktop (con Docker Compose v2)
- No se necesita Java, Maven, Node, npm ni Angular CLI globales

## Docker

El proyecto crea **exclusivamente** sus propios recursos:

| Recurso | Nombre |
|---|---|
| Proyecto Compose | `portfolio-commerce` |
| Contenedor PostgreSQL | `portfolio-commerce-postgres` |
| Contenedor Backend | `portfolio-commerce-backend` |
| Contenedor Frontend | `portfolio-commerce-frontend` |
| Volumen de datos | `portfolio-commerce-postgres-data` |
| Red | `portfolio-commerce-network` |

No se tocan recursos externos al proyecto.

### Levantar el stack

```bash
# 1. Crear .env a partir de la plantilla y cambiar POSTGRES_PASSWORD
copy .env.example .env        # PowerShell/CMD
cp .env.example .env          # Git Bash

# 2. Validar la configuración
docker compose config

# 3. Construir y levantar
docker compose up -d --build
```

El backend espera a que PostgreSQL esté `healthy`; el frontend espera al backend. La conexión interna entre servicios usa los nombres de red del proyecto (`postgres:5432`, `backend:8080`), nunca `localhost`.

### URLs

| Servicio | URL |
|---|---|
| Frontend | http://localhost:14200 |
| Backend API | http://localhost:18080/api |
| Swagger UI | http://localhost:18080/swagger-ui/index.html |
| Actuator health | http://localhost:18080/actuator/health |

### Puertos

| Servicio | Host | Container |
|---|---|---|
| PostgreSQL | `55432` | `5432` |
| Backend | `18080` | `8080` |
| Frontend | `14200` | `4200` |

### Detener / eliminar

```bash
docker compose stop        # detiene, conserva datos
docker compose down        # elimina containers y red de ESTE proyecto
docker compose down -v     # además elimina el volumen de datos de ESTE proyecto
```

No usar comandos globales (`docker system prune`, `docker volume prune`, etc.): podrían afectar recursos de otros proyectos.

## Usuario demo

La API todavía no tiene autenticación ni Users API. El frontend usa temporalmente `demoUserId = 1` para crear carritos, centralizado en `frontend/src/app/core/config/environment.ts` (y documentado en el README del frontend). Si el usuario `1` no existe en la base, la creación de carrito devuelve un error comprensible (`404 User not found`). Esto será reemplazado por JWT en una etapa posterior.

## API

### Products

Base path: `/api/products`

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/products` | Crea un producto (`active=true` por defecto). |
| `GET` | `/api/products/{id}` | Obtiene un producto por ID. |
| `GET` | `/api/products` | Listado paginado; opcionalmente `?active=true` filtra por activos. |
| `PUT` | `/api/products/{id}` | Actualiza un producto existente. |
| `DELETE` | `/api/products/{id}` | Baja lógica: cambia `active=false`, no borra el registro. |

### Carts

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

### Errores

Respuesta consistente `{timestamp, status, error, message, path}`; en validaciones se agrega `fieldErrors` con el detalle por campo. `404` para recursos inexistentes, `400` para body o parámetros inválidos, `409` para intentos de modificar un carrito no `ACTIVE` o agregar un producto inactivo, `500` para errores internos (sin stack traces).

### CORS

El backend permite orígenes explícitos configurados por entorno (`CORS_ALLOWED_ORIGINS`, por defecto `http://localhost:14200`). Nunca se usa el wildcard `*`.

## Swagger UI

Con el backend levantado:

- Swagger UI: http://localhost:18080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:18080/v3/api-docs

## Tests

**Backend** (sin Docker ni PostgreSQL externo):

```bash
cd backend
./mvnw test
```

**Frontend** (requiere Node 22):

```bash
cd frontend
npm ci
npm test
npm run build
```

## Estructura del frontend

```
frontend/src/app/
├── core/
│   ├── config/environment.ts      # apiUrl y demoUserId centralizados
│   ├── models/                    # Product, Cart, PagedModel, ApiError
│   ├── services/                  # ProductService, CartService
│   └── utils/error-handler.ts     # mensajes de error reutilizables
├── shared/
│   ├── components/navbar/         # branding + navegación + badge de carrito
│   ├── components/footer/
│   ├── components/toast/          # feedback success/error/info
│   ├── components/product-card/
│   ├── components/loading/
│   └── components/empty-state/
└── features/
    ├── products/catalog/          # /products
    ├── products/manage/           # /products/manage
    ├── products/product-form/     # formulario crear/editar
    └── cart/cart-page/            # /cart
```
