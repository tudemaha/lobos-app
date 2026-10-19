# Lobos

A vocabulary and grammar management application built with **Spring Boot**. Lobos provides both a RESTful JSON API and a Thymeleaf-based web interface for organizing vocabulary collections and grammar notes.

---

## Features

- 📚 **Collections** — Group vocabulary entries into named, color-coded collections
- 📝 **Grammar Notes** — Store words with their meanings, examples, and star/favorite them
- 🔐 **Authentication & Sessions** — Register, log in, refresh token rotation, and multi-device session revocation
- ✉️ **Email Verification & Password Reset** — Email verification on signup and email change, plus secure token-based password reset flows
- 🔄 **Offline-First & Delta Sync** — Client-generated UUID support, idempotent mutations, soft deletion, and cursor-based delta sync (`/api/sync`)
- 🍪 **Remember Me** — Optional persistent login (JDBC-backed, 30-day rotating token) so the web session survives a closed browser
- 🌐 **Web UI** — Full-featured browser interface built with Thymeleaf and Tailwind CSS
- 🔌 **REST API** — Stateless JSON API with standardized error responses (`HttpResponse<T>`) secured with JWT bearer tokens
- 🤖 **MCP Server** — Remote MCP server (Streamable HTTP) exposing collections and grammars as tools for AI clients, secured with scoped MCP tokens
- 📄 **Pagination & Search** — Navigate and search large lists with built-in pagination

---

## Tech Stack

| Layer        | Technology                                                |
| ------------ | --------------------------------------------------------- |
| Language     | Java 21                                                   |
| Framework    | Spring Boot 4.1.0                                         |
| Web / MVC    | Spring Web MVC, Thymeleaf                                 |
| Security     | Spring Security (JWT for API, session for Web)            |
| Database     | MySQL                                                     |
| Persistence  | Spring JDBC (`JdbcTemplate`) — raw SQL, no ORM            |
| Migrations   | Flyway 11                                                 |
| Mail         | Spring Mail (`JavaMailSender`)                            |
| Build Tool   | Maven (`./mvnw`) with Spotless (Palantir Java Format)     |
| Frontend CSS | Tailwind CSS via CDN                                      |
| Password     | jBCrypt                                                   |
| JWT          | JJWT 0.12.6                                               |
| MCP Server   | Spring AI MCP Server (WebMVC, Streamable HTTP)            |

---

## Project Structure

```
src/main/java/id/my/tudemaha/lobos/
├── config/             # Security & application configuration (SecurityConfig, OpenApiConfig)
├── controller/
│   ├── api/            # REST controllers (@RestController: AuthController, ProfileController, CollectionController, GrammarController, McpTokenController, SyncController)
│   ├── web/            # MVC controllers (@Controller: WebAuthController, WebProfileController, WebCollectionController, WebGrammarController, WebMcpTokenController)
│   └── mcp/            # MCP tools (@McpTool components exposed over the MCP server)
├── dto/
│   ├── request/        # Incoming request DTOs
│   └── response/       # Outgoing response DTOs (incl. HttpResponse envelope)
├── exception/          # Custom exceptions & GlobalExceptionHandler
├── mapper/             # Entity ↔ DTO mappers
├── model/              # Domain models (User, Collection, Grammar, McpToken, RefreshToken, EmailVerificationToken)
├── repository/         # Data access via JdbcTemplate
├── security/           # JWT/MCP filters, remember-me (JdbcPersistentTokenRepository, AppUserDetailsService) & token services
├── service/            # Business logic (UserService, CollectionService, GrammarService, McpTokenService, RefreshTokenService, EmailService, SyncService)
└── utils/              # Pagination, Hasher, SyncCursor, TokenUtil

src/main/resources/
├── application.properties   # App configuration (DB, JWT, Flyway, Mail, MCP)
├── db/migration/            # Flyway SQL migrations (V1__baseline.sql .. V4__sync_support.sql)
├── static/
│   ├── css/main.css         # Global styles
│   ├── fonts/               # Custom fonts (vimala.ttf)
│   ├── js/                  # Vanilla JS per-page scripts (incl. mcp.js, profile.js)
│   └── index.html           # Static landing page
└── templates/               # Thymeleaf HTML templates
    ├── layout.html           # Shared base layout
    ├── auth/                 # login, register, profile, verify-email, forgot-password, reset-password
    ├── mcp/                  # index.html (MCP token management)
    ├── collections/          # index.html
    └── grammars/             # index.html, detail.html
```

---

## Database Schema

Database migrations are managed automatically by **Flyway**:

```sql
users                     (id, first_name, last_name, email, password, is_verified, created_at, updated_at)
persistent_logins         (series, user_id, token, last_used)
refresh_tokens            (id, user_id → users.id, token_hash, expires_at, created_at, revoked_at)
email_verification_tokens (id, user_id → users.id, token_hash, type, pending_email, expires_at, created_at, used_at)
mcp_tokens                (id, user_id → users.id, name, token, created_at, last_used_at)
collections               (id, name, color, user_id → users.id, created_at, updated_at, deleted_at, version)
grammars                  (id, word, meaning, example, is_starred, collection_id → collections.id, created_at, updated_at, deleted_at, version)
```

Primary keys are `VARCHAR(36)` UUIDs, supporting both client-generated UUIDs on creation and MySQL default generation (`DEFAULT (UUID())`). Collections and grammars use soft deletion (`deleted_at`) and versioning for offline delta synchronization.

---

## Getting Started

### Prerequisites

- Java 21+
- MySQL database
- Maven (or use the included `./mvnw` wrapper)

### 1. Clone the repository

```bash
git clone <repository-url>
cd lobos
```

### 2. Configure the application

Edit `src/main/resources/application.properties` (or set environment variables):

```properties
spring.datasource.url=jdbc:mysql://<host>:<port>/<database>
spring.datasource.username=<db_user>
spring.datasource.password=<db_password>

jwt.secret=<your_strong_secret_key>
jwt.access-ttl=15m
jwt.refresh-ttl=30d

app.remember-me.key=<your_strong_secret_key>
app.base-url=http://localhost:8080

spring.mail.host=<smtp_host>
spring.mail.port=587
spring.mail.username=<email_user>
spring.mail.password=<email_password>
```

> ⚠️ **Never commit `application.properties` with real credentials to version control.**

### 3. Run the application

Database migrations execute automatically via Flyway on startup:

```bash
./mvnw spring-boot:run
```

The application starts at **`http://localhost:8080`**.

---

## Docker

### Run with Docker Compose (prebuilt image)

The prebuilt image is published to GitHub Container Registry at `ghcr.io/tudemaha/lobos:latest`. Fill in your database and JWT values in `docker-compose.yml`, then start it:

```yaml
services:
  lobos:
    restart: on-failure
    image: ghcr.io/tudemaha/lobos:latest
    environment:
      - DB_URL=jdbc:mysql://<host>:<port>/<database>
      - DB_USERNAME=<username>
      - DB_PASSWORD=<password>
      - JWT_SECRET=<secret>
      - REMEMBER_ME_KEY=<key>
    ports:
      - 127.0.0.1:8080:8080
```

```bash
docker compose up -d
```

The app will be available at **`http://localhost:8080`**.

### Build the image locally

```bash
docker build -t lobos .
```

### Run the image directly

```bash
docker run -d \
  -p 8080:8080 \
  -e DB_URL=jdbc:mysql://<host>:<port>/<database> \
  -e DB_USERNAME=<username> \
  -e DB_PASSWORD=<password> \
  -e JWT_SECRET=<secret> \
  -e REMEMBER_ME_KEY=<key> \
  lobos
```

> ⚠️ **Never commit real database credentials, JWT secrets, or the remember-me key into `docker-compose.yml` or version control.**

---

## Usage

### Web Interface

| Route               | Description                                    |
| ------------------- | ---------------------------------------------- |
| `/`                 | Landing page                                   |
| `/login`            | Log in to your account                         |
| `/register`         | Create a new account                           |
| `/verify-email`     | Verify account email address via token link    |
| `/forgot-password`  | Request password reset email                   |
| `/reset-password`   | Reset password with emailed token              |
| `/profile`          | View user profile                              |
| `/profile/update`   | Update profile name details                    |
| `/profile/email`    | Request email address update with verification |
| `/profile/password` | Update account password                        |
| `/collections`      | Manage your vocabulary collections             |
| `/grammars`         | Browse and manage grammar notes                |
| `/grammars/{id}`    | View grammar note detail                       |
| `/tokens`           | Manage MCP tokens                              |

### REST API

All API endpoints are prefixed with `/api` and return standardized `HttpResponse<T>` JSON envelopes. Bearer JWT authentication is required for all endpoints except public auth routes.

#### Authentication (`/api/auth`)

| Method | Endpoint                         | Description                                        |
| ------ | -------------------------------- | -------------------------------------------------- |
| POST   | `/api/auth/register`             | Register a new account                             |
| POST   | `/api/auth/login`                | Log in; returns JWT access token and refresh token |
| POST   | `/api/auth/refresh`              | Rotate refresh token and issue new access token    |
| POST   | `/api/auth/logout`               | Revoke current refresh token                       |
| POST   | `/api/auth/logout-all`           | Revoke all active refresh tokens for the user      |
| POST   | `/api/auth/verify-email`         | Verify email address using verification token      |
| POST   | `/api/auth/resend-verification`  | Resend email verification link                     |
| POST   | `/api/auth/forgot-password`      | Request password reset email                       |
| POST   | `/api/auth/reset-password`       | Reset account password using token                 |

#### Profile (`/api/profile`)

| Method | Endpoint                 | Description                                    |
| ------ | ------------------------ | ---------------------------------------------- |
| GET    | `/api/profile`           | Get current authenticated user profile         |
| PUT    | `/api/profile`           | Update user profile names                      |
| PATCH  | `/api/profile/email`     | Request email change (sends verification email)|
| PATCH  | `/api/profile/password`  | Change account password                        |
| DELETE | `/api/profile`           | Delete account and associated data             |

#### Collections (`/api/collections`)

| Method | Endpoint                  | Description                                            |
| ------ | ------------------------- | ------------------------------------------------------ |
| GET    | `/api/collections`        | List user collections (supports search and pagination) |
| POST   | `/api/collections`        | Create collection (supports client-generated UUID)     |
| PUT    | `/api/collections/{id}`   | Update collection name and color                       |
| DELETE | `/api/collections/{id}`   | Soft delete collection                                 |

#### Grammar Notes (`/api/collections/{collectionId}/grammars`)

| Method | Endpoint                                               | Description                                       |
| ------ | ------------------------------------------------------ | ------------------------------------------------- |
| GET    | `/api/collections/{collectionId}/grammars`             | List grammar notes in a collection                |
| POST   | `/api/collections/{collectionId}/grammars`             | Create grammar note (supports client UUID)        |
| GET    | `/api/collections/{collectionId}/grammars/{grammarId}` | Get single grammar note detail                    |
| PUT    | `/api/collections/{collectionId}/grammars/{grammarId}` | Update grammar note                               |
| PATCH  | `/api/collections/{collectionId}/grammars/{grammarId}` | Set or toggle starred status                      |
| DELETE | `/api/collections/{collectionId}/grammars/{grammarId}` | Soft delete grammar note                          |

#### Synchronization (`/api/sync`)

| Method | Endpoint    | Description                                                     |
| ------ | ----------- | --------------------------------------------------------------- |
| GET    | `/api/sync` | Delta sync changes feed (cursor-based pagination via timestamp) |

#### MCP Tokens (`/api/tokens`)

| Method | Endpoint            | Description              |
| ------ | ------------------- | ------------------------ |
| POST   | `/api/tokens`       | Generate a new MCP token |
| GET    | `/api/tokens`       | List user's MCP tokens   |
| PATCH  | `/api/tokens/{id}`  | Rename an MCP token      |
| DELETE | `/api/tokens/{id}`  | Revoke an MCP token      |

### MCP Server

Lobos embeds a remote MCP server (Streamable HTTP) at `/mcp`, letting AI clients (Claude Desktop, Claude Code, etc.) read and write a user's collections and grammars directly. It's authenticated with a dedicated MCP token — generated from the `/tokens` page — instead of the login JWT, so a leaked token can't be used to change account credentials.

```bash
claude mcp add --transport http lobos http://localhost:8080/mcp -H "Authorization: Bearer <mcp_token>"
```

| Tool                  | Description                               |
| --------------------- | ----------------------------------------- |
| `list_collections`    | List the authenticated user's collections |
| `create_collection`   | Create a new collection                   |
| `update_collection`   | Update a collection's name and color      |
| `delete_collection`   | Delete a collection                       |
| `list_grammars`       | List grammars within a collection         |
| `get_grammar`         | Get a single grammar's detail             |
| `create_grammar`      | Create a grammar note inside a collection |
| `update_grammar`      | Update a grammar note                     |
| `toggle_star_grammar` | Toggle star/favorite on a grammar note    |
| `delete_grammar`      | Delete a grammar note                     |

---

## Development

### Common Commands

```bash
# Compile
./mvnw compile

# Run tests
./mvnw test

# Check code formatting (Spotless)
./mvnw spotless:check

# Apply code formatting (Spotless)
./mvnw spotless:apply

# Package as JAR
./mvnw clean package

# Run with Spring Boot dev tools (live reload)
./mvnw spring-boot:run
```

---

## Security Notes

- REST API (`/api/**`) uses **stateless JWT** authentication via `JwtAuthFilter`.
- MCP server (`/mcp/**`) uses **stateless MCP tokens** (`McpAuthFilter`), separate from the login JWT and scoped to collections/grammars only.
- Web UI uses **session-based** Spring Security authentication, with an optional **remember-me** persistent login (JDBC-backed rotating token, 30-day validity) when the "Keep me signed in" checkbox is used at login. Tokens are keyed off the immutable `users.id` and are invalidated on password change or account deletion.
- All POST forms include a **CSRF token** for protection.
- Passwords are hashed with **jBCrypt**.
- Unauthenticated users are redirected to `/login`.

---

## License

This project is licensed under the [Apache License 2.0](LICENSE).
