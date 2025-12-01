## SpaceX Rockets information page  
#### Using Spring and Angular

### Backend (Spring, Maven)

Prerequisites:
- Java JDK installed (ensure your JDK matches the version configured in pom.xml)
- Maven 3.9+

Common commands:
- Clean and build the project and run the backend in dev mode:
- `  mvn clean package && mvn spring-boot:run`

Default server address:
- The backend starts on http://localhost:8080 by default.

Verify it’s running:
- Open in browser
  - Example: `curl http://localhost:8080/api/rockets/all`
  - Or if using Jetbrains, use the docs/*http file

### Run with Docker Compose

Prerequisites:
- Docker and Docker Compose installed

Build and start the backend:
- `docker compose up --build`

Then open:
- http://localhost:8080/api/rockets/all

Stop containers:
- `docker compose down`
