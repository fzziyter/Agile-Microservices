# Agile Microservices

A project management platform built with a microservices architecture using Spring Boot, deployed on Kubernetes (Minikube).

## Table of Contents

- [Architecture](#architecture)
- [Services](#services)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Kubernetes Deployment](#kubernetes-deployment)
- [Local Development](#local-development)
- [Configuration](#configuration)
- [API Reference](#api-reference)
- [Tech Stack](#tech-stack)

---

## Architecture

```
Browser (localhost:3000)
    │  Vite proxy /api/*
    ▼
kubectl port-forward ──── localhost:8080
    │
    ▼  (NodePort 30080)
┌─────────────────────────────────────────────┐
│              Minikube Cluster               │
│                                             │
│  ┌──────────────┐    ┌──────────────────┐   │
│  │  API Gateway │◄──►│  Eureka Server   │   │
│  │   :8080      │    │  :8761           │   │
│  └──────┬───────┘    └──────────────────┘   │
│         │ lb://MS-*                          │
│    ┌────┴──────────────────────────┐         │
│    ▼         ▼         ▼          ▼         │
│  ms-auth  ms-projects ms-backlog ms-sprints  │
│  :8081    :8082       :8083      :8084       │
│                 ▼                            │
│           ms-notifications                  │
│           :8085                             │
│                 │                           │
│         ┌───────▼───────┐                  │
│         │     MySQL     │                  │
│         │  StatefulSet  │                  │
│         │    :3306      │                  │
│         └───────────────┘                  │
└─────────────────────────────────────────────┘
```

**Request lifecycle:**
1. The React frontend (Vite, port 3000) sends a request to `/api/*`
2. Vite proxies the request to `localhost:8080` (Kubernetes port-forward)
3. The API Gateway receives the request and queries Eureka to resolve `lb://MS-AUTH-USER`
4. Eureka returns the direct IP of the target pod (`prefer-ip-address=true`)
5. The Gateway routes to the correct microservice
6. The microservice connects to MySQL via the internal DNS name `mysql:3306`

---

## Services

| Service | Port | Database | Role |
|---|---|---|---|
| `discovery-server` | 8761 | — | Eureka registry — all services register here |
| `api-gateway` | 8080 | — | Single entry point, routes requests to microservices |
| `ms-auth-user` | 8081 | `auth_db` | JWT authentication, user and role management |
| `ms-projects` | 8082 | `projet` | Project CRUD, members, settings |
| `ms-backlog` | 8083 | `backlog` | User stories, priorities, statuses |
| `ms-sprints` | 8084 | `sprints` | Sprint planning and tracking |
| `ms-notifications` | 8085 | `notifications` | Real-time notifications |
| `mysql` | 3306 | multiple | Shared database (StatefulSet with persistent storage) |

### Default Admin Account

An admin account is automatically created on the first startup of `ms-auth-user`:

| Field | Value |
|---|---|
| Username | `admin` |
| Password | `admin123` |
| Role | `ADMIN` |

---

## Prerequisites

| Tool | Minimum version | Purpose |
|---|---|---|
| Java JDK | 17 | Compile Spring Boot services |
| Maven | 3.8+ | Build projects |
| Docker | 20+ | Build images |
| Minikube | 1.38+ | Local Kubernetes cluster |
| kubectl | 1.28+ | Manage the cluster |
| Node.js | 18+ | React frontend |

---

## Project Structure

```
Agile-Microservices/
├── discovery-server/           # Eureka Server
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── api-gateway/                # Spring Cloud Gateway
│   ├── src/
│   │   └── main/resources/
│   │       └── application.yml   # Routes to each microservice
│   ├── Dockerfile
│   └── pom.xml
├── ms-auth-user/               # Authentication & users
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── ms-projects/                # Project management
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── ms-backlog/                 # Backlog & user stories
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── ms-sprints/                 # Sprint management
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── ms-notifications/           # Notifications
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                   # React application (Vite)
│   ├── src/
│   ├── vite.config.js
│   └── package.json
└── k8s/                        # Kubernetes manifests
    ├── deploy.sh               # Automated deployment script
    ├── start-portforwards.sh   # Local tunnels to the cluster
    ├── mysql/
    │   └── mysql.yaml          # StatefulSet + PVC + Service
    ├── discovery/
    │   └── discovery-server.yaml
    ├── gateway/
    │   └── api-gateway.yaml
    ├── ms-auth/
    │   └── ms-auth-user.yaml
    ├── ms-projects/
    │   └── ms-projects.yaml
    ├── ms-backlog/
    │   └── ms-backlog.yaml
    ├── ms-sprints/
    │   └── ms-sprints.yaml
    └── ms-notifications/
        └── ms-notifications.yaml
```

### Anatomy of a Kubernetes Manifest

Each `.yaml` file in `k8s/` contains three stacked resources:

```yaml
# 1. ConfigMap — environment variables for the service
kind: ConfigMap
data:
  SPRING_APPLICATION_NAME: "ms-auth-user"
  EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE: "http://discovery-server:8761/eureka/"
  EUREKA_INSTANCE_PREFER_IP_ADDRESS: "true"   # Register pod IP, not hostname
---
# 2. Deployment — the application container
kind: Deployment
spec:
  template:
    spec:
      initContainers:
        - wait-for-mysql    # Waits until MySQL is ready
        - wait-for-eureka   # Waits until Eureka is ready
      containers:
        - image: ms-auth-user:latest
          imagePullPolicy: Never    # Use local Minikube image
---
# 3. Service — stable network address inside the cluster
kind: Service
spec:
  type: ClusterIP    # Only reachable inside the cluster
  ports:
    - port: 8081
```

---

## Kubernetes Deployment

### Full Deployment (recommended)

```bash
# 1. Start Minikube
minikube start --driver=docker --memory=6144 --cpus=4

# 2. From the project root
chmod +x k8s/deploy.sh
./k8s/deploy.sh
```

The `deploy.sh` script automatically:
1. Checks that Minikube, kubectl and Maven are available
2. Configures Docker to use the Minikube daemon (`eval $(minikube docker-env)`)
3. Runs `mvn clean package -DskipTests` for each service found
4. Builds Docker images into the Minikube registry
5. Deploys MySQL and waits for it to be ready
6. Deploys Eureka and waits for it to be ready
7. Deploys the Gateway and all microservices
8. Restarts deployments to apply the latest images
9. Displays the final status of all pods and services

### Accessing the Application

After deployment, open two separate terminals:

```bash
# Terminal 1 — tunnels to the cluster (keep open)
chmod +x k8s/start-portforwards.sh
./k8s/start-portforwards.sh
# → API Gateway available at http://localhost:8080
# → Eureka Dashboard at http://localhost:8761

# Terminal 2 — frontend
cd frontend
npm install
npm run dev
# → Application available at http://localhost:3000
```

### Useful Commands

```bash
# Check the status of all pods
kubectl get pods

# Stream logs of a service in real time
kubectl logs -f deployment/ms-auth-user

# Check services registered in Eureka
kubectl port-forward service/discovery-server 8761:8761
# Then open http://localhost:8761

# Access MySQL directly
kubectl exec -it mysql-0 -- mysql -u root -pmysql

# Restart a service after a code change
kubectl rollout restart deployment/ms-auth-user

# Tear down the entire deployment
kubectl delete -f k8s/
```

### Updating a Microservice

```bash
# Point Docker to Minikube
eval $(minikube docker-env)

# Rebuild
cd ms-auth-user
mvn clean package -DskipTests
docker build -t ms-auth-user:latest .

# Redeploy
kubectl rollout restart deployment/ms-auth-user
```

---

## Local Development

To run services without Kubernetes (for development):

### 1. Start MySQL locally

```bash
docker run -d \
  --name mysql-local \
  -e MYSQL_ROOT_PASSWORD=mysql \
  -p 3306:3306 \
  mysql:8.0
```

### 2. Start services in order

```bash
# 1. Discovery Server
cd discovery-server && mvn spring-boot:run

# 2. API Gateway
cd api-gateway && mvn spring-boot:run

# 3. Microservices (any order)
cd ms-auth-user     && mvn spring-boot:run
cd ms-projects      && mvn spring-boot:run
cd ms-backlog       && mvn spring-boot:run
cd ms-sprints       && mvn spring-boot:run
cd ms-notifications && mvn spring-boot:run

# 4. Frontend
cd frontend && npm run dev
```

### 3. Verify Eureka registration

Open [http://localhost:8761](http://localhost:8761) — all services should appear with status `UP`.

---

## Configuration

### Key Environment Variables

Each microservice can be configured via environment variables (which take priority over `application.properties`):

| Variable | Default | Description |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/<db>` | MySQL connection URL |
| `SPRING_DATASOURCE_USERNAME` | `root` | MySQL user |
| `SPRING_DATASOURCE_PASSWORD` | `mysql` | MySQL password |
| `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE` | `http://localhost:8761/eureka/` | Eureka server URL |
| `EUREKA_INSTANCE_PREFER_IP_ADDRESS` | `true` | Register pod IP instead of hostname (required in Kubernetes) |
| `JWT_SECRET` | — | Secret key for signing JWT tokens |

### API Gateway Routes

Defined in `api-gateway/src/main/resources/application.yml`:

| Path | Target Service |
|---|---|
| `/api/auth/**` | `ms-auth-user` |
| `/api/admin/**` | `ms-auth-user` |
| `/api/projects/**` | `ms-projects` |
| `/api/backlog/**` | `ms-backlog` |
| `/api/sprints/**` | `ms-sprints` |
| `/api/notifications/**` | `ms-notifications` |

### CORS

The Gateway allows the following origins by default:
- `http://localhost:3000` (Vite dev server)
- `http://localhost:5173` (Vite alternative port)

---

## API Reference

### Authentication (`ms-auth-user`)

**Login**
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin",
  "role": "ADMIN"
}
```

All subsequent requests require the header:
```http
Authorization: Bearer <token>
```

### Projects (`ms-projects`)

```http
GET    /api/projects          # List all projects
POST   /api/projects          # Create a project
GET    /api/projects/{id}     # Get project details
PUT    /api/projects/{id}     # Update a project
DELETE /api/projects/{id}     # Delete a project
```

### Backlog (`ms-backlog`)

```http
GET    /api/backlog           # List user stories
POST   /api/backlog           # Create a user story
PUT    /api/backlog/{id}      # Update a user story
DELETE /api/backlog/{id}      # Delete a user story
```

### Sprints (`ms-sprints`)

```http
GET    /api/sprints               # List sprints
POST   /api/sprints               # Create a sprint
PUT    /api/sprints/{id}          # Update a sprint
POST   /api/sprints/{id}/start    # Start a sprint
POST   /api/sprints/{id}/complete # Complete a sprint
```

### Notifications (`ms-notifications`)

```http
GET    /api/notifications              # List notifications
PUT    /api/notifications/{id}/read    # Mark as read
```

---

## Tech Stack

**Backend**
- Java 17
- Spring Boot 3.x
- Spring Cloud Gateway
- Spring Cloud Netflix Eureka
- Spring Security + JWT
- Spring Data JPA / Hibernate
- MySQL 8.0

**Frontend**
- React 18
- Vite
- React Router v6
- Axios

**Infrastructure**
- Docker
- Kubernetes (Minikube v1.38)
- kubectl

---

## Key Architecture Decisions

**Service Discovery with Eureka** — each microservice registers itself on startup. The Gateway resolves `lb://MS-AUTH-USER` by querying Eureka, which returns the direct pod IP. The property `eureka.instance.prefer-ip-address=true` is critical here: without it, Eureka registers the pod hostname (e.g. `ms-auth-user-7679c7bff6-2n6w5`) which cannot be resolved via DNS inside the cluster, causing a `NXDOMAIN` error.

**Guaranteed startup order** — `initContainers` in each Deployment make the pod wait until both MySQL and Eureka are reachable before starting the Spring Boot application. This prevents crash loops caused by missing dependencies.

**Local Minikube images** — `imagePullPolicy: Never` tells Kubernetes never to pull the image from a remote registry. Images are built directly into the Minikube Docker daemon via `eval $(minikube docker-env)`, keeping the workflow entirely local.

**Data isolation** — each microservice owns its own MySQL database, created automatically on startup via `createDatabaseIfNotExist=true`. This follows the microservices principle of loose coupling: no service accesses another service's database directly.