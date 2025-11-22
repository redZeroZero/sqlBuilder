## Query Validation Utility – Scope & Plan

Goal: add an opt-in validator that inspects a `Query` (no execution by default) to catch structural and dialect-related issues early, without changing render/compile semantics.

### What to validate (static checks)
- **Projection vs grouping:** non-aggregated columns in SELECT must appear in GROUP BY (unless dialect explicitly allows otherwise).
- **Derived tables/CTEs:** alias count matches projection count; if raw projections are present and no aliases provided to `toTable`, surface a validation error instead of throwing at runtime.
- **Order by safety:** flag ORDER BY on non-grouped columns when GROUP BY is present; suggest ordering by aggregate/alias instead.
- **Parameters:** compiled queries have all named params bound (no duplicates for positional binding, no unknown/missing names), optional-condition params are typed.
- **Raw fragments:** best-effort checks for missing GROUP BY when raw projections include non-aggregated expressions; warn on obvious syntax gaps.
- **Dialect rules (pluggable):** e.g., Postgres disallows alias-qualified SET targets; add a hook for dialect-specific validations.
- **Structural:** SELECT has at least one projection; HAVING used after GROUP BY unless raw; CTE/derived table present when referenced.

### API shape
- Internal utility `QueryValidator` in `core.query.validation` with:
  - `ValidationReport validate(Query query, Dialect dialect)` – returns errors/warnings/info, does not throw for validation failures.
  - Optional runtime hook (not default): `ValidationReport validateSyntax(Query query, DataSource ds)` that tries `PREPARE`/`EXPLAIN` without executing.
- Public facade (opt-in): `SqlQuery.validate(Query)` delegating to the internal validator with the default dialect; returns `ValidationReport`.
- `ValidationReport` contains lists of messages (type, code, description).

### Implementation notes
- Walk the `QueryImpl` AST: projections (typed/agg/raw), groupBy, having, orderBy, from (including derived tables and their subqueries), set operations, CTEs.
- Use `SelectionAliasResolver` to derive aliases and check counts for `toTable`.
- Reuse placeholder collection to validate parameter bindings/duplicates.
- Provide a small dialect rule interface to register checks (initially implement Postgres SET clause rule).

### Tests
- Errors when wrapping raw projections with `toTable` without explicit aliases (validated, not thrown).
- GROUP BY + ORDER BY non-grouped column flagged.
- Parameters: duplicate positional bindings rejected; missing/unknown names reported.
- Postgres-specific: alias-qualified SET flagged.

### Docs
- README snippet showing `SqlQuery.validate(query)` usage and typical outputs; clarify it’s opt-in and non-mutating.
