## Spring Boot integration app

- Prerequisites: start the Postgres container in `integration/docker` (`docker compose -f integration/docker/docker-compose.yml up -d`), which seeds the demo schema and data used by the DSL.
- Run the app: `mvn -pl integration spring-boot:run` (or via your IDE) and it will bind to `${PORT:-8080}`. JDBC settings reuse the existing env vars: `SQLBUILDER_JDBC_URL`, `SQLBUILDER_JDBC_USER`, `SQLBUILDER_JDBC_PASSWORD`.
- Endpoints:
  - `GET /queries` — list available demo queries (id, title, description).
  - `GET /queries/{id}` — execute the selected query, returning `sql`, `params`, and `rows` (JSON array of column/value maps).
- Catalog coverage: exposes the existing integration scenarios (projections, joins, aggregates, pagination, CTEs, grouped/optional filters, raw SQL fragments) plus new demos like department salary totals, top paid employees, orders with customers, and per-product revenue.
- Tests: `QueryControllerTest` exercises the REST surface with mocks. `QueryServiceIntegrationTest` is gated by `SQLBUILDER_IT=true` to allow opt-in database-backed verification once the Postgres container is running.
