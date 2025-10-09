## Multi-stage Dockerfile for the Paging application
## Builds the Maven artifact and produces a small runtime image

FROM maven:3.9.4-eclipse-temurin-17 as builder
WORKDIR /workspace

# Copy project and build
COPY . /workspace
RUN chmod +x mvnw || true
RUN mvn -B -DskipTests package -DskipITs

FROM eclipse-temurin:17-jre as runtime
WORKDIR /app

# Copy the built jar from the builder stage (adjust name if your artifactId/version changes)
COPY --from=builder /workspace/target/paging-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
