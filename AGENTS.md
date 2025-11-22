# Repository Guidelines

sqlBuilder is a fluent DSL for composing SQL queries in Java 21. Follow these guidelines to keep contributions predictable, well-tested, and easy to review.

## Project Structure & Module Organization
- `src/main/java/org/in/media/res/sqlBuilder/api`: Public-facing contracts grouped by domain (`api/model`, `api/query`).
- `src/main/java/org/in/media/res/sqlBuilder/core/model`: Concrete column/table descriptors bound to descriptors in `api.model`.
- `src/main/java/org/in/media/res/sqlBuilder/core/query`: Clause implementations, builders, and validation; supporting factories reside in `core/query/factory`.
- `src/main/java/org/in/media/res/sqlBuilder/core/query/transpiler`: Dialect-specific transpilers (Oracle lives in `.../oracle`); mirror this layout for new dialects.
- `src/main/java/org/in/media/res/sqlBuilder/example`: Lightweight domain schema used in integration-style tests and docs.
- `src/test/java/org/in/media/res/sqlBuilder`: JUnit regression suite exercising behaviour and validation paths.
- `SchemaScanner` in `core/model` supports automatic table discovery; wire a schema with `new EmployeeSchema("com.foo.bar")` to target a custom package.

## Build, Test, and Development Commands
- `mvn clean compile` — Compile main sources with the Java 21 toolchain; run after signature or API changes.
- `mvn test` (or `mvn -o test` once dependencies are cached) — Execute the full JUnit Jupiter suite.
- `mvn package` — Produce `target/sqlBuilder-0.0.1-SNAPSHOT.jar` for local consumption or manual smoke tests.

## Coding Style & Naming Conventions
- Keep tab-based indentation and brace placement consistent with existing files; avoid mixing spaces and tabs.
- Interfaces expose descriptive names (`Query`, `Clause`, etc.), while concrete classes live in `core` with an `Impl` suffix (e.g., `QueryImpl`, `SelectImpl`, `OracleSelectTranspilerImpl`).
- Methods should read fluently (`select(columns)`, `where(column).eq(value)`) and return `this` when extending the builder chain.
- Define shared SQL symbols in the `constants` package and prefer uppercase enum members for readability.

## Testing Guidelines
- Author tests in `src/test/java/org/in/media/res/sqlBuilder` using JUnit Jupiter annotations (`@Test`, `@BeforeEach`).
- Extend `QueryBehaviourTest` for end-to-end clause combinations and `QueryValidationTest` for guard-rail scenarios.
- Name test methods with action-oriented camelCase (e.g., `fromVarargsIncludesAllTables`) and assert exact SQL strings via `transpile()` or `prettyPrint()`.
- Run `mvn test` before pushing; target coverage that exercises every new branch or clause registration path.

## Commit & Pull Request Guidelines
- Use concise, present-tense messages following `<type>: summary` where practical (e.g., `feat: add union support`); align with existing `feat`/`refactor`/`fix` prefixes.
- Reference related issues or tickets in the commit footer and capture any follow-up tasks in the PR description.
- PRs should call out behavioural changes, include sample SQL output or affected tests, and document new factory wiring or enums.
- Confirm the Maven test suite passes and mention the exact command executed; attach screenshots only when UI-affecting (rare).

## Extensibility & Transpiler Tips
- Clause factories in `core/query/factory` coordinate builders and transpilers—update factory registration when introducing new clauses or dialects.
- Mirror the Oracle transpiler package layout when creating alternatives; keep public interfaces dialect-agnostic to preserve fluent chaining.
- Prefer enriching `Operator`, `AggregateOperator`, or `SortDirection` enums over scattering literals, ensuring new cases integrate with validation utilities.
