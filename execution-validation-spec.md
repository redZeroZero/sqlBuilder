## Execution & Validation Improvements

This document outlines proposed enhancements to harden the DSL beyond string rendering by executing queries, expanding coverage, and adding validation utilities.

### 1) H2-backed execution tests in `core`
- **Goal:** Add fast, repeatable tests that execute representative DSL queries against an in-memory database to catch issues that pure string comparison misses (qualification rules, NULL handling, grouping, optional predicates).
- **Scope:**
  - Introduce a JUnit 5 test harness that spins up H2 in PostgreSQL compatibility mode (`jdbc:h2:mem:sb_core;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH`) for portability.
  - Seed a minimal schema mirroring core test fixtures (employees, departments, jobs) with a handful of rows.
  - Execute a curated set of DSL queries end-to-end: projections, joins, aggregates + HAVING, optional filters (IS NULL guards), derived tables/CTEs, raw fragments with GROUP BY/HAVING, DML (UPDATE/INSERT/DELETE) to ensure SET clause behaviour.
  - Assert both SQL text and result shapes/row counts; validate optional filters can be toggled via `null` parameters without type errors.
  - Keep this harness isolated under `src/test/java` with no impact on public API.
- **Rationale:** Catch runtime incompatibilities early without external dependencies; faster feedback than containerised Postgres.

### 2) Expand integration scenarios (PostgreSQL)
- **Goal:** Double the integration coverage with more complex and realistic queries executed against the containerised Postgres schema.
- **Scope:**
  - Add ~15 new scenarios covering: multi-join chains, nested subqueries, window-like patterns via raw fragments, advanced aggregations (rollups where supported via raw), pagination with ties, complex boolean logic, mixed raw/typed clauses, more DML (multi-column updates with guards), and DELETE with subqueries.
  - Ensure each scenario logs/transpiles SQL and asserts deterministic ordering/limits to keep outputs stable.
  - Mirror these in the Spring Boot demo catalog so REST users can exercise the same coverage.
  - Keep existing scenarios; extend the set rather than replacing.
- **Rationale:** Broader behavioural surface reduces regressions and demonstrates more DSL capabilities in realistic contexts.

### 3) Validator utility in `core`
- **Goal:** Provide an optional validator that inspects a `Query` to catch common issues before execution.
- **Scope:**
  - API sketch: `QueryValidator.validate(Query, DataSource | SchemaMetadata) -> ValidationReport`.
  - Checks:
    - Structural: non-empty projection for SELECT; grouping consistency (non-aggregated columns must be grouped unless dialect allows otherwise); derived table alias alignment; presence of WHERE/HAVING when required by caller.
    - Parameter checks: duplicate/missing named parameters in `CompiledQuery`; optional conditions properly typed/cast.
    - Raw fragment integration: ensure GROUP BY/HAVING are present when raw projections include expressions (best-effort heuristic).
    - Dialect hooks: allow dialect-specific rules (e.g., Postgres disallowing qualified SET columns).
  - Optional runtime check: with a `DataSource`, attempt `EXPLAIN` or `PREPARE` to validate syntax without executing (opt-in, not default).
  - Deliver as an internal utility (package-private or internal package) to avoid expanding the public API prematurely; can be promoted later if stable.
- **Rationale:** Early detection of malformed queries and parameter issues; complements execution tests by offering lightweight validation.

### 4) CI/profile strategy
- **Goal:** Run execution-backed tests opportunistically in CI without slowing the default pipeline.
- **Scope:**
  - Default CI: run H2-backed core execution tests always.
  - Optional profile (e.g., `-Ppostgres-integration`): spin up containerised Postgres and run the integration module end-to-end.
  - Optional Oracle check: lightweight H2 `MODE=Oracle` execution tests as a fast gate for basic syntax/identifiers, plus an opt-in Oracle XE/Testcontainers profile for true compatibility when available.
  - Document profiles and how to opt in locally.
- **Rationale:** Balance coverage and speed; allow deeper checks when the environment permits.
