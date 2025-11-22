## Spring Boot integration plan

- Add Spring Boot (web + JDBC) and PostgreSQL driver dependencies to `integration/pom.xml`, keeping the module self-contained and still runnable via the existing Dockerised Postgres.
- Introduce a Boot entry point under `org.in.media.res.sqlBuilder.integration.boot` with configuration that reads the existing `SQLBUILDER_*` env vars for JDBC details and exposes REST endpoints.
- Build a query registry that wraps the current integration scenarios and a handful of new demos (joins, ordering, pagination, aggregates) so each can return both the transpiled SQL and result rows.
- Implement a controller/service pair that lists available demos and executes a selected one against the Postgres container, returning `{sql, rows}`; log SQL at INFO.
- Add focused tests (controller slice + one container-backed integration) and a brief README note in the module describing how to start the Boot app against the container.
