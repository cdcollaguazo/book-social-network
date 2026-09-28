# BSN

Book Social Network is an application for publishing, discovering, and borrowing books within the `cdcollaguazo` platform.

The project is built with **Angular** and **Spring Boot**. Authentication is delegated to the platform IAM service, while shared AWS resources are provided by the common infrastructure project.

---

## 1. Responsibilities

- Provide the user interface for the Book Social Network.
- Expose REST APIs for books, borrowing, users, and feedback.
- Manage the application domain and persistence.
- Integrate with the platform IAM service for authentication and authorization.
- Keep application-specific deployment resources independent from the shared infrastructure.

---

## 2. Architecture

```text
Browser
   |
CloudFront
   |
   |-- /bsn/* --------------------> S3
   |                                 Angular UI
   |
   |-- /bsn/api/v1/* -------------> Shared ALB
   |                                    |
   |                                    v
   |                              BSN Target Group
   |                                    |
   |                                    v
   |                              ECS Spring Boot API
   |                                    |
   |                                    v
   |                               PostgreSQL
   |
   |-- /auth/* --------------------> Keycloak
```

The frontend is deployed as static content under `/bsn/`.

The backend owns its ECS service, Target Group, ALB Listener Rule, and application-specific configuration. Shared infrastructure references are retrieved from SSM Parameter Store.

---

## 3. Project Structure

```text
bsn/
├── .github/                # GitHub workflows and CI/CD configuration
├── apps/
│   ├── api/                # Spring Boot REST API
│   └── web/                # Angular frontend
├── database/               # Database migrations
├── infra/                  # Infrastructure as Code (IaC)
└── docker-compose.yml      # Local development services
```

---

## 4. Local Development

### Requirements

- Java 21
- Node.js
- Docker

Start the local dependencies:

```
docker compose up -d
```

Run the backend:

```
cd apps/api
./mvnw spring-boot:run
```

Run the frontend:

```
cd apps/web
npm ci
npm run start
```

---

## 5. Database Migrations

Database changes are managed as versioned migrations and applied before the application service is released.

Migrations are kept separate from the application code so schema changes can be tracked and deployed consistently across environments.
