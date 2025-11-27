# SpaceXRockets

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 21.0.0.

## Development server

To start a local front end development server, run:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Running unit tests

To execute unit tests with the [Karma](https://karma-runner.github.io) test runner, use the following command:

```bash
ng test
```

## Running end-to-end tests

For end-to-end (e2e) testing, run:

```bash
ng e2e
```

Angular CLI does not come with an end-to-end testing framework by default. You can choose one that suits your needs.

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.

## Backend Development

To build and run the backend server (Spring Boot with Maven) from the project root:

1. Ensure prerequisites are installed:
   - Java JDK (matching the version in the backend pom.xml)
   - Maven 3.9+

2. In a terminal at the repository root (`/Users/m/code/SpaceXRockets`), run the backend in dev mode:
   
   ```bash
   mvn spring-boot:run
   ```

   The server starts by default at:
   - http://localhost:8080

3. Verify the API is responding:
   
   ```bash
   curl http://localhost:8080/api/rockets/all
   ```

4. Optional: build a jar and run it directly:
   
   ```bash
   mvn clean package
   java -jar target/SpaceXRockets-0.0.1-SNAPSHOT.jar
   ```

5. Change port if needed (example: 8081):
   
   ```bash
   mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
   ```

Notes:
- During frontend development, the Angular dev server is configured to proxy `/api` to `http://localhost:8080`. Start it from `frontend/SpaceXRockets` with `npm start`.

