# EV Charging Reservation and Management Application

EV Charging Reservation and Management Application is a Spring Boot web application for managing electric vehicle charging operations. It supports user registration and login, vehicle management, station and charger discovery, reservation creation, charging session tracking, and administrative charger/station operations.

## Project Links

| Resource | Link |
|---|---|
| Live Demo | `https://charge-go.duckdns.org/` |
| GitHub Repository | `https://github.com/alperenmursalli/ev-charging-system` |
| API Documentation | `https://charge-go.duckdns.org/swagger-ui.html` |
| Health Check | `https://charge-go.duckdns.org/actuator/health` |

## Main Features

- User registration and authentication
- Role-based access control with user and admin roles
- Vehicle creation, listing, updating, and deletion
- Charging station and charger management
- Station discovery with filtering support
- Reservation creation with connector compatibility and time conflict checks
- Charging session start/end flow
- Energy consumption and total cost calculation
- Thymeleaf-based web UI
- REST API with Swagger/OpenAPI documentation
- PostgreSQL database support
- Docker deployment support

## Technology Stack

- Java 17
- Spring Boot 4
- Spring MVC
- Spring Security
- Spring Data JPA
- Hibernate
- Thymeleaf
- PostgreSQL
- H2 for tests
- Maven
- Docker and Docker Compose
- JUnit 5 and Mockito

## Architecture

The project follows a layered MVC architecture:

```text
Browser / User
    |
    v
Thymeleaf UI / REST API
    |
    v
Spring MVC Controllers
    |
    v
Service Layer
    |
    v
Spring Data JPA Repositories
    |
    v
PostgreSQL Database
```

### Main Layers

- `controller`: Handles HTTP requests and API endpoints.
- `service`: Contains business rules and use case logic.
- `repository`: Provides database access through Spring Data JPA.
- `entity`: Defines domain models such as `Vehicle`, `Station`, `Charger`, `Reservation`, `ChargingSession`, and `AppUser`.
- `dto`: Defines request and response objects used by the API.
- `templates`: Contains Thymeleaf UI pages.
- `config`: Contains security, locale, OpenAPI, and startup configuration.

## Subsystems

- Authentication and user management
- Vehicle management
- Station management
- Charger management
- Reservation management
- Charging session management
- Map and station discovery UI
- Admin operations
- Scheduled reservation/session cleanup

## Running Locally

Create a `.env` file based on `.env.example`, then run:

```bash
scripts/run-local.sh
```

The application will be available at:

```text
http://localhost:18083
```

To use a different local port:

```bash
APP_LOCAL_PORT=8085 scripts/run-local.sh
```

## Running with an Empty Local Database

For development without the remote PostgreSQL database:

```bash
scripts/run-local-empty.sh
```

This uses an in-memory H2 database. Data will not persist after the application stops.

## Running with Docker

Build and start the container:

```bash
docker compose up --build
```

By default, Docker exposes the application at:

```text
http://localhost:8081
```

The exposed port can be changed with `APP_PORT` in `.env`.

## API Documentation

Swagger UI is available for admin users at:

```text
http://localhost:18083/swagger-ui.html
```

## Admin User List Endpoint

The system provides an admin-only user list endpoint:

```text
GET /users
```

Example:

```bash
curl -u <admin-username>:<admin-password> http://localhost:18083/users
```

The endpoint returns user metadata only. Password hashes are not exposed.

Example response:

```json
[
  {
    "id": 1,
    "username": "admin",
    "role": "ROLE_ADMIN",
    "enabled": true
  }
]
```

## Security

Passwords are not stored in plain text. The application uses Spring Security's `BCryptPasswordEncoder` to hash passwords before saving them to the database.

Role-based access control is used:

- `ROLE_USER`: Standard user operations such as vehicle management, reservation creation, and charging sessions.
- `ROLE_ADMIN`: Administrative operations such as station/charger management, Swagger access, and user listing.

## Testing

Run all tests:

```bash
./mvnw test
```

The project includes:

- Controller happy-path tests
- Service-layer business rule tests
- DTO validation tests
- Entity validation tests
- Exception handler tests
- Spring application context test

Example tested use cases:

- Creating a vehicle
- Creating a reservation
- Rejecting overlapping reservations
- Rejecting incompatible connector types
- Ending a charging session
- Calculating consumed kWh
- Calculating total charging cost
- Validating invalid request inputs

## Environment Variables

The application expects the following variables for PostgreSQL-backed execution:

```text
SPRING_DATASOURCE_URL=
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=
GOOGLE_MAPS_API_KEY=
APP_SECURITY_SEED_ADMIN_USERNAME=
APP_SECURITY_SEED_ADMIN_PASSWORD=
```

Do not commit real `.env` files or secrets to Git.

## Project Structure

```text
src/main/java/com/example/evsystem
├── config
├── controller
├── dto
├── entity
├── enums
├── exception
├── repository
├── service
└── validation

src/main/resources
├── static
│   ├── css
│   └── js
└── templates
```

## Notes

- `.env` is ignored by Git and should remain local.
- `src/main/resources/application-local.properties` is also ignored and excluded from Docker builds.
- Docker deployment uses container port `8080` and host port `${APP_PORT:-8081}`.
- Local script execution uses port `18083` by default.

