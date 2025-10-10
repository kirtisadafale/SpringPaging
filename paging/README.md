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

## Redis-backed rate limiter (optional)

This project includes an optional Redis-backed token-bucket rate limiter implemented in `RedisRateLimiterService` and backed by a Lua script (`src/main/resources/redis/token_bucket.lua`). The Redis implementation is disabled by default. To enable it at runtime, set the following property:

```
app.rateLimiter.type=redis
```

When enabled, the app will use Redis for token-bucket operations. This is useful for horizontally-scaled deployments where a central rate-limiter state is required.

Running the Redis integration test (CI-friendly)

The project includes a Testcontainers-based integration test that starts a temporary Redis container and verifies the Redis-backed limiter end-to-end. The test uses the `test` profile and the test properties in `src/test/resources/application-test.properties`.

Run Redis integration test locally (PowerShell)

If you want to run only the Testcontainers-based Redis integration test on your machine (recommended for a quick, isolated check), run the following from PowerShell. This requires Docker Desktop (or another working Docker engine) so Testcontainers can start a temporary Redis container.

Prerequisites:
- Docker Desktop (running and accessible to Testcontainers)
- Java 17+ (JDK used by the Maven wrapper)

PowerShell example (from the project root):

```powershell
Set-Location .\paging
# Quote the -Dtest value in PowerShell so Maven parses the property correctly
.\mvnw.cmd -Dtest="com.page.example.paging.RedisRateLimiterIntegrationTest" test
```

Notes and troubleshooting:
- If Docker is not available, you can point the integration test at a running Redis instance instead of using Testcontainers by setting properties on the command line, for example:

```powershell
Set-Location .\paging
.\mvnw.cmd -Dtest="com.page.example.paging.RedisRateLimiterIntegrationTest" -Dapp.rateLimiter.type=redis -Dspring.redis.host=127.0.0.1 -Dspring.redis.port=6379 test
```

- If you previously saw a Maven error like "Unknown lifecycle phase \".page.example.paging.RedisRateLimiterIntegrationTest\"", make sure you quote the `-Dtest` value exactly as shown above (PowerShell treats arguments differently than bash).
- If Testcontainers fails to pull images, ensure Docker Desktop has enough resources and that your machine can reach Docker Hub (or configure a local registry mirror).
- The test uses Testcontainers which will automatically start and stop a temporary Redis container; no manual cleanup is needed.

If you run this in CI, ensure Docker is available on the runner so Testcontainers can start the Redis container. Alternatively, set `app.rateLimiter.type=redis` and point `spring.redis.host` and `spring.redis.port` to an existing Redis instance.

## Running Redis locally (start / stop)

If you need a local Redis instance for manual testing or to run the Redis-backed limiter without Testcontainers, here are a few reliable options. Docker is recommended because it's fast, reproducible, and matches CI.

1) Docker (recommended)

Start a temporary Redis container (binds host 6379 -> container 6379):

```bash
# bash / PowerShell (works in both terminals)
docker run --rm -d --name redis-test -p 6379:6379 redis:7.2.2
```

Verify it's running:

```bash
docker exec -it redis-test redis-cli ping    # should print PONG
# or if you have redis-cli installed locally:
redis-cli -h 127.0.0.1 -p 6379 ping
```

Stop and remove the container:

```bash
docker stop redis-test
# container was started with --rm so it will be removed automatically
```

Start Redis with no persistence (good for ephemeral test runs):

```bash
docker run --rm -d --name redis-test -p 6379:6379 redis:7.2.2 redis-server --save "" --appendonly no
```

2) Docker Compose (useful when running multiple services)

Create `docker-compose.yml`:

```yaml
version: '3.8'
services:
  redis:
    image: redis:7.2.2
    ports:
      - "6379:6379"
```

Start in background:

```bash
docker compose up -d
```

Stop and remove:

```bash
docker compose down
```

3) Testcontainers (in tests)

If you run the included Testcontainers-based integration test, you do not need to start Redis manually. Testcontainers manages the Redis lifecycle for each test. Ensure Docker is available on the machine or CI runner.

4) Linux (systemd)

Install and run via package manager (Ubuntu example):

```bash
sudo apt update
sudo apt install redis-server -y
sudo systemctl start redis-server
sudo systemctl stop redis-server
```

5) macOS (Homebrew)

```bash
brew install redis
brew services start redis    # run in background
brew services stop redis
# or run temporarily:
redis-server /usr/local/etc/redis.conf
# (stop with Ctrl-C)
```

6) Windows

- Recommended: run Redis in Docker or WSL2 (install Redis inside WSL and use the Linux commands above).
- If you have a native Windows Redis build, use its service manager or Task Scheduler to start/stop.

Notes & troubleshooting

- Port conflicts: if host port 6379 is in use, map to a different host port: `-p 6380:6379` and set `spring.redis.port=6380`.
- Firewalls and Docker Desktop: ensure Docker is running and the container ports are reachable from your local environment.
- Logs: `docker logs -f redis-test` to stream container logs.
- Removing leftover container: `docker rm -f redis-test`.

## Local Redis helper scripts

This repo includes a pair of helper scripts to make starting/stopping Redis easier on Windows and development machines. There are two flavors:

- Docker-backed scripts (clean, isolated):
  - `scripts/start-redis.ps1` / `scripts/stop-redis.ps1` — start/stop a Redis Docker container (recommended if you have Docker).
  - Usage (PowerShell):
    ```powershell
    # start with default name and port
    .\scripts\start-redis.ps1

    # stop
    .\scripts\stop-redis.ps1
    ```

- Local (non-Docker) scripts — useful when you prefer a native/WSL Redis:
  - `scripts/start-redis-local.ps1` / `scripts/stop-redis-local.ps1` — attempt to start/stop a locally-installed Redis without Docker.
  - Behavior:
    - If WSL is present, the scripts try to run `redis-server`/`redis-cli` inside WSL (recommended on Windows).
    - Otherwise they look for a Windows service named like `redis*` and attempt to start/stop it.
  - Usage (PowerShell):
    ```powershell
    # start Redis in WSL or Windows service
    .\scripts\start-redis-local.ps1

    # stop
    .\scripts\stop-redis-local.ps1
    ```

Recommended workflow

- For CI and isolated tests: use Testcontainers (no manual start needed) or the Docker-backed scripts above.
- For local development on Windows where you want low overhead and native tooling, install Redis inside WSL and use the local scripts. The local scripts call into WSL and will start Redis as a daemon there.

How to verify Redis is running

- From PowerShell (if using Docker container named `redis-local` or `redis-test`):
  ```powershell
  docker exec -it redis-local redis-cli ping
  # or
  redis-cli -h 127.0.0.1 -p 6379 ping
  ```
- From WSL:
  ```bash
  redis-cli -p 6379 ping
  # should reply: PONG
  ```

Notes

- The Docker scripts are idempotent and will remove stopped containers with the same name before starting a new one. The local scripts require a previously-installed Redis (WSL or Windows service) and will not install Redis for you.
- If you prefer commands, the repository now includes a `Makefile` with handy targets:
  - `make docker-up` — build images (if needed) and `docker compose up -d`
  - `make docker-down` — `docker compose down`

MySQL in Docker Compose

The compose stack now includes a `mysql` service for local development. When you run `docker compose up -d`, a MySQL 8.0 container will be started with the database `paging_db` and a user `paging_user` / `paging_pass` (these are demo credentials). The app's environment in compose points Spring Boot at the container host `mysql`.

Important notes:
- Credentials in `docker-compose.yml` are for local development only. Do not commit production secrets — use environment files or CI secrets.
- If you already run a MySQL instance on your machine (localhost:3306), either stop it or edit `docker-compose.yml` to map the container port to a different host port (for example `- "3307:3306"`) and update `SPRING_DATASOURCE_URL` accordingly.
- The app is configured to run Flyway migrations on startup (if Flyway is enabled). The MySQL container will be used by the app on startup when using compose.





## Troubleshooting

- "NoClassDefFoundError: PagedResponse" during startup: this was caused by DevTools RestartClassLoader in some environments. We removed DevTools to resolve it.
- "No qualifying bean of type 'com.fasterxml.jackson.databind.ObjectMapper'": ensure `jackson-databind` is on the classpath and the application defines an ObjectMapper bean. The project includes an ObjectMapper bean in `PagingApplication`.


Runnning Application with -Dspring-boot.run.profiles=prod

I'll show short, actionable ways to run your app with the prod profile set (command-line + VS Code + IntelliJ + environment-var options). Pick whichever matches how you run the app.

What I'll cover

Run the packaged jar
Run with the Maven wrapper (spring-boot:run) — quick and recommended during dev
Pass a JVM arg to a direct java launch
Use an environment variable (PowerShell)
Add to a VS Code Java launch configuration (launch.json)
Add to an IntelliJ Run/Debug configuration
Run the packaged jar (java -jar)
Good for production-like runs after mvn package
Command (PowerShell):
Run via Maven wrapper (recommended for quick local runs)
Option A — let the Spring Boot plugin set the profile:
Option B — pass JVM system property (if you need to set other JVM args):
Run tests or Maven goals with the profile
Passing -Dspring.profiles.active=prod to Maven will set the system property for the JVM(s) spawned by Maven/Surefire in most cases:
For the spring-boot:run plugin prefer the forms in (2) (spring-boot.run.profiles or spring-boot.run.jvmArguments).
Use an environment variable (PowerShell)
Spring Boot also reads SPRING_PROFILES_ACTIVE env var, which is handy in CI or PowerShell sessions:
Add to VS Code launch configuration
If you run/ debug from VS Code, add a Java launch entry with vmArgs. Create or edit launch.json:
Alternatively, set environment variable in the same config:
Add to IntelliJ Run/Debug configuration
Open Run → Edit Configurations → select your application configuration
Add to "VM options":
Or use the "Environment variables" field:
Quick tips / troubleshooting

For mvn spring-boot:run prefer -Dspring-boot.run.profiles=prod (that is the plugin’s documented property).
-D passed directly after mvn usually sets Maven properties and is visible to plugin configuration — but for JVM you can use spring-boot.run.jvmArguments.
If you use a launcher/IDE that reuses an existing process, ensure you restart the process after changing vmArgs or env vars.
When running in Windows PowerShell remember ; is not needed between commands; use separate lines or ; if you do chain commands.


Build a docker image 
Set-Location C:\JavaRnD\SpringPaging\paging
docker compose up -d --build
To check logs
docker compose logs -f --tail 200 app

start docker app service in interactive mode=> docker compose run --service-ports app

to  start  in dev/prod
docker compose run --service-ports --entrypoint "java -Dspring.profiles.active=prod -jar /app/app.jar" app

to check if the migration is successfully applied 

1 Tail the app logs to confirm Flyway activity:

Look for Flyway lines like "Successfully applied migration V2__create_users_and_authorities.sql" or any connection errors.
Check flyway_schema_history in MySQL:

