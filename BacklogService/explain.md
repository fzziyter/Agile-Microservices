 # Backend Project Explanation

## 1. Project Overview

This is a **Spring Boot 3.5.13** backend application built with **Maven**. It provides a REST API for an Agile Project Management tool. The stack includes:

- **Spring Web**: REST controllers and HTTP handling.
- **Spring Data JPA**: Object-Relational Mapping (ORM) for MySQL.
- **Spring Security + JWT**: Stateless authentication and role-based authorization.
- **Lombok**: Reduces boilerplate code (getters, setters, etc.).
- **SpringDoc OpenAPI**: Auto-generates Swagger UI documentation.

---

## 2. Build & Configuration Files

| File | Purpose |
|------|---------|
| `pom.xml` | Maven Project Object Model. Declares dependencies (Spring Boot starters, JPA, Security, JWT, MySQL driver, Lombok, OpenAPI) and build plugins. |
| `mvnw` / `mvnw.cmd` | Maven Wrapper scripts (Unix & Windows). Allows building the project without having Maven installed globally. |
| `.mvn/wrapper/` | Contains the wrapper configuration (e.g., `maven-wrapper.properties`) telling the wrapper which Maven version to download. |
| `src/main/resources/application.properties` | Main configuration file. Sets up the MySQL database connection (`projetagile`), JPA/Hibernate auto-DDL, and JWT secret key + expiration. |
| `.gitignore` | Lists files/directories to ignore in Git (IDE files, `target/`, build artifacts). |
| `.gitattributes` | Enforces line endings for shell scripts (`LF`) and Windows batch files (`CRLF`). |
| `HELP.md` | Auto-generated Spring Boot reference links and guides. |

---

## 3. Application Entry Point

### `BackendApplication.java`
- The standard `main` method that bootstraps the entire Spring Boot application using `SpringApplication.run(...)`.
- The `@SpringBootApplication` annotation enables auto-configuration, component scanning, and Spring Boot magic.

---

## 4. Configuration Layer (`config/`)

### `SecurityConfig.java`
- Defines the security rules for the application.
- **Key behaviors:**
  - Disables CSRF (not needed for stateless JWT APIs).
  - Configures **CORS** to allow requests from frontend origins (`localhost:3000`, `localhost:5173`).
  - Sets session management to **STATELESS** (no server-side sessions).
  - Defines URL-based authorization:
    - `POST /api/auth/login` → Public.
    - Swagger/OpenAPI endpoints → Public.
    - `/api/admin/**` → `ADMIN` only.
    - `POST /api/projects` → `ADMIN` or `PRODUCT_OWNER`.
    - `/api/backlog/**` → `PRODUCT_OWNER`, `ADMIN`, or `DEVELOPER`.
    - Everything else → Authenticated.
  - Registers the custom `JwtAuthenticationFilter` **before** Spring's default username/password filter.
- Exposes beans for `PasswordEncoder` (BCrypt) and `AuthenticationManager`.

### `DataInitializer.java`
- Implements `CommandLineRunner`, so it runs automatically on startup.
- Checks if a user named `admin` exists in the database.
- If not, it creates a default admin user with username `admin` and password `admin123` (hashed via BCrypt) and role `ADMIN`.

---

## 5. Model / Entity Layer (`model/`)

These are JPA entities mapped to MySQL tables.

### `User.java`
- Represents an application user.
- Fields: `id`, `username` (unique), `password` (hashed), `role`.
- Mapped to table `users`.

### `Role.java`
- A simple Java `enum` defining possible roles: `ADMIN`, `PRODUCT_OWNER`, `SCRUM_MASTER`, `DEVELOPER`, `MANAGER`.

### `Project.java`
- Represents an Agile project.
- Fields: `id`, `name`, `methodology`, `startDate`, `endDate`, `theoreticalCapacity`.

### `BacklogItem.java`
- Represents an item in a project's backlog (User Story, Bug, or Technical Task).
- Fields: `id`, `title`, `description`, `type` (enum `ItemType`), `priority`.
- Has a `@ManyToOne` relationship linking it to a `Project`.

---

## 6. Repository Layer (`repository/`)

These are Spring Data JPA interfaces. They provide CRUD operations automatically.

### `UserRepository.java`
- Extends `JpaRepository<User, Long>`.
- Custom method: `Optional<User> findByUsername(String username)` — heavily used by authentication logic.

### `ProjectRepository.java`
- Extends `JpaRepository<Project, Long>`.
- Provides standard CRUD for projects.

### `BacklogItemRepository.java`
- Extends `JpaRepository<BacklogItem, Long>`.
- Custom method: `List<BacklogItem> findByProjectId(Long projectId)` — fetches backlog items for a specific project.

---

## 7. Security Layer (`security/`)

### `JwtUtil.java`
- Utility class for JSON Web Token operations.
- **Responsibilities:**
  - `generateToken(username, role)`: Creates a signed JWT with user info and expiration.
  - `parseToken(token)`: Validates signature and parses claims.
  - `extractUsername(token)` / `extractRole(token)`: Reads data from the token.
  - `isTokenValid(token)`: Returns true if the token is correctly signed and not expired.
- Reads configuration from `application.properties` (`jwt.secret`, `jwt.expiration-ms`).

### `JwtAuthenticationFilter.java`
- A custom Spring `OncePerRequestFilter`.
- Intercepts every incoming HTTP request.
- Looks for an `Authorization: Bearer <token>` header.
- If the token is valid, it extracts the username and role, creates a `UsernamePasswordAuthenticationToken`, and sets it in the `SecurityContextHolder`.
- This tells Spring Security: "This user is authenticated for this request."

---

## 8. Service Layer (`service/` & `service.impl/`)

Services contain the business logic and act as a bridge between Controllers and Repositories.

### `UserService.java`
- `createUser(user)`: Hashes the password and saves the user.
- `findAllUsers()`: Returns all users.
- `updateUser(id, details)`: Updates username, role, or password (re-hashing if password changes).
- `deleteUser(id)`: Deletes a user by ID.

### `ProjectService.java`
- `createProject(project)`: Saves a new project.
- `findAll()`: Lists all projects.
- `findById(id)`: Finds a project or throws an exception.
- `updateProject(id, details)`: Updates project fields selectively.
- `deleteProject(id)`: Deletes a project.

### `BacklogService.java`
- `addItem(projectId, item)`: Associates a new backlog item with an existing project and saves it.
- `getItemsByProject(projectId)`: Retrieves all backlog items for a given project.
- `updateItem(itemId, details)`: Updates fields of an existing backlog item.
- `deleteItem(itemId)`: Deletes a backlog item.

### `UserDetailsServiceImpl.java`
- Implements Spring Security's `UserDetailsService` interface.
- `loadUserByUsername(username)`: Fetches the user from the database and returns a Spring Security `UserDetails` object.
- Automatically prepends `ROLE_` to the user's role so that Spring Security annotations like `.hasRole("ADMIN")` work correctly.
- This is the class Spring Security uses internally to verify credentials during login.

---

## 9. Controller / REST API Layer (`controller/`)

Controllers handle HTTP requests and return JSON responses.

### `AuthController.java`
- Base path: `/api/auth`
- `POST /api/auth/login`: Accepts username/password, uses `AuthenticationManager` to verify credentials, generates a JWT via `JwtUtil`, and returns `{ token, username, role }`.
- `GET /api/auth/me`: Returns the currently authenticated user's info based on the provided Bearer token.

### `AdminController.java`
- Base path: `/api/admin`
- Protected by `SecurityConfig` (ADMIN only).
- `POST /api/admin/users`: Create a new user.
- `GET /api/admin/users`: List all users.
- `PUT /api/admin/users/{id}`: Update a user.
- `DELETE /api/admin/users/{id}`: Delete a user.

### `ProjectController.java`
- Base path: `/api/projects`
- `POST /api/projects`: Create a project (ADMIN or PRODUCT_OWNER).
- `GET /api/projects`: List all projects (Authenticated).
- `GET /api/projects/{id}`: Get a single project.
- `PUT /api/projects/{id}`: Update a project.
- `DELETE /api/projects/{id}`: Delete a project.

### `BacklogController.java`
- Base path: `/api/backlog`
- `POST /api/backlog/{projectId}`: Add a backlog item to a project.
- `GET /api/backlog/project/{projectId}`: Get all backlog items for a project.
- `PUT /api/backlog/{itemId}`: Update a backlog item.
- `DELETE /api/backlog/{itemId}`: Delete a backlog item.

---

## 10. Data Flow & Request Lifecycle

Here is how a typical request moves through the backend:

```
┌─────────────┐
│   Client    │
│  (Frontend) │
└──────┬──────┘
       │ HTTP Request (with Bearer Token)
       ▼
┌──────────────────────────────────────┐
│  CORS Check (SecurityConfig)         │
│  - Validates origin (localhost:5173) │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│  JwtAuthenticationFilter             │
│  - Reads Authorization header        │
│  - Validates JWT signature & expiry  │
│  - Sets Authentication in context    │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│  SecurityFilterChain                 │
│  - Checks roles for the URL          │
│  - Rejects if not authorized (403)   │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│  Controller (e.g., ProjectController)│
│  - Maps HTTP to Java method          │
│  - Extracts path variables / body    │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│  Service (e.g., ProjectService)      │
│  - Business logic / validation       │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│  Repository (e.g., ProjectRepository)│
│  - Spring Data JPA generates SQL     │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│  MySQL Database                      │
│  - Stores/Retrieves data             │
└──────────────────────────────────────┘
```

### Special Flow: Login

1. Client sends `POST /api/auth/login` with `{ username, password }`.
2. `AuthController` passes credentials to `AuthenticationManager`.
3. `AuthenticationManager` uses `UserDetailsServiceImpl` to load the user from the DB.
4. Password is verified using `BCryptPasswordEncoder`.
5. On success, `AuthController` calls `JwtUtil.generateToken(...)`.
6. The JWT token is returned to the client.
7. The client stores the token and sends it in the `Authorization: Bearer <token>` header for all subsequent requests.

---

## 11. Role-Based Access Summary

| Endpoint Pattern | Required Role(s) |
|------------------|------------------|
| `POST /api/auth/login` | Public |
| `/swagger-ui/**`, `/v3/api-docs/**` | Public |
| `/api/admin/**` | `ADMIN` |
| `POST /api/projects` | `ADMIN`, `PRODUCT_OWNER` |
| `/api/backlog/**` | `ADMIN`, `PRODUCT_OWNER`, `DEVELOPER` |
| All other endpoints | Any authenticated user |

---

## 12. Testing

### `BackendApplicationTests.java`
- A standard Spring Boot test class generated by Spring Initializr.
- Can be used to write integration tests that load the full application context.

---

## 13. Can This Project Be Divided into Microservices?

**Yes.** The current monolith is a good candidate for decomposition because the business domains are already well separated:

- **Authentication & Authorization** (`AuthController`, `JwtUtil`, security filters)
- **User Management** (`AdminController`, `UserService`, `User` entity)
- **Project Management** (`ProjectController`, `ProjectService`, `Project` entity)
- **Backlog Management** (`BacklogController`, `BacklogService`, `BacklogItem` entity)

### Proposed Microservices Architecture

| Microservice | Responsibility | Own Database |
|--------------|----------------|--------------|
| **Service Registry** (Eureka Server) | Tracks where all running services are located. | N/A |
| **API Gateway** | Single entry point for the frontend. Handles routing, CORS, and global JWT validation. | N/A |
| **Auth Service** | Login, token generation, token validation, `/api/auth/**`. | `auth_db` |
| **User Service** | CRUD for users, roles, password hashing, `/api/admin/**`. | `user_db` |
| **Project Service** | CRUD for projects, `/api/projects/**`. | `project_db` |
| **Backlog Service** | CRUD for backlog items, `/api/backlog/**`. | `backlog_db` |

### Why This Split Works

1. **Independent Scaling**: If the backlog is heavily used, you scale only the `Backlog Service`.
2. **Independent Deployment**: A bug fix in user management doesn't require redeploying the project service.
3. **Technology Diversity**: Each service could theoretically use a different database or language (though Spring Boot + JPA is consistent here).
4. **Fault Isolation**: If the `Project Service` goes down, users can still log in and view backlogs.

---

## 14. Role of Eureka Server

**Eureka** is a service registry from the Netflix OSS stack, integrated via **Spring Cloud Netflix Eureka**.

### What It Does

- **Service Registration**: When a microservice (e.g., `Project Service`) starts, it registers itself with Eureka under a logical name (e.g., `PROJECT-SERVICE`) along with its IP and port.
- **Service Discovery**: Other services (or the API Gateway) ask Eureka: "Where is `PROJECT-SERVICE`?" Eureka returns the current available instance(s).
- **Health Checks**: Eureka periodically pings registered services. If one fails, it is removed from the registry so no traffic is routed to a dead instance.

### How It Fits

```
┌─────────────┐         ┌─────────────────┐         ┌─────────────────┐
│  Frontend   │────────▶│  API Gateway    │────────▶│  Eureka Server  │
│             │         │  (Spring Cloud) │         │  (Service Reg.) │
└─────────────┘         └─────────────────┘         └─────────────────┘
                                │
            ┌───────────────────┼───────────────────┐
            ▼                   ▼                   ▼
    ┌──────────────┐   ┌──────────────┐   ┌──────────────┐
    │  Auth Svc    │   │  Project Svc │   │ Backlog Svc  │
    │  :8081       │   │  :8082       │   │  :8083       │
    └──────────────┘   └──────────────┘   └──────────────┘
```

- The **API Gateway** is the only service the frontend talks to.
- The Gateway uses Eureka to discover the actual IP/port of downstream services dynamically.

---

## 15. Impact on the Frontend

### 1. Single Endpoint (API Gateway) — Frontend Remains 100% Unchanged
**Before (Monolith):**
```javascript
// Frontend calls different paths on the same server
fetch('http://localhost:8080/api/auth/login')
fetch('http://localhost:8080/api/projects')
fetch('http://localhost:8080/api/backlog/1')
```

**After (Microservices + Gateway):**
```javascript
// Frontend calls the Gateway only. The Gateway routes to the correct service.
// The frontend code does NOT change at all.
fetch('http://localhost:8080/api/auth/login')   // → Auth Service
fetch('http://localhost:8080/api/projects')      // → Project Service
fetch('http://localhost:8080/api/backlog/1')     // → Backlog Service
```

The **frontend code remains completely unchanged**. The URL paths, headers, request bodies, and response handling are identical. The Gateway acts as a transparent proxy that handles routing internally.

### 2. CORS Moves to the Gateway
In the monolith, `SecurityConfig.java` handles CORS. In a microservices setup:
- CORS is configured **only on the API Gateway**.
- Individual backend services can disable CORS entirely or only allow traffic from the Gateway's IP range.

### 3. Authentication Handling — Option A (Selected Architecture)
In this architecture, the **API Gateway is the sole entry point** and performs all JWT validation:

1. The frontend sends `Authorization: Bearer <token>` on every request (unchanged behavior).
2. The Gateway intercepts the request and validates the JWT signature and expiry using `JwtUtil`.
3. If valid, the Gateway extracts the `username` and `role`, then **forwards them to the downstream microservice via HTTP headers** (e.g., `X-User-Username: admin`, `X-User-Role: ADMIN`).
4. The downstream services (Project Service, Backlog Service, etc.) **trust these headers implicitly** and do not re-validate the token. They only perform local role/permission checks based on the header values.
5. If the token is invalid or missing, the Gateway returns `401 Unauthorized` immediately, protecting the internal services from ever seeing the bad request.

**Why this is chosen:**
- **Zero frontend changes**: The frontend continues to send the same `Authorization` header to the same base URL.
- **Centralized security**: Token validation logic lives only in the Gateway. Internal services are simpler and do not need JWT libraries or filters.
- **Performance**: Avoids redundant token parsing in every single microservice.
- **Cleaner services**: Downstream controllers can focus purely on business logic.

### 4. Latency & Resilience
- **Latency**: Requests now hop through the Gateway + Network instead of internal method calls. This adds a few milliseconds per request.
- **Resilience**: You should add a **Circuit Breaker** (e.g., Resilience4j) in the Gateway. If `Project Service` is down, the Gateway can return a cached fallback or a friendly error instead of hanging the frontend.

### 5. Swagger / OpenAPI
- In the monolith, Swagger UI is at `http://localhost:8080/swagger-ui.html`.
- With microservices, each service can expose its own docs (e.g., `:8081/swagger-ui`, `:8082/swagger-ui`).
- The Gateway can aggregate these into a single Swagger UI using **Spring Cloud Gateway + OpenAPI aggregation** libraries, or you can maintain separate docs per service.

---

## 16. Trade-offs & Considerations

| Aspect | Monolith | Microservices |
|--------|----------|---------------|
| **Complexity** | Low — one codebase, one DB. | High — multiple repos, networking, distributed tracing. |
| **Deployment** | One JAR to run. | Requires Eureka + Gateway + N services running. |
| **Data Consistency** | Single DB = easy ACID transactions. | Distributed transactions require patterns like **Saga** or eventual consistency. |
| **Communication** | In-memory method calls. | HTTP / REST (or message brokers like RabbitMQ/Kafka). |
| **Debugging** | Simple stack traces. | Harder — need centralized logging (ELK, Grafana Loki). |

### Recommendation

For a **school/academic project (S4 level)**, the monolith is simpler and perfectly fine. If the goal is to **learn microservices architecture**, you can split it into **3 services** (`Auth+User`, `Project`, `Backlog`) + **1 Gateway** + **1 Eureka Server** as a proof of concept, but expect significantly more configuration and DevOps overhead.

---

*End of Explanation*

