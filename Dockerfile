## Multi-stage Dockerfile for Spring Boot (Java 25)

# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace

# Pre-copy pom to leverage Docker layer caching
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -q -e -DskipTests dependency:go-offline

# Copy source and build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -e -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:25-jre
WORKDIR /app

# Expose the default Spring Boot port
EXPOSE 8080

# Copy the built jar
COPY --from=build /workspace/target/*-SNAPSHOT.jar /app/app.jar

# Use JAVA_OPTS for optional JVM flags
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
