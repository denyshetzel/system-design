# URL Shortening Service

Spring Boot URL shortener with PostgreSQL, Flyway migrations, rate limiting, and performance scripts (JIT and Native).

## Start Here

- Architecture guide: [ARCHITECTURE.md](./ARCHITECTURE.md)
- Performance guide: [perf/README.md](./perf/README.md)

## Tech Stack

- Java 25
- Spring Boot 4
- PostgreSQL 17
- Flyway
- Docker / Docker Compose
- k6 (load test)

## Project Structure (high-level)

- `domain`: business model
- `application`: use cases, ports, facade
- `infrastructure/web`: controller, HTTP DTOs, error handling, rate limit
- `infrastructure/persistence`: JPA adapters, entities, repositories

## Running Locally (IDE)

1. Start PostgreSQL (Docker or local instance).
2. Ensure env vars are set (or rely on defaults from `application-local.yml`).
3. Run app with `local` profile (default if no profile is set).

Default local DB fallback:
- URL: `jdbc:postgresql://localhost:5432/shortering-db`
- User: `postgres`
- Password: `postgres`

## Running with Docker Compose

From `url-shortering` folder:

```powershell
docker compose up -d --build
```

Main services:
- JIT API: `http://localhost:8081`
- Native API: `http://localhost:8082`
- PostgreSQL: `localhost:5432`
- pgAdmin (dev profile): `localhost:5050`

## Profiles and Config

- Base config: `src/main/resources/application.yml`
- Local overrides: `src/main/resources/application-local.yml`
- Production overrides: `src/main/resources/application-prod.yml`

Key runtime flags:
- `SPRING_PROFILES_ACTIVE` (`local` or `prod`)
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `APP_BASE_URL`
- `APP_OBSERVABILITY_REQUEST_LOG_SAMPLE_PERCENT` (request log sampling, 0 to 100)

## API Base Path

Server context path:
- `/shortening/api`

Main endpoints (final paths):
- `POST /shortening/api/shortener`
- `GET /shortening/api/shortener/{shortUrl}`
- `GET /shortening/api/shortener/{shortUrl}/redirect`
- `DELETE /shortening/api/shortener/{shortUrl}`

Health endpoints:
- `GET /shortening/api/actuator/health`
- `GET /shortening/api/actuator/health/readiness`

OpenAPI/Swagger (enabled by config):
- `GET /shortening/api/api-docs`
- `GET /shortening/api/swagger-ui.html`

## Build and Test

From repository root:

```powershell
./mvnw -pl url-shortering test
```

If Maven wrapper is unavailable in your environment, use Docker Maven:

```powershell
docker run --rm -v "${PWD}:/workspace" -w /workspace maven:3.9.11-eclipse-temurin-25 mvn -pl url-shortering test
```

## Load Tests and Benchmarks

- k6 scripts and benchmark instructions:
  - [perf/README.md](./perf/README.md)
- JIT vs Native benchmark script:
  - `.\perf\benchmark-jit-vs-native.ps1`
