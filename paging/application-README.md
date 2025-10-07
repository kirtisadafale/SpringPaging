# Application profile & Flyway quick guide

This short note shows how to run the application with the `prod` profile (Flyway migrations enabled) and a few common Flyway troubleshooting tips.

## Run with the `prod` profile

Run from Maven (uses `application-prod.properties` overrides):

```powershell
Set-Location .\paging
.\mvnw.cmd -Dspring-boot.run.profiles=prod -DskipTests spring-boot:run
```

Or run the packaged JAR with the `prod` profile:

```powershell
Set-Location .\paging
& 'C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot\bin\java.exe' -jar -Dspring.profiles.active=prod target\paging-0.0.1-SNAPSHOT.jar
```

When using the `prod` profile the project is configured to:
- enable Flyway migrations on startup (`spring.flyway.enabled=true`)
- allow baseline on migrate (`spring.flyway.baseline-on-migrate=true`)
- disable Hibernate schema auto-update (`spring.jpa.hibernate.ddl-auto=none`)

## Common Flyway troubleshooting

- Migration fails with "column already exists":
  - If you are introducing Flyway to an existing DB, set `spring.flyway.baseline-on-migrate=true` and create a baseline (or run Flyway's `baseline` command) so Flyway doesn't try to reapply older changes.
  - Check the Flyway history table (`flyway_schema_history`) to see what migrations were applied.

- Flyway can't connect to the DB / authentication errors:
  - Verify JDBC URL, username, and password in the active application properties or env variables.
  - Ensure the DB is reachable from the running host and the user has privileges to run DDL.

- SQL uses `IF NOT EXISTS` and migration crashes on older DB versions:
  - Some RDBMS or older versions do not support `IF NOT EXISTS` on `ALTER TABLE`; replace the migration with a DB-specific conditional script or perform the schema change manually before running the migration.

- Migration partially applied / checksum mismatch:
  - Use `flyway:repair` cautiously to fix checksums if you intentionally modified a migration after it was applied (prefer: create a new migration instead).

## Manual Flyway commands (Maven)

Run migrations manually using Maven (reads your active properties/profile):

```powershell
Set-Location .\paging
.\mvnw.cmd -DskipTests flyway:info    # show migrations
.\mvnw.cmd -DskipTests flyway:migrate # apply migrations
.\mvnw.cmd -DskipTests flyway:repair  # repair metadata (use with caution)
```

If you'd like, I can also add a CI job snippet that runs `flyway:validate` before deployment.