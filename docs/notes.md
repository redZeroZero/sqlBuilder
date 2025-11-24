# Notes & Tips

- The builder creates SQL strings; execution is left to your JDBC/ORM. Use `Query.prettyPrint()` for clause-per-line debugging.
- Identifiers are dialect-quoted (Oracle uses double quotes). Override the dialect or aliases to change quoting style.
- Transpilers are pluggable; defaults target Oracle syntax (OFFSET/FETCH). Extend factories for other dialects.
- WHERE / HAVING support the full comparator set: `<>`, `LIKE`, `NOT LIKE`, `BETWEEN`, `IN` / `NOT IN`, `IS (NOT) NULL`, plus scalar and set subqueries.
- Derived tables: wrap subqueries with `Query.as(alias, columns...)`.
- Prefer typed column descriptors for compile-time guards (`ColumnRef<BigDecimal> SALARY = ColumnRef.of("SALARY", BigDecimal.class);`).
- Stage interfaces expose typed overloads (e.g., `select(customerColumns.ID())`).
- Annotation processor emits `<Table>Columns` interfaces and implementations; enable processing in your modules to generate them automatically.
- `LIKE` patterns are escaped automatically; transpiler appends `ESCAPE '\'`.
- `EmployeeSchema` auto-discovers tables in `org.in.media.res.sqlBuilder.example`; pass a different base package to scan your own schema.

## Configuration & Integration Tips

- **Dialect selection:** defaults target Oracle. Supply custom transpilers/dialect factories for other databases.
- **Schema wiring:** reuse `EmployeeSchema` as a template—create a `ScannedSchema` subclass pointing to your package.
- **Performance baseline:** run `org.in.media.res.sqlBuilder.tools.QueryBenchmark` to gauge transpilation throughput.
