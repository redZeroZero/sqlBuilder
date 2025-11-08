# sqlBuilder Functional Specification — Common Table Expressions (CTEs)

**Title:** WITH / CTE Support  
**Version:** 0.1  
**Audience:** sqlBuilder contributors and advanced consumers  
**Scope:** Introduce first-class support for Common Table Expressions (CTEs) using `WITH` clauses,
aligned with existing:
- `Query` / `QueryImpl`
- `SqlAndParams`
- `CompiledQuery`
- `Dialect`
- parameter binding and placeholders

This spec targets **non-recursive** CTEs as a first step.

---

## 1. Goals

1. Allow users to express SQL `WITH ... AS (...)` CTEs in a fluent, type-safe way.
2. Treat each CTE as:
   - a named, reusable subquery,
   - usable as a `FROM` / `JOIN` source in subsequent queries.
3. Ensure full compatibility with:
   - parameterized rendering (`?` + params),
   - `CompiledQuery` / structural caching,
   - dialect abstraction (`Dialect`).
4. Maintain predictable, debuggable SQL output:
   - No hidden condition rewriting.
   - CTE semantics are explicit and visible.

Non-goals (for v1):
- Recursive CTEs (`WITH RECURSIVE`).
- Vendor-specific extensions around materialization hints.

---

## 2. Conceptual Model

A CTE is modeled as:

```text
WITH cte_name AS ( <subquery> ), cte2 AS ( <subquery2> )
SELECT ...
FROM cte_name
JOIN cte2 ...
```

In `sqlBuilder` terms:

- A CTE definition wraps an existing `Query`.
- The CTE has:
  - a name,
  - zero or more column aliases (optional for v1),
  - a `Query` body.
- The main query (the “outer” SELECT) can reference that CTE as if it were a table.

CTEs are **scoped** to the statement in which they appear:
- A CTE is visible in:
  - the main query body,
  - CTEs defined *after* it (if desired and supported).
- CTEs are **not** globally registered.

---

## 3. Public API

### 3.1 CTE Builder Entry Point

Add a fluent entry point to start building a query with CTEs.

Option A (recommended): `SqlQuery.with()`

```java
WithBuilder with = SqlQuery.with();
```

### 3.2 Declaring CTEs

On `WithBuilder`:

```java
CteRef cte(String name, Query cteQuery);

CteRef cte(String name, Query cteQuery, String... columnAliases);
```

**Requirements:**

1. `name`:
   - Must be a valid SQL identifier (validated minimally or delegated to Dialect quoting).
   - Must be unique within a given `WITH` block.
2. `cteQuery`:
   - Any `Query` built via the existing DSL (including joins, groups, etc.).
3. `columnAliases` (optional):
   - If provided, MUST match the number of projected columns in `cteQuery`
     (or be validated as best-effort).
   - Dialect-specific nuances follow standard SQL rules.

`cte(...)` returns a `CteRef` (or `Table`-like descriptor) that can be used in `FROM` / `JOIN` clauses of subsequent queries.

### 3.3 Referencing CTEs

`CteRef` behaves like a logical table:

```java
interface CteRef /* or extends Table-like interface */ {
    String getName();        // CTE name
    ColumnRef<?> col(String aliasOrName); // resolve projected columns (optional v1)
}
```

Usage in DSL:

```java
CteRef salaryAvg = with.cte("salary_avg", salaryAvgQuery);

Query main = with.main(
    SqlQuery
        .from(Employee.TABLE)
        .join(salaryAvg).on(Employee.ID, salaryAvg.col("EMPLOYEE_ID"))
        .select(Employee.FIRST_NAME, Employee.LAST_NAME)
);
```

### 3.4 Attaching the Main Query

`WithBuilder` exposes:

```java
Query main(Query body);
```

This returns a `Query` (or `WithQuery`) representing:

```sql
WITH ...cte definitions...
<body query>
```

**Key property:**

- From the caller’s perspective, the result is “just a Query”:
  - can be `render(...)`,
  - can be `compile(...)`,
  - can be treated like any other query object.

---

## 4. Rendering Semantics

### 4.1 Non-Compiled (`render`)

For:

```java
SqlAndParams sp = SqlQuery
    .with()
    .cte("salary_avg", salaryAvgQuery)
    .main(mainQuery)
    .render(dialect);
```

Rendering MUST:

1. Render each CTE query into SQL fragments using the same `Dialect`.
2. Preserve CTE order as declared.
3. Build final SQL:

```sql
WITH salary_avg AS (
    <rendered SQL of salaryAvgQuery>
),
other_cte AS (
    <rendered SQL of other_cte>
)
<rendered SQL of mainQuery>
```

4. Build `params` as concatenation of:
   - all CTE params in declaration order,
   - followed by main query params in normal traversal order.

**No special treatment**: CTEs are just structured subqueries with names.

### 4.2 Compiled (`compile`)

For:

```java
CompiledQuery cq = SqlQuery
    .with()
    .cte("salary_avg", salaryAvgQuery)
    .main(mainQuery)
    .compile(dialect);
```

Compilation MUST:

- Produce a single SQL string with `WITH` + main query.
- Produce a flat placeholder list (`Placeholder`s) for:
  - all CTEs’ placeholders,
  - all main query placeholders.

Binding rules are identical:
- `bind(...)` simply supplies values.
- No structural mutation occurs at bind-time.

---

## 5. Parameter Binding Rules with CTEs

1. Parameters inside CTEs are treated the same as in any subquery:
   - All `?` positions are recorded as placeholders in `CompiledQuery`.
   - All values must be supplied via `bind(...)` when executing.

2. CTE-local and main-query parameters share the same namespace:
   - If using `SqlParameter` with names, names must be unique at the statement level
     or well-defined (last write wins is NOT allowed).
   - Best practice: distinct `SqlParameter` instances per logical role.

3. The order of parameters in `SqlAndParams`:
   - MUST match the textual order of `?` in the final SQL:
     - CTE1 params,
     - then CTE2 params,
     - ...,
     - then main query params.

This preserves compatibility with JDBC drivers and your existing model.

---

## 6. Dialect Integration

### 6.1 Base Assumption

Most target dialects (Oracle, Postgres, SQL Server, etc.) support `WITH` CTE syntax.

### 6.2 `Dialect` Considerations

Optionally, extend `Dialect` with:

```java
default boolean supportsCte() {
    return true;
}
```

**Rules:**

- If `supportsCte() == true`:
  - Use standard `WITH` syntax.
- If `supportsCte() == false`:
  - For v1, either:
    - throw `UnsupportedOperationException` when rendering/compiling a CTE query, or
    - keep CTE APIs but document that some dialects cannot execute them.

For v1, no transformation fallback (e.g. inlining as subqueries) is required.

---

## 7. Examples

### 7.1 Simple CTE

**DSL:**

```java
Query topDepartments = SqlQuery
    .from(Department.TABLE)
    .select(Department.ID, Department.NAME)
    .where(Department.ACTIVE).eq(true);

Query main = SqlQuery
    .with()
    .cte("top_departments", topDepartments)
    .main(
        SqlQuery
            .from("top_departments")
            .selectAll()
    );
```

**Rendered (Postgres-style):**

```sql
WITH top_departments AS (
    SELECT d.id, d.name
    FROM department d
    WHERE d.active = ?
)
SELECT *
FROM top_departments
```

`params = [true]`.

---

### 7.2 CTE + Join + Params

**DSL:**

```java
Query salaryAvg = SqlQuery
    .from(Employee.TABLE)
    .join(Job.TABLE).on(Employee.ID, Job.EMPLOYEE_ID)
    .select(Employee.ID.as("employee_id"))
    .select(avg(Job.SALARY).as("avg_salary"))
    .groupBy(Employee.ID);

SqlParameter<Integer> pMin = SqlParameters.param("minAvg");

Query main = SqlQuery
    .with()
    .cte("salary_avg", salaryAvg)
    .main(
        SqlQuery
            .from(Employee.TABLE)
            .join("salary_avg").on(Employee.ID, col("salary_avg", "employee_id"))
            .select(Employee.FIRST_NAME, Employee.LAST_NAME)
            .where(col("salary_avg", "avg_salary")).supOrEqTo(pMin)
    );
```

**Semantics:**

- Single compiled SQL with CTE and bind parameter for `minAvg`.
- Can be reused with different `bind(Map.of("minAvg", value))`.

---

## 8. Interaction with Optional Filters

CTEs work seamlessly with:

- `whereIf*` (build-time optional filters) inside CTEs or main query.
- `whereOptional*` (bind-time optional filters) inside CTEs or main query.

Key property:

- Once the query with CTEs is compiled:
  - all optional behavior is encoded either structurally (`whereIf*`) or logically (`whereOptional*`),
  - CTEs don’t change how params or placeholders work.

---

## 9. Error Handling

1. Duplicate CTE names:
   - MUST throw an exception during build/compile.
2. Invalid names (empty, null):
   - MUST throw during CTE creation.
3. Inconsistent column aliases count (if enforced):
   - MUST throw during CTE creation or compile.
4. Usage of `CteRef` outside its `WITH`:
   - MUST be rejected or rendered invalid; implementation should keep references scoped.
5. Dialect without CTE support:
   - Rendering/compilation of CTE-based queries MUST fail fast with an explanatory error.

---

## 10. Summary

This CTE design:

- Extends `sqlBuilder` with a powerful and standard SQL feature.
- Reuses all existing foundations:
  - fluent queries,
  - parameter binding,
  - compiled queries,
  - dialect abstraction.
- Stays explicit and predictable:
  - CTEs are real named subqueries,
  - no hidden rewrites,
  - no surprises at bind-time.

It also positions `sqlBuilder` as a serious tool for complex reporting queries, while keeping the mental model consistent with the rest of the framework.
