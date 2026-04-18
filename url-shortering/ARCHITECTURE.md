# Architecture Overview (Hexagonal)

This document describes the package organization and dependency rules for this project.

## 1) Layers and Responsibilities

- `br.com.systemdesign.urlshortening.domain`
  - Pure business model and rules.
  - No Spring, no HTTP, no JPA.

- `br.com.systemdesign.urlshortening.application`
  - Use cases, input/output ports, application models, and facades.
  - Orchestrates business flow by calling ports.
  - Depends on `domain`.

- `br.com.systemdesign.urlshortening.infrastructure`
  - Technical implementations (web, persistence, etc.).
  - Adapters for external systems/frameworks.
  - Depends on `application` and `domain`.

## 2) Package Map

- `domain/model`
  - Domain records/entities used by business logic.

- `application/model`
  - Application-level commands/views (not HTTP DTOs).

- `application/port/in`
  - Input ports (use cases) consumed by web adapters.

- `application/port/out`
  - Output ports implemented by infrastructure adapters.

- `application/service`
  - Facades/use-case orchestration.

- `service`
  - Current use-case implementations (application behavior).
  - Should depend on ports/models, not framework-specific web classes.

- `infrastructure/web/controller`
  - REST controllers.

- `infrastructure/web/dto`
  - HTTP request/response payloads.

- `infrastructure/web/error`
  - Global HTTP exception handling.

- `infrastructure/web/ratelimit`
  - Rate-limit web interceptor.

- `infrastructure/web/config`
  - Web adapter configuration.

- `infrastructure/persistence/jpa/entity`
  - JPA entities.

- `infrastructure/persistence/jpa/repository`
  - Spring Data repositories.

- `infrastructure/persistence`
  - Persistence adapters implementing output ports.

## 3) Dependency Rules

Allowed:
- `infrastructure -> application`
- `application -> domain`
- `service -> application/domain`

Not allowed:
- `domain -> application/infrastructure`
- `application -> infrastructure`
- `service -> infrastructure/web`

## 4) Request Flow

1. HTTP request arrives at `infrastructure/web/controller`.
2. Controller maps HTTP DTO to `application/model` command.
3. Controller calls `application/port/in` (via facade).
4. Use case/service calls `application/port/out`.
5. Infrastructure adapter (`infrastructure/persistence`) executes repository/JPA logic.
6. Result returns as `application/model` view.
7. Controller maps view to HTTP DTO response.

## 5) How to Add New Feature

1. Add/update input port in `application/port/in`.
2. Add command/view models in `application/model` if needed.
3. Implement behavior in service/facade.
4. If external access is needed, add output port in `application/port/out`.
5. Implement output port in `infrastructure/...`.
6. Add/update controller + HTTP DTO mapping.
7. Add tests for use case and web adapter.
