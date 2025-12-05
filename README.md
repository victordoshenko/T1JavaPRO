# Java app

Spring Boot 3 (Java 17) service that exposes:
- **Limits**: reservation/confirmation/cancellation of user spending limits.
- **Payments**: simple payment execution with product ownership checks.
- **Products**: product lookup endpoints.

## Tech stack
- Java 17, Spring Boot 3.2
- Spring Web, Data JPA, Validation
- PostgreSQL (prod/dev), H2 (tests)
- Liquibase for migrations
- Maven Wrapper (`mvnw`/`mvnw.cmd`)

## Prerequisites
- JDK 17 (`JAVA_HOME` should point to it)
- Docker (for local DB)

## Running locally
1) Start PostgreSQL:
```bash
docker-compose up -d
```
DB credentials are in `docker-compose.yml` and `src/main/resources/application.yml`
(`postgres/postgres`, db `postgres`).

2) Run the app:
```bash
./mvnw spring-boot:run
```
or on Windows:
```powershell
.\mvnw.cmd spring-boot:run
```

## Tests
```bash
./mvnw test
```
On Windows:
```powershell
.\mvnw.cmd test
```
H2 is used automatically for tests.

## Notable endpoints (base `/api`)
- Limits: `/limits/{userId}`, `/limits/{userId}/reserve`, `/limits/confirm`, `/limits/cancel`, `/limits/{userId}/restore`, `/limits/{userId}/deduct`
- Payments: `/v1/payments/users/{userId}/products`, `/v1/payments/execute`
- Products: `/v1/users/{userId}/products`, `/v1/products/{productId}`

## Database migrations
- Liquibase changelog: `src/main/resources/db/changelog/db.changelog-master.xml`
- Seed data and schema SQL in `src/main/resources/db/migration/` and `init.sql`

## Profiles / configuration
Key settings in `src/main/resources/application.yml`. Override via env vars or `--spring.profiles.active` as needed.


