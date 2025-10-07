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

-- GET /movies?pageNo=1&pageSize=10 - returns a paged list of movies (pageNo is 1-based)
- POST /movies - accepts JSON movie payload and publishes it to the `movies` Kafka topic (the consumer persists it and writes an audit record)

Example POST payload:

```json
{
  "name": "Inception",
  "genre": "Sci-Fi"
}
```
 
 For manual verification:
 
 Location: http://localhost:8080/movies/The%20Matrix
 JSON body with "check": "http://localhost:8080/movies/The%20Matrix"

 Example paged response (GET /movies?pageNo=1&pageSize=10):

 ```json
 {
   "content": [ { "id": 1, "name": "Inception", "genre": "Sci-Fi" } ],
   "currentPage": 1,
   "pageSize": 10,
   "totalElements": 42,
   "totalPages": 5,
   "showingFrom": 1,
   "showingTo": 1,
   "hasNext": true,
   "hasPrevious": false,
   "nextPageUrl": "/movies?pageNo=2&pageSize=10",
   "prevPageUrl": null
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

### Database migrations (Flyway)

This project includes a Flyway migration to add the `correlation_id` column to the `movie_audit` table so that correlation IDs are persisted for traceability.

- Migration file: `src/main/resources/db/migration/V1__add_correlation_id_to_movie_audit.sql`
- Flyway is included as a dependency so migrations run automatically on application startup when Flyway is enabled and a DataSource is available.

Local / development
 - If you want Flyway to run locally (recommended for testing), ensure the DB configured in `application.properties` is available and start the app normally. Flyway will run migrations automatically at startup.
 - If you prefer Hibernate-managed schema during development, you can keep `spring.jpa.hibernate.ddl-auto=update` for dev only and disable Flyway by setting `spring.flyway.enabled=false` in your local properties or profile.

Production / controlled upgrades
 - Use Flyway to manage schema changes in production. Typical steps:
   1. Add the migration SQL under `src/main/resources/db/migration` (already done).
   2. Deploy the new application artifact to your staging/production environment; Flyway will run migrations during application startup. We recommend setting `spring.jpa.hibernate.ddl-auto=none` or `validate` in production to avoid Hibernate altering the schema.
   3. Consider using `spring.flyway.baseline-on-migrate=true` if you are introducing Flyway into an existing database with pre-existing schema.

Running Flyway manually
 - If you prefer to run Flyway commands manually (for example, in CI), you can use the Flyway CLI or Maven plugin. Example using Maven plugin:

```powershell
Set-Location .\paging
.\mvnw.cmd -DskipTests flyway:migrate
```

 - You can also run the Flyway CLI in your environment against the same JDBC URL configured in `application.properties`.

## Production deployment

When deploying to production, run the application with the `prod` profile so Flyway migrations are applied and production-grade settings are used (security enabled and Hibernate auto-ddl disabled).

Key points:

- Start the app with the `prod` profile (either via JVM argument or environment variable). This enables the `application-prod.properties` settings where Flyway is enabled and `spring.jpa.hibernate.ddl-auto` is set to `none`.
- Flyway migrations will run automatically at startup when a DataSource is available. If you prefer to run migrations separately (for example in CI/CD), use the Flyway Maven plugin or the Flyway CLI.
- Security: the app enables HTTP Basic authentication when the `prod` profile is active (see `SecurityConfig`). Make sure to configure appropriate credentials or integrate with your identity provider before exposing the app.
- If the app runs behind a proxy/load-balancer, ensure the proxy sets standard forwarded headers (for example `X-Forwarded-Host`, `X-Forwarded-Proto`, `X-Forwarded-Port` or the `Forwarded` header) so absolute next/prev links are generated correctly.

Example PowerShell commands to build and run with the `prod` profile:

```powershell
Set-Location .\paging
.\mvnw.cmd -DskipTests -Dspring.profiles.active=prod clean package
& 'C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot\bin\java.exe' -Dspring.profiles.active=prod -jar target\paging-0.0.1-SNAPSHOT.jar
```

Run Flyway migrations explicitly via Maven (optional):

```powershell
Set-Location .\paging
.\mvnw.cmd -DskipTests -Dspring.profiles.active=prod flyway:migrate
```

Notes
- The included SQL uses `ALTER TABLE ... ADD COLUMN IF NOT EXISTS` which is compatible with MySQL 8+ and H2 used by tests. If your production DB does not support `IF NOT EXISTS`, replace the migration with a DB-specific safe script or run a controlled pre-check before applying.

## Notes

- `spring-boot-devtools` was removed from dependencies due to a RestartClassLoader issue that caused NoClassDefFoundError during startup in some environments. If you need devtools behavior, add it back and test carefully.
- If you want help adding Flyway migrations or Kafka consumer retry/DLQ handling, tell me which to do next and I'll add it.

### Generating a BCrypt hash and seeding an admin user (example)

This project includes two tiny helpers to help you create a BCrypt-hashed password and seed an example admin user for production or staging testing.

1) Generate a BCrypt hash locally using the bundled helper (Maven exec):

```powershell
Set-Location .\paging
.\mvnw.cmd -Dexec.mainClass=com.page.example.paging.SimpleBcrypt -Dexec.args="your-plaintext-password" exec:java
# If the Maven exec approach fails you can run the helper directly using the compiled classes and the jBCrypt JAR:

# Direct Java (Windows PowerShell):
# & 'C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot\bin\java.exe' -cp "target\classes;C:\Users\<your-user>\.m2\repository\org\mindrot\jbcrypt\0.4\jbcrypt-0.4.jar" com.page.example.paging.SimpleBcrypt your-plaintext-password
# Output will be the BCrypt hash you should paste into the next step
```

2) Seed the DB (safe options):

- Use the example SQL script (edit and replace <bcrypt-hash>):

  src/main/resources/examples/seed-admin-mysql.sql

  Then run it against your MySQL instance (for example using the mysql client):

```powershell
mysql -u root -p paging_db < src\main\resources\examples\seed-admin-mysql.sql
```

- Or add the hash into the Flyway seed migration `src/main/resources/db/migration/V3__seed_example_admin.sql` (it contains a placeholder) and run the migration with the `prod` profile or via the Flyway Maven plugin:

```powershell
.\mvnw.cmd -DskipTests -Dspring.profiles.active=prod flyway:migrate
```

Important: do NOT commit real plaintext passwords into your repository. If you store a seed migration with a hashed password, rotate it after the first successful deploy.

## Troubleshooting

- "NoClassDefFoundError: PagedResponse" during startup: this was caused by DevTools RestartClassLoader in some environments. We removed DevTools to resolve it.
- "No qualifying bean of type 'com.fasterxml.jackson.databind.ObjectMapper'": ensure `jackson-databind` is on the classpath and the application defines an ObjectMapper bean. The project includes an ObjectMapper bean in `PagingApplication`.