# SpringPaging

Minimal Spring Boot demo that exposes a paged movies endpoint and demonstrates producing/consuming movie events via Kafka, with an audit table and transactional write behavior.

## Quick run (packaged JAR)

1. Build the project:

```powershell
Set-Location .\paging
.\mvnw.cmd -DskipTests clean package
```

2. Run the packaged JAR:

```powershell
Set-Location .\paging
& 'C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot\bin\java.exe' -jar target\paging-0.0.1-SNAPSHOT.jar
```

This starts the Spring Boot application on the port configured in `src/main/resources/application.properties` (default: 8080).

## Quick run (Maven plugin)

If you prefer to run from Maven (requires the wrapper file `mvnw.cmd` to be present and executable):

```powershell
Set-Location .\paging
.\mvnw.cmd -DskipTests spring-boot:run
```

## Endpoints

- GET /movies?pageNo=0&pageSize=10 - returns a paged list of movies
- POST /movies - accepts JSON movie payload and publishes it to the `movies` Kafka topic (the consumer persists it and writes an audit record)

Example POST payload:

```json
{
  "name": "Inception",
  "genre": "Sci-Fi"
}
```

## Kafka

- The app publishes and consumes messages on the topic configured in `application.properties` (default `movies`).
- A running Kafka broker is required at the address configured in `spring.kafka.bootstrap-servers` (default: `localhost:9092`).

If you run Kafka locally from a source distribution (e.g., in `C:\JavaRnD\kafka-3.9.0-src`), start the broker first. Example (bash):

```bash
# from Kafka distribution
bin/zookeeper-server-start.sh config/zookeeper.properties    # if using ZK-based Kafka
bin/kafka-server-start.sh config/server.properties

# create topic (example)
bin/kafka-topics.sh --create --topic movies --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```

If you see connection errors from the application on startup or when sending messages, verify the broker is reachable at the configured host/port.

## Database

- The app uses the JDBC URL configured in `application.properties`. By default the project is set to use a MySQL database at `jdbc:mysql://localhost:3306/paging_db`.
- Hibernate is configured with `spring.jpa.hibernate.ddl-auto=update`, so schema changes (movie and movie_audit tables) will be applied automatically in development.

For production use, replace `ddl-auto=update` with explicit migrations (Flyway or Liquibase).

## Notes

- `spring-boot-devtools` was removed from dependencies due to a RestartClassLoader issue that caused NoClassDefFoundError during startup in some environments. If you need devtools behavior, add it back and test carefully.
- If you want help adding Flyway migrations or Kafka consumer retry/DLQ handling, tell me which to do next and I'll add it.

## Troubleshooting

- "NoClassDefFoundError: PagedResponse" during startup: this was caused by DevTools RestartClassLoader in some environments. We removed DevTools to resolve it.
- "No qualifying bean of type 'com.fasterxml.jackson.databind.ObjectMapper'": ensure `jackson-databind` is on the classpath and the application defines an ObjectMapper bean. The project includes an ObjectMapper bean in `PagingApplication`.