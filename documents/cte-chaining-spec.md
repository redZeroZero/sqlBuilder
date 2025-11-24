# sqlBuilder Specification — Chained CTE Builder

## Purpose
- Provide an optional, SQL-like fluent style for declaring multiple CTEs in sequence while retaining `CteRef` handles and existing validation/ordering semantics.
- Preserve backward compatibility with current `WithBuilder.cte(...)` and `WithBuilder.main(...)` APIs.

## Goals
- Allow `WITH` declarations to be chained fluently: `SqlQuery.with().with(name, body, aliases...).and().with(...).main(mainQuery)`.
- Ensure each chained CTE returns or exposes a `CteRef` for downstream joins/filters.
- Keep validation behaviour unchanged: unique/non-blank names, at least one projected column, unique column aliases, fast-fail on unsupported dialects when attaching the main query.
- Maintain parameter binding order (CTE params before main-query params) and CTE declaration order in rendering.

## Non-goals
- No changes to SQL output format.
- No new dialect behaviours beyond existing `Dialect.supportsCte()` gating.
- No breaking changes to existing public methods.

## Public API Additions (contracts)
- In `api/query/WithBuilder` add a chaining-friendly entry point:
	- `CteStep with(String name, Query cteBody, String... columnAliases);`
- Introduce a nested step interface to expose both the registered CTE and the ability to continue chaining:
	- `interface CteStep {`
		- `CteRef ref();` // access the registered CTE columns
		- `WithBuilder and();` // continue declaring CTEs
	- `}`
- `main(Query)` remains the terminal method that returns the final `Query` with attached CTEs. `cte(...)` overloads remain unchanged for compatibility.

## Usage Examples
```java
var with = SqlQuery.with();

var salaryAvg = with.with("salary_avg", avgSalary, "EMPLOYEE_ID", "AVG_SALARY").ref();
var deptTotals = with.and().with("dept_totals", deptTotalsQuery, "DEPT_ID", "TOTAL_SALARY").ref();

var main = SqlQuery.newQuery()
		.select(EmployeesTable.C_FIRST_NAME)
		.select(EmployeesTable.C_LAST_NAME)
		.from(IntegrationSchema.employees())
		.join(salaryAvg).on(EmployeesTable.C_ID, salaryAvg.column("EMPLOYEE_ID"))
		.join(deptTotals).on(EmployeesTable.C_DEPT_ID, deptTotals.column("DEPT_ID"))
		.where(salaryAvg.column("AVG_SALARY")).supOrEqTo(85_000)
		.asQuery();

return with.main(main).render();
```
- The chain can start directly from `SqlQuery.with()`, with each `with(...)` returning a `CteStep`. `and()` resumes the builder to allow further `with(...)` calls. Access to `CteRef` remains explicit.

## Validation & Behaviour (unchanged from current)
- Registering a CTE:
	- Name must be non-blank and unique within the builder; duplicates throw `IllegalArgumentException`.
	- CTE body must project at least one column; empty projection throws `IllegalArgumentException`.
	- Column aliases (if provided) must be non-blank and unique; duplicates throw `IllegalArgumentException`.
- Rendering/attach:
	- `main(query)` attaches all declared CTEs in declaration order and throws `UnsupportedOperationException` when `Dialect.supportsCte()` is false.
	- Parameter ordering remains: all CTE parameters in declaration order, then main-query parameters.

## Implementation Plan (core)
- Update `api/query/WithBuilder` to add `with(...)` returning `CteStep`, and define `CteStep`.
- Implement `CteStep` in `core/query/cte/WithBuilderImpl`:
	- Reuse existing CTE registration logic (`registerCte` equivalent) to enforce validation.
	- `with(...)` registers the CTE and returns a lightweight `CteStep` holding the `CteRef` and the enclosing builder.
	- `CteStep.ref()` returns the stored `CteRef`; `CteStep.and()` returns the same `WithBuilderImpl` for further chaining.
- Keep existing `cte(...)` overloads; ensure both code paths share the same validation.
- Ensure parameter and CTE ordering reuse existing list structure; no changes to rendering pipeline.

## Testing
- Add JUnit coverage under `core/query` (or `core/query/cte`) to verify:
	- Chained `with(...).and().with(...)` registers multiple CTEs and preserves order in rendered SQL.
	- `CteStep.ref()` is usable for joins/filters.
	- Duplicate CTE name via chained `with(...)` throws `IllegalArgumentException`.
	- Duplicate column alias via chained `with(...)` throws `IllegalArgumentException`.
	- Empty projection in the CTE body still triggers the existing validation.
	- Dialect without CTE support still fails fast when calling `main(...)`.

## Backward Compatibility
- Existing `cte(...)` + `main(...)` usage remains valid.
- New chaining API is additive and optional; no behavioural change to existing callers.
