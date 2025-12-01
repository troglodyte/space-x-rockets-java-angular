## SpaceX Rockets information page  
#### Using Spring and Angular

### Backend (Spring Boot, Maven)

Prerequisites:
- Java JDK installed (ensure your JDK matches the version configured in pom.xml)
- Maven 3.9+

Common commands:
- Clean and build the project and run the backend in dev mode:
- `mvn clean package && mvn spring-boot:run``

Default server address:
- The backend starts on http://localhost:8080 by default.

Verify it’s running:
- Open in browser or curl: `http://localhost:8080/api/rockets/all`
  - Example: `curl http://localhost:8080/api/rockets/all`

### Run with Docker Compose

Prerequisites:
- Docker and Docker Compose installed

Build and start the backend:
- `docker compose up --build`

Then open:
- http://localhost:8080/api/rockets/all

Environment variables (optional):
- You can override JVM or Spring settings, e.g.:
  - `JAVA_OPTS="-Xms256m -Xmx512m" docker compose up --build`
  - `SPRING_PROFILES_ACTIVE=prod docker compose up --build`

Stop containers:
- `docker compose down`
