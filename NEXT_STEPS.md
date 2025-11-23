## Follow-up ideas

- Add an Oracle XE profile: provide a docker-compose service with seeded schema, a Spring `oracle` profile that swaps JDBC URL/driver, and wire the Oracle transpiler so catalog queries run against XE. Mirror the Postgres opt-in integration test to catch dialect gaps.
- Improve test wiring: fix Surefire to pick up JUnit 5 so controller/service tests run by default; keep the DB-backed suite opt-in (e.g., `SQLBUILDER_IT=true`). Add an integration test that iterates the full catalog to ensure every demo query executes.
- Expand window-function DSL: add `OVER (PARTITION BY ... ORDER BY ... [ROWS|RANGE ...])` support with helpers for `ROW_NUMBER`, `RANK`, moving averages, and running totals. Include validation coverage and rendered SQL assertions.
- Broaden demo coverage: add scenarios that stress optional filters, grouped conditions, nullable joins, HAVING with aggregates, vendor-specific raw fragments, and a richer product-revenue example that joins payments safely across dialects.
- Documentation updates: note dialect switching steps, sample Oracle XE run commands, and window-function usage examples in the README and Spring Boot app guide.
