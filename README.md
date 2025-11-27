## SpaceX Rockets information page  
#### Using Spring and Angular

### Backend (Spring Boot, Maven)

Prerequisites:
- Java JDK installed (ensure your JDK matches the version configured in pom.xml)
- Maven 3.9+

Common commands:
- Clean and build the project:
  - `mvn clean package`
- Run the backend in dev mode:
  - `mvn spring-boot:run`
- Run without tests (faster build):
  - `mvn clean package -DskipTests`

Default server address:
- The backend starts on http://localhost:8080 by default.

Verify it’s running:
- Open in browser or curl: `http://localhost:8080/api/rockets/all`
  - Example: `curl http://localhost:8080/api/rockets/all`

Run the packaged jar (alternative):
1) Build it: `mvn clean package`
2) Run: `java -jar target/SpaceXRockets-0.0.1-SNAPSHOT.jar`

Environment configuration:
- If you use a .env file, it will be loaded via spring-dotenv.
- To change the server port, set `SERVER_PORT` in your environment or add `--server.port=XXXX` to the run command, e.g.:
  - `mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081`

Endpoints (examples):
- Rockets: `GET /api/rockets/all`
- Launches: `GET /api/launches/all`
