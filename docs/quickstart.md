# Quickstart

## Get started in 60 seconds

1. Build locally (installs the snapshot to `~/.m2`):
   ```bash
   mvn -q -DskipTests install
   ```
2. Add the dependency:
   - Maven
     ```xml
     <dependency>
       <groupId>org.in.media.res</groupId>
       <artifactId>sqlBuilder-core</artifactId>
       <version>0.0.1-SNAPSHOT</version>
     </dependency>
     ```
   - Gradle (Kotlin DSL)
     ```kotlin
     implementation("org.in.media.res:sqlBuilder-core:0.0.1-SNAPSHOT")
     ```
3. Write your first query:
   ```java
   var schema = new EmployeeSchema();
   var employee = schema.getTableBy(Employee.class);
   var job = schema.getTableBy(Job.class);

   SqlAndParams sap = SqlQuery.query()          // or SqlQuery.newQuery() for staged typing
       .select(Employee.C_FIRST_NAME, Employee.C_LAST_NAME)
       .innerJoin(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
       .where(Employee.C_FIRST_NAME).eq("Alice")
       .orderBy(Employee.C_LAST_NAME)
       .limitAndOffset(20, 0)
       .render();

   sap.sql();    // SELECT ... OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
   sap.params(); // ["Alice", 0, 20]
   ```

> API boundary: only `org.in.media.res.sqlBuilder.api.*` is stable. Everything in `*.core.*` or `*.processor.*` is internal—always use the `SqlQuery` facade instead of `*Impl` classes.

## Build & test

- Compile: `mvn clean compile`
- Full test suite: `mvn test` (or `mvn -o test` once dependencies are cached)
- Package only: `mvn -q -DskipTests package`
- Examples module only: `mvn -pl examples -q test`

## Integration quickstart (Postgres)

- Start the demo database (seeds schema + data):  
  `docker compose -f integration/docker/docker-compose.yml up -d`
- Export (or rely on defaults) for the integration modules:  
  `export SQLBUILDER_JDBC_URL=jdbc:postgresql://localhost:5432/sqlbuilder`  
  `export SQLBUILDER_JDBC_USER=sb_user`  
  `export SQLBUILDER_JDBC_PASSWORD=sb_pass`
- Run the console harness against Postgres:  
  `mvn -pl integration exec:java`
- Run the Spring Boot demo API:  
  `mvn -pl integration spring-boot:run` (binds to `${PORT:-8080}`)
- Optional integration tests (live DB required):  
  `SQLBUILDER_IT=true mvn -pl integration test`
- Stop the container when done:  
  `docker compose -f integration/docker/docker-compose.yml down`

### Oracle XE variant

- Start Oracle XE with seeded schema (service name `oracle-xe`):  
  `docker compose -f integration/docker/docker-compose.yml up -d oracle-xe`
- Use the Oracle defaults (set if different):  
  `export SQLBUILDER_DIALECT=oracle`  
  `export SQLBUILDER_JDBC_URL=jdbc:oracle:thin:@localhost:1521/XEPDB1`  
  `export SQLBUILDER_JDBC_USER=SB_USER`  
  `export SQLBUILDER_JDBC_PASSWORD=sb_pass`
- Run console harness against XE:  
  `mvn -pl integration exec:java`
- Run Spring Boot API with the Oracle profile:  
  `mvn -pl integration spring-boot:run -Dspring-boot.run.profiles=oracle`
- Optional XE-backed test (live DB required):  
  `SQLBUILDER_IT_ORACLE=true mvn -pl integration test`
- Oracle JDBC: the XE profile expects `ojdbc11` on your Maven cache; download/publish locally if your mirror blocks Oracle artifacts.

### Switching between Postgres and Oracle

- Only run one container at a time to avoid port conflicts. Stop the other service first:  
  `docker compose -f integration/docker/docker-compose.yml down oracle-xe` (when moving to Postgres)  
  `docker compose -f integration/docker/docker-compose.yml down sqlbuilder-db` (when moving to Oracle)
- Swap env vars and profile to match the active DB:
  - Postgres: (defaults) `SQLBUILDER_DIALECT=postgres`, URL `jdbc:postgresql://localhost:5432/sqlbuilder`, user `sb_user`, pass `sb_pass`; no Spring profile needed.
  - Oracle: `SQLBUILDER_DIALECT=oracle`, URL `jdbc:oracle:thin:@localhost:1521/XEPDB1`, user `SB_USER`, pass `sb_pass`; add `-Dspring-boot.run.profiles=oracle` when running the Spring Boot app.
- Re-run your console (`mvn -pl integration exec:java`) or Boot (`mvn -pl integration spring-boot:run ...`) command after adjusting.

### Spring Boot wiring (schema + query beans)

- The integration app publishes the DSL dialect and schema as beans (`IntegrationDslConfig`), so the same `ScannedSchema` is reused across controllers/services.
- Each demo query is a Spring bean (`DemoQueryRepository`), then collected by `QueryCatalog`—repository-like and easy to inject or mock per domain/query.

## Repository layout

- `core/` — distributable DSL, factories, validators, annotation processor (packaged; build runs with `-proc:none`).
- `examples/` — sample schema, `MainApp`, benchmarks, integration-style tests (runs the processor).
