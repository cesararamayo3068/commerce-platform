# commerce-platform — Etapa 2: Modelo de dominio inicial

Proyecto personal de portfolio. La **Etapa 0** validó el aislamiento total del entorno Docker personal (PostgreSQL 17 con recursos propios). La **Etapa 1** agregó el skeleton del backend **Spring Boot 3 / Java 21**. La **Etapa 2** implementa el **modelo de dominio inicial** (User, Product, Cart, CartItem) con su persistencia vía Flyway.

> **Estado:** Etapa 2 — PostgreSQL 17 + backend Spring Boot 3.5 (Java 21, Maven, JPA, Flyway, Actuator, OpenAPI) + modelo de dominio inicial. Frontend (Angular), Spring Security/JWT, promociones, checkout y CI/CD se agregan en etapas posteriores.

## Qué crea este proyecto (y qué NO toca)

Al levantar el proyecto, Docker Compose crea **exclusivamente**:

| Recurso | Nombre | Tipo |
|---|---|---|
| Proyecto Compose | `portfolio-commerce` | proyecto |
| Contenedor | `portfolio-commerce-postgres` | container |
| Contenedor | `portfolio-commerce-backend` | container |
| Volumen de datos | `portfolio-commerce-postgres-data` | named volume |
| Red | `portfolio-commerce-network` | network (bridge) |

**NO se tocan, reutilizan ni eliminan** recursos existentes: `jinja_docs`, `postgres-artico-pe`, ni ningún otro container, volume o network del entorno laboral. No hay bind mounts hacia carpetas de Windows ni hacia proyectos de trabajo.

## Requisitos

- Docker Desktop (con Docker Compose v2) en Windows.
- Nada más: no se necesita Java, Maven, Node, Angular ni PostgreSQL instalados globalmente.

## 1. Crear y configurar `.env`

1. Copiá la plantilla:
   - PowerShell/CMD: `copy .env.example .env`
   - Git Bash: `cp .env.example .env`
2. Editá `.env` y cambiá al menos `POSTGRES_PASSWORD` por una contraseña segura.
3. Opcional: ajustá `POSTGRES_USER`, `POSTGRES_DB` y `POSTGRES_PORT` (por defecto `55432`).

> `.env` está en `.gitignore`: nunca se versiona.

## 2. Validar el compose

Desde la raíz del proyecto (donde está `compose.yaml`):

```bash
docker compose config
```

Debe mostrar el proyecto `portfolio-commerce`, el servicio `postgres` con la imagen `postgres:17`, el puerto `55432:5432`, el volumen `portfolio-commerce-postgres-data` y la red `portfolio-commerce-network`.

> Si validás **sin** haber creado `.env`, verás un error indicando que `POSTGRES_PASSWORD` es obligatoria. Es el comportamiento esperado: creá `.env` y volvé a validar.

## 3. Levantar solamente este proyecto

```bash
docker compose up -d
```

Docker Desktop mostrará el proyecto `portfolio-commerce` con los servicios `postgres` y `backend`. El contenedor `portfolio-commerce-postgres` queda corriendo y PostgreSQL escucha en `localhost:55432`; el backend queda escuchando en `localhost:18080`.

## 4. Ver su estado

```bash
docker compose ps
```

## 5. Ver sus logs

```bash
docker compose logs -f postgres
docker compose logs -f backend
```

## 6. Detenerlo

```bash
docker compose stop
```

Detiene el contenedor pero **conserva** el volumen de datos y la red.

## 7. Volver a levantarlo

```bash
docker compose start
```

(o `docker compose up -d`). Los datos persisten en `portfolio-commerce-postgres-data`.

## 8. Eliminar exclusivamente sus recursos/datos

```bash
# Detiene y elimina SOLO los containers y la red de ESTE proyecto
docker compose down

# Igual que arriba, y además elimina el volumen de datos de ESTE proyecto
docker compose down -v
```

`docker compose down` (con o sin `-v`) solo afecta a los recursos del proyecto `portfolio-commerce`. **No** utilices comandos globales como `docker system prune`, `docker volume prune` o `docker network prune`: podrían afectar recursos de otros proyectos (incluidos los laborales).

## Etapa 1 — Backend Spring Boot

El backend es un skeleton profesional de **Spring Boot 3.5 / Java 21** (Maven) en `backend/`, con:

- Spring Web, Spring Data JPA, PostgreSQL Driver, Flyway, Bean Validation, Actuator y OpenAPI/Swagger.
- **Sin** dominio de negocio todavía (sin entidades, sin endpoints de negocio, sin Security/JWT): eso llega en etapas posteriores.
- Configuración 100% por variables de entorno (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`).
- Flyway como autoridad del esquema: Hibernate usa `ddl-auto: validate` y **no** crea ni modifica tablas. Aún no hay migraciones (no hay modelo de dominio); la carpeta `db/migration/` queda preparada.
- Maven Wrapper incluido (`mvnw` / `mvnw.cmd`): no dependés de ningún Maven global.

### Construir y levantar PostgreSQL + backend

```bash
docker compose up -d --build
```

El backend espera a que PostgreSQL esté `healthy` (`depends_on` con condición) y se conecta internamente por `postgres:5432` (nunca por `localhost:55432`).

### Consultar health

```bash
curl http://localhost:18080/actuator/health
```

Debe responder `{"status":"UP"}` (con detalles de `db` y `diskSpace`).

### Acceder a Swagger / OpenAPI

- Swagger UI: http://localhost:18080/swagger-ui.html
- OpenAPI JSON: http://localhost:18080/v3/api-docs

### Ver logs del backend

```bash
docker compose logs -f backend
```

### Detener exclusivamente este proyecto

```bash
docker compose down
```

### Correr el backend fuera de Docker (opcional, para desarrollo)

Con el PostgreSQL del proyecto ya levantado en Docker:

```bash
cd backend
set DB_HOST=localhost && set DB_PORT=55432 && set DB_NAME=commerce && set DB_USER=portfolio && set DB_PASSWORD=tu_password
mvnw.cmd spring-boot:run
```

> En PowerShell usá `$env:DB_HOST="localhost"` etc. Esto **no** toca tu Java/Maven global: usá el JDK 21 y Maven del contenedor, o un JDK/Maven portable bajo `C:\portfolio\tools`.

## Etapa 2 — Modelo de dominio

El modelo de dominio inicial vive en `com.portfolio.commerce.domain` y cubre las entidades del challenge original de Shopping Cart:

| Entidad | Tabla | Notas |
|---|---|---|
| `User` | `users` | `dni` obligatorio y único; `vip` identifica clientes VIP. Sin autenticación todavía. |
| `Product` | `products` | `price` como `BigDecimal` (NUMERIC(12,2), nunca double/float); `active` para el catálogo. |
| `Cart` | `carts` | Pertenece a un `User`; `status` es un enum (`ACTIVE`, `CHECKED_OUT`, `CANCELLED`) persistido como STRING. |
| `CartItem` | `cart_items` | `quantity` > 0; `UNIQUE(cart_id, product_id)` impide duplicar el mismo producto en el mismo carrito. |

### Decisiones de diseño

- **Relaciones unidireccionales** (`Cart -> User`, `CartItem -> Cart`, `CartItem -> Product`), todas `LAZY`: el modelo es simple, evita colecciones innecesarias y problemas de serialización/N+1.
- **Auditoría con callbacks JPA** (`@PrePersist`/`@PreUpdate`) en la clase base `AuditableEntity`: cero configuración extra y suficiente para `createdAt`/`updatedAt`. Si más adelante se necesita auditoría de usuario (`createdBy`), se migra a Spring Data auditing.
- **Sin `equals`/`hashCode` customizados**: las entidades usan identidad de objeto, lo que evita problemas con proxies y lazy loading.
- **Flyway como autoridad del schema**: `V1__create_initial_domain.sql` crea las tablas con PK, FK, NOT NULL, UNIQUE, CHECK e índices. Hibernate corre con `ddl-auto: validate` y valida que las entidades coincidan con el schema; nunca lo modifica.
- **Doble línea de defensa**: Bean Validation (`@NotBlank`, `@DecimalMin`, `@Min`) valida en la capa de aplicación; los CHECK constraints de PostgreSQL respaldan a nivel de base.
- **Repositories** Spring Data JPA mínimos por entidad; `UserRepository.findByDni` es el único método derivado (útil para validar el modelo).

### Migración Flyway

`backend/src/main/resources/db/migration/V1__create_initial_domain.sql` — crea `users`, `products`, `carts` y `cart_items` (compatible PostgreSQL 17). Se aplica automáticamente al arrancar el backend.

### Tests

- `EntityValidationTest`: validación de beans (dni obligatorio, precio no negativo, cantidad ≥ 1).
- `CommerceBackendApplicationTests`: `contextLoads()` (smoke test de contexto, sin base externa).

```bash
cd backend
./mvnw test
```

> La suite de tests corre sin Docker ni PostgreSQL externo. Las migraciones Flyway y la validación del schema (`ddl-auto: validate`) se ejercitan al arrancar la aplicación, por ejemplo con Docker Compose.

## Conexión a PostgreSQL

| Parámetro | Valor |
|---|---|
| Host | `localhost` |
| Puerto | `55432` |
| Usuario | el de `POSTGRES_USER` (default `portfolio`) |
| Base de datos | el de `POSTGRES_DB` (default `commerce`) |
| Contraseña | la de `POSTGRES_PASSWORD` |

## Estructura

```
commerce-platform/
├── backend/          # API Spring Boot 3.5 / Java 21
│   ├── src/main/java/com/portfolio/commerce/
│   │   ├── CommerceBackendApplication.java
│   │   └── domain/                        # modelo de dominio (Etapa 2)
│   │       ├── AuditableEntity.java       # base auditable (createdAt/updatedAt)
│   │       ├── user/                      # User + UserRepository
│   │       ├── product/                   # Product + ProductRepository
│   │       └── cart/                      # CartStatus, Cart, CartItem + repositories
│   ├── src/main/resources/application.yml      # config por env vars
│   ├── src/main/resources/db/migration/        # V1__create_initial_domain.sql
│   ├── src/test/                               # contextLoads + tests de dominio
│   ├── Dockerfile                              # multi-stage (build + runtime)
│   ├── pom.xml                                 # Maven (Java 21, Boot 3.5.16)
│   ├── mvnw / mvnw.cmd / .mvn/                 # Maven Wrapper
├── frontend/         # reservado: SPA Angular (etapas futuras)
├── compose.yaml      # stack: postgres (Etapa 0) + backend (Etapa 1)
├── .env.example      # plantilla de variables de entorno
├── .gitignore
└── README.md
```
