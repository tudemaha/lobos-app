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
│   ├── web/            # MVC Web Controllers (@Controller for Thymeleaf views)
│   └── mcp/            # MCP Tools (@McpTool components exposed over the MCP server, CollectionMcpTools, GrammarMcpTools)
├── dto/                # Request & Response DTOs (request/, response/)
├── exception/          # Custom exceptions & GlobalExceptionHandler
├── mapper/             # Object mappers between entities and DTOs
├── model/              # Domain models (User, Collection, Grammar, McpToken)
├── repository/         # Data access using JdbcTemplate (UserRepository, CollectionRepository, GrammarRepository, McpTokenRepository)
├── security/           # JWT & MCP authentication filters and token utilities (JwtAuthFilter, JwtService, McpAuthFilter)
├── service/            # Business logic (UserService, CollectionService, GrammarService, McpTokenService)
└── utils/              # Common utilities (Pagination, PasswordHasher)

src/main/resources/
├── application.properties
├── schema.sql           # MySQL DDL schema definitions
├── templates/           # Thymeleaf templates (layout.html, auth/, collections/, grammars/)
└── static/
    ├── index.html       # Public marketing/landing page (plain HTML, not Thymeleaf)
    ├── css/             # Shared stylesheets (main.css) + page-specific overrides (landing.css)
    ├── js/              # Shared script (main.js) + one file per page/feature (grammars.js, collections.js, ...)
    └── fonts/           # Static font assets (e.g. vimala.ttf for Balinese script)
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
- **Controllers (Triple Architecture)**:
  - **REST API Controllers** (`controller/api`): Annotated with `@RestController`. Handle JSON HTTP requests, validate payloads (`@Valid`), delegate execution to services, and return standardized responses (`HttpResponse<T>`). Secured with JWT tokens.
  - **MVC Web Controllers** (`controller/web`): Annotated with `@Controller`. Handle web page navigation, process form submissions bound to DTOs (`th:object`, `BindingResult`), delegate business logic to services, populate `Model` attributes, and return Thymeleaf view template paths.
  - **MCP Tools** (`controller/mcp`): `@Component` classes exposing `@McpTool`-annotated methods (Spring AI) as tools callable by AI clients over the embedded MCP server. Resolve the authenticated user from `SecurityContextHolder` (not `@AuthenticationPrincipal`, since tool methods aren't `@Controller` methods) and delegate straight into the same services as the REST/web controllers — no separate business logic. Secured with MCP tokens.
- **Services**: Execute domain business logic, access control, and transaction rules. Reused by REST, MVC, and MCP tool front doors.
- **Repositories**: Execute raw SQL queries using `JdbcTemplate` and map database rows to Java models via `RowMapper`.
- **DTOs**: Keep strict separation between incoming `request` DTOs (e.g. `CreateGrammar`, `UserLogin`) and outgoing `response` DTOs (e.g. `HttpResponse`, `GrammarDetail`).

### 3. Error Handling
- Use specific domain exceptions (`NotFoundException`, `DuplicateEmailException`, `ForbiddenAccessException`, `LoginException`).
- REST exceptions are centrally intercepted and transformed into standardized JSON responses by `@RestControllerAdvice` in `GlobalExceptionHandler`.
- Web MVC exceptions are handled by rendering error pages or binding form validation errors to view models.

### 4. Security & Authentication
- **REST Endpoints (`/api/**`)**: Guarded by `JwtAuthFilter` with stateless JWT bearer token authentication.
- **MCP Server (`/mcp/**`)**: Guarded by `McpAuthFilter` with stateless MCP token (`Bearer <token>`) authentication — a separate, opaque token type (managed via `McpTokenService`/`/api/tokens`/`/tokens`), independent from the login JWT so a leaked MCP token can't be used to change account credentials.
- **MVC Web Routes**: Configured for session-based authentication rendering Thymeleaf views with CSRF protection enabled for form submissions.
- `SecurityConfig` defines three ordered `SecurityFilterChain` beans, one per front door: `apiSecurityFilterChain` (`/api/**`, `@Order(1)`), `mcpSecurityFilterChain` (`/mcp/**`, `@Order(2)`), `webSecurityFilterChain` (everything else, `@Order(3)`).
- Public endpoints (e.g., `/api/auth/register`, `/api/auth/login`, login/register web routes) are explicitly configured in `SecurityConfig`.
- Static asset paths (`/css/**`, `/js/**`, `/images/**`, `/fonts/**`, `/webjars/**`) are `permitAll` in `webSecurityFilterChain` so they load on public pages (e.g. the landing page) without authentication. Add any new static asset directory here or it will 302-redirect to `/login`.

### 5. Search & Pagination
- List endpoints accept an `@ModelAttribute PaginationRequest` (`page`, `perPage`, `query`) bound automatically from query params on `GET` requests.
- `Pagination.buildPaginationRequest` normalizes `page`/`perPage` defaults; `query` (if non-blank) is applied as a `LIKE` filter in the repository layer (see `GrammarRepository.findAllByCollectionId`, `CollectionRepository.findAllByUserId`). Multi-column `LIKE` conditions must be wrapped in parentheses (`AND (word LIKE ? OR meaning LIKE ?)`) to avoid breaking the surrounding `AND` scoping.
- Search bars in templates are plain `GET` forms with a `name="query"` input; pagination links must carry the current `query` value forward (`th:href="@{...(page=...,query=${paginationRequest.query})}"`) so search state isn't lost when paging.

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

### CSS & JS: No Inline Blocks — Use `static/css` & `static/js`
Reusable styling/behavior lives in `src/main/resources/static/`, referenced via `<link>`/`<script src>` — never write inline `<style>` or `<script>` blocks in templates or `static/index.html`.
- `css/main.css`: shared, site-wide rules (buttons, `nav-link`, `focus-pastel`, the Balinese `@font-face`/`.font-balinese`). Linked once in `layout.html`.
- `css/landing.css`: overrides/rules scoped only to the public landing page (`static/index.html`), layered on top of `main.css`.
- `js/tailwind-config.js`: the shared `tailwind.config` object, loaded right after the Tailwind CDN script.
- `js/main.js`: cross-page behavior — nav dropdown/mobile menu toggles, `togglePwd`, and the shared confirm-modal helpers. Loaded globally from `layout.html`, and also included on `static/index.html`. Guard any DOM lookups for elements that may not exist on every page (e.g. `if (el) {...}`), since `main.js` is shared across pages with different markup.
- One JS file per page/feature for page-specific logic (`grammars.js`, `grammar-detail.js`, `collections.js`, `profile.js`), included via that page's `scripts` fragment.
- Server-rendered values a script needs (e.g. `collectionId`) stay in a tiny inline `th:inline="javascript"` snippet assigning a `const`, immediately followed by the external `<script src="...">` that reads it — Thymeleaf can't process external `static/js/*.js` files, so dynamic values can't live there.

### Confirmations & Alerts: Use the Shared Modal, Not `confirm()`/`alert()`
`layout.html` renders one shared `#confirm-modal` (message + Cancel/Confirm buttons) available on every Thymeleaf page. Trigger it from JS instead of native dialogs:
```js
showConfirmModal('Delete this item?', function() {
    // runs only if the user confirms
    document.getElementById('some-form').submit();
});
```
`static/index.html` (the plain landing page) has no `#confirm-modal` markup — `main.js` checks for its existence before wiring listeners, so it's safe to include there even though the helper isn't used.

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