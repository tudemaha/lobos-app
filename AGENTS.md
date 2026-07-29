# AGENTS.md

This file provides guidance for AI agents and developers working on this codebase.

---

## Project Overview

Lobos is a Spring Boot application featuring a REST API and a Thymeleaf web interface for managing vocabulary collections and grammar notes.

- **Backend**: Spring Boot (Java 21), Spring Security, JDBC API (raw SQL via `JdbcTemplate`), MySQL
- **Frontend**: Thymeleaf templating engine, Tailwind CSS via CDN
- **Auth**: Spring Security with JWT for REST APIs (`JwtAuthFilter`) & session support for web UI
- **IDs**: UUID (auto-generated in MySQL via `DEFAULT (UUID())`, stored as `VARCHAR(36)`)
- **Build System**: Maven (`./mvnw`)
- **Base Package**: `id.my.tudemaha.lobos`

---

## Codebase Architecture & Structure

```
src/main/java/id/my/tudemaha/lobos/
├── config/             # Security & Application configuration (SecurityConfig)
├── controller/
│   ├── api/            # REST Controllers (@RestController for JSON APIs)
│   └── web/            # MVC Web Controllers (@Controller for Thymeleaf views)
├── dto/                # Request & Response DTOs (request/, response/)
├── exception/          # Custom exceptions & GlobalExceptionHandler
├── mapper/             # Object mappers between entities and DTOs
├── model/              # Domain models (User, Collection, Grammar)
├── repository/         # Data access using JdbcTemplate (UserRepository, CollectionRepository, GrammarRepository)
├── security/           # JWT authentication filter & token utility (JwtAuthFilter, JwtService)
├── service/            # Business logic (UserService, CollectionService, GrammarService)
└── utils/              # Common utilities (Pagination, PasswordHasher)

src/main/resources/
├── application.properties
├── schema.sql           # MySQL DDL schema definitions
└── templates/           # Thymeleaf templates (layout.html, auth/, collections/, grammars/)
```

---

## Common Development Commands

### Build & Test
- **Compile**: `./mvnw compile`
- **Run Tests**: `./mvnw test`
- **Package JAR**: `./mvnw clean package`

### Run Application
- **Start Spring Boot server**: `./mvnw spring-boot:run`

---

## Backend & Database Conventions

### 1. Database & Persistence (`JdbcTemplate`)
- Database DDL schema is defined in `src/main/resources/schema.sql`.
- Persistence relies on raw SQL executed via `JdbcTemplate`. Do not introduce ORM/JPA mappings unless requested.
- Primary keys are 36-character UUID strings (`VARCHAR(36)`).

### 2. Architecture & Layer Responsibilities
- **Controllers (Dual Architecture)**:
  - **REST API Controllers** (`controller/api`): Annotated with `@RestController`. Handle JSON HTTP requests, validate payloads (`@Valid`), delegate execution to services, and return standardized responses (`HttpResponse<T>`). Secured with JWT tokens.
  - **MVC Web Controllers** (`controller/web`): Annotated with `@Controller`. Handle web page navigation, process form submissions bound to DTOs (`th:object`, `BindingResult`), delegate business logic to services, populate `Model` attributes, and return Thymeleaf view template paths.
- **Services**: Execute domain business logic, access control, and transaction rules. Reused by both REST and MVC controllers.
- **Repositories**: Execute raw SQL queries using `JdbcTemplate` and map database rows to Java models via `RowMapper`.
- **DTOs**: Keep strict separation between incoming `request` DTOs (e.g. `CreateGrammar`, `UserLogin`) and outgoing `response` DTOs (e.g. `HttpResponse`, `GrammarDetail`).

### 3. Error Handling
- Use specific domain exceptions (`NotFoundException`, `DuplicateEmailException`, `ForbiddenAccessException`, `LoginException`).
- REST exceptions are centrally intercepted and transformed into standardized JSON responses by `@RestControllerAdvice` in `GlobalExceptionHandler`.
- Web MVC exceptions are handled by rendering error pages or binding form validation errors to view models.

### 4. Security & Authentication
- **REST Endpoints (`/api/**`)**: Guarded by `JwtAuthFilter` with stateless JWT bearer token authentication.
- **MVC Web Routes**: Configured for session-based authentication rendering Thymeleaf views with CSRF protection enabled for form submissions.
- Public endpoints (e.g., `/api/auth/register`, `/api/auth/login`, login/register web routes) are explicitly configured in `SecurityConfig`.

---

## Frontend Rules & Guidelines

### Design System
- **Main Design**: Clean and minimalist layout for each page.
- **Font**: Garamond
- **Color Palette**: Monochrome with pastel blue (`#70A3C9`).
- **Responsiveness**: Responsive layout with mobile view optimization.

### Technology Stack
- **Templating**: Thymeleaf templates located in `src/main/resources/templates/`.
- **CSS**: Tailwind CSS via CDN (`https://cdn.tailwindcss.com`). No standalone build step required.
- **JavaScript**: Pure Vanilla JS for dynamic UI elements and modals. Avoid heavy JS frameworks.

### Tailwind Setup
Include this script tag in the `<head>` of templates or shared layouts (`layout.html`):
```html
<script src="https://cdn.tailwindcss.com"></script>
```

Prefer standard Tailwind scale utility classes (`w-80`, `p-4`, `text-sm`, etc.) over arbitrary values (`w-[347px]`).

### Layout & Page Composition
- Use `layout.html` as the shared base layout.
- Individual templates inject main content using Thymeleaf layout fragments (`th:replace` or `th:insert`).

### Thymeleaf Conventions
- Always declare required XML namespaces on `<html>`:
```html
  xmlns:th="http://www.thymeleaf.org"
```
- Render text using `th:text` rather than raw string interpolation in HTML nodes.
- Format dynamic links with `@` syntax: `th:href="@{/path/{id}(id=${item.id})}"`.
- Form binding uses `th:object` and `th:field`, paired with `BindingResult` in the controller.
- Standard loops use `th:each`, and conditionals use `th:if` / `th:unless`.
- Access controls use `sec:authorize="isAuthenticated()"` / `sec:authorize="hasRole('ADMIN')"`.

### CSRF Protection
Every POST form must include the CSRF token input parameter:
```html
<input type="hidden"
       th:name="${_csrf.parameterName}"
       th:value="${_csrf.token}"/>
```

### Form & Validation Error Handling
Display field-level validation messages next to relevant inputs:
```html
<input type="text" th:field="*{title}" class="..." />
<span th:if="${#fields.hasErrors('title')}" th:errors="*{title}" class="..."></span>
```
Display global form error summaries at the top of forms:
```html
<div th:if="${#fields.hasErrors('*')}" class="...">
    <ul class="list-disc list-inside">
        <li th:each="error : ${#fields.allErrors()}" th:text="${error}"></li>
    </ul>
</div>
```