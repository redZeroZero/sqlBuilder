# sqlBuilder

sqlBuilder is a lightweight fluent DSL for assembling SQL in Java 21 without string concatenation. It focuses on expressive builders, dialect-aware rendering, and a small stable API surface.

## What do you want to do?

- [Get started in 60 seconds](#get-started-in-60-seconds)
- [Build & test locally](#build--test)
- [Understand the public API](#public-api-surface--stability)
- [Learn the DSL basics](#core-concepts)
- [Use the DSL by task](#task-guides)
- [Model schemas](#schema-modeling)
- [Switch dialects / functions](#dialects--functions)
- [Integrate (Postgres, Spring Boot, Spring JDBC)](#integration-modules)
- [Reference notes](#reference)

## Get started in 60 seconds

1. Build locally (installs the snapshot to `~/.m2`):
   ```bash
   mvn -q -DskipTests package
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

## Repository layout

- `core/` — distributable DSL, factories, validators, annotation processor (packaged; build runs with `-proc:none`).
- `examples/` — sample schema, `MainApp`, benchmarks, integration-style tests (runs the processor).

## Public API Surface & Stability

| Package / Namespace | Purpose | Stability |
| --- | --- | --- |
| `org.in.media.res.sqlBuilder.api.model` (incl. `.annotation`) | Table/column/schema descriptors and annotation processor contracts. | **Stable** |
| `org.in.media.res.sqlBuilder.api.query` (+ subpackages except `.spi`) | Fluent SQL builders, dialect abstractions, helper utilities, formatters. | **Stable** |
| `org.in.media.res.sqlBuilder.api.query.spi` | Extension hooks for custom clauses/transpilers. | **Advanced / SPI** |
| `org.in.media.res.sqlBuilder.api.query.params`, `.helper`, `.format` | Parameter helpers, optional-condition builders, `SqlFormatter`. | **Stable** |
| `org.in.media.res.sqlBuilder.core.*`, `.processor.*`, `.examples.*` | Internal implementations and docs fixtures. | **Internal** |

## Core concepts

- **Entry points:** `SqlQuery.newQuery()` yields staged builders (`SelectStage` → `FromStage`) with compile-time clause hints. `SqlQuery.query()` widens immediately to `Query`; both converge once you reach `Query`.
- **Rendering:** `render()` returns `SqlAndParams` (SQL with `?` placeholders + ordered params). Use `.sql()` for the SQL string, `.params()` for the values. `transpile()` exists for SPI code but is deprecated for user flows.
- **Validation:** `SqlQuery.validate(query)` runs structural checks (grouping, aliases, parameters) without executing SQL.
- **Dialect propagation:** Builders carry the active `Dialect`. Use `SqlQuery.newQuery(myDialect)` or set it on your schema. Dialect controls quoting, pagination, set operators, LIKE escaping, and function rendering.
- **Compiled queries:** `compile()` freezes SQL + placeholders; later `bind(...)` supplies values (map binding rejects unknown names).

## Task guides

### Selecting & filtering

```java
SqlAndParams selectByName = SqlQuery.newQuery()
    .select(Employee.C_ID)
    .where(Employee.C_FIRST_NAME).eq("Alice")
    .render();
```

Use `like(...)`/`notLike`, `between`, `in`/`notIn`, `isNull`/`isNotNull`, or subqueries (`in(subquery)`, `exists(...)`).

### Joins

```java
String sql = SqlQuery.query()
    .select(Employee.C_FIRST_NAME, Job.C_DESCRIPTION)
    .leftJoin(schema.getTableBy(Job.class)).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
    .where(Job.C_SALARY).supOrEqTo(50_000)
    .render().sql();
```

### Aggregates, GROUP BY, HAVING

```java
String sql = SqlQuery.query()
    .select(Employee.C_FIRST_NAME)
    .select(AggregateOperator.AVG, Job.C_SALARY)
    .join(schema.getTableBy(Job.class)).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
    .groupBy(Employee.C_FIRST_NAME)
    .having(Job.C_SALARY).avg(Job.C_SALARY).supTo(60_000)
    .orderBy(Employee.C_FIRST_NAME)
    .render().sql();
```

### Pagination

```java
SqlQuery.query()
    .select(Job.C_DESCRIPTION)
    .from(schema.getTableBy(Job.class))
    .orderBy(Job.C_SALARY, SortDirection.DESC)
    .limitAndOffset(10, 20)
    .render();
```

### Set operations

```java
SqlQuery.newQuery()
    .select(schema.getTableBy(Employee.class))
    .union(SqlQuery.newQuery().select(schema.getTableBy(Job.class)).asQuery())
    .render().sql();
```

`unionAll`, `intersect`, and `except` are available (`except` maps to `MINUS` for the default Oracle dialect).

### Derived tables & CTEs

```java
Query summary = SqlQuery.newQuery()
    .select(Employee.C_ID)
    .select(AggregateOperator.AVG, Job.C_SALARY)
    .from(schema.getTableBy(Employee.class))
    .join(schema.getTableBy(Job.class)).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
    .groupBy(Employee.C_ID)
    .asQuery();

Table salaryAvg = SqlQuery.toTable(summary, "SALARY_AVG", "EMPLOYEE_ID", "AVG_SALARY");

SqlAndParams sap = SqlQuery.with()
    .cte("salary_avg", summary, "EMPLOYEE_ID", "AVG_SALARY")
    .main(
        SqlQuery.newQuery()
            .select(Employee.C_FIRST_NAME)
            .from(schema.getTableBy(Employee.class))
            .join(salaryAvg).on(Employee.C_ID, salaryAvg.get("EMPLOYEE_ID"))
            .where(salaryAvg.get("AVG_SALARY")).supOrEqTo(80_000)
            .asQuery()
    ).render();
```

### Optional filters for compiled queries

```java
SqlParameter<String> pName = SqlParameters.param("name");
SqlParameter<Integer> pMinSalary = SqlParameters.param("minSalary");

CompiledQuery cq = SqlQuery.newQuery()
    .select(Employee.C_FIRST_NAME)
    .from(schema.getTableBy(Employee.class))
    .whereOptionalEquals(Employee.C_FIRST_NAME, pName)
    .whereOptionalGreaterOrEqual(Employee.C_SALARY, pMinSalary)
    .asQuery()
    .compile();
```

Map binding applies or skips each optional predicate based on `null` values; positional `bind(...)` only works when each placeholder name is unique.

### Grouped filters (nested AND/OR)

```java
var stateGroup = QueryHelper.group()
    .where(Employee.C_STATE).eq("CA")
    .or(Employee.C_STATE).eq("OR");

var salaryGroup = QueryHelper.group()
    .where(Job.C_SALARY).supOrEqTo(120_000)
    .orGroup()
        .where(Job.C_SALARY).between(80_000, 90_000)
    .endGroup();

SqlQuery.newQuery()
    .select(Employee.C_FIRST_NAME)
    .from(schema.getTableBy(Employee.class))
    .join(schema.getTableBy(Job.class)).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
    .where(stateGroup)
    .and(salaryGroup)
    .render();
```

### Raw fragments

Use `*Raw(...)` when you must bypass validation (vendor functions, hints, custom predicates):

```java
SqlParameter<Integer> minProjects = SqlParameters.param("minProjects");

SqlQuery.newQuery()
    .selectRaw("emp.*, COUNT(*) OVER (PARTITION BY emp.DEPARTMENT_ID) AS dept_count")
    .fromRaw("HR.EMPLOYEE emp")
    .whereRaw("EXISTS (SELECT 1 FROM HR.PROJECT p WHERE p.EMP_ID = emp.ID AND p.STATE = 'ACTIVE')")
    .andRaw("emp.PROJECT_COUNT >= ?", minProjects)
    .orderByRaw("emp.HIRE_DATE DESC NULLS LAST")
    .render();
```

### DML (update / insert / delete)

```java
SqlParameter<Integer> empId = SqlParameters.param("empId");
SqlParameter<Double> newSalary = SqlParameters.param("newSalary");

CompiledQuery updateSalary = SqlQuery.update(schema.getTableBy(Employee.class))
    .set(Employee.C_SALARY, newSalary)
    .set(Employee.C_UPDATED_AT, LocalDateTime.now())
    .where(Employee.C_ID).eq(empId)
    .compile();

SqlAndParams bound = updateSalary.bind(Map.of("empId", 42, "newSalary", 120_000d));
```

`insertInto` supports multi-row or `INSERT ... SELECT`; `deleteFrom` shares the predicate DSL. Raw `setRaw`/`valuesRaw` are available when needed.

## Dialects & functions

- Supply a dialect: `SqlQuery.newQuery(new PostgresDialect())` or set one on your schema. Dialect controls quoting, pagination, set operators, LIKE escaping, and function rendering.
- Built-ins target Oracle (OFFSET/FETCH, `MINUS` for `EXCEPT`). Override via `Dialects.postgres()` or custom implementations.
- Functions: implement `renderFunction(logicalName, argsSql)` to map logical names (`lower`, `coalesce`, etc.) to SQL.

## Schema modeling

### Annotated tables + scanning

```java
@SqlTable(name = "Customer", alias = "C")
public final class Customer {
    @SqlColumn(name = "ID", javaType = Long.class) public static ColumnRef<Long> ID;
    @SqlColumn(name = "FIRST_NAME", alias = "firstName", javaType = String.class) public static ColumnRef<String> FIRST_NAME;
    @SqlColumn(name = "LAST_NAME", alias = "lastName", javaType = String.class) public static ColumnRef<String> LAST_NAME;
    private Customer() {}
}

public final class SalesSchema extends ScannedSchema {
    public SalesSchema() { super("com.acme.sales.schema"); }
}
```

Run the annotation processor to generate `<Table>Columns` + `<Table>ColumnsImpl`. Fetch typed handles via:

```java
SalesSchema schema = new SalesSchema();
CustomerColumns cols = schema.facets().columns(Customer.class, CustomerColumns.class);
SqlQuery.newQuery().select(cols.ID(), cols.FIRST_NAME()).where(cols.LAST_NAME()).like("%son").render();
```

Classpath scanning can be restricted in shaded/fat jars; fall back to manual registration if needed.

### Manual table registration

```java
ColumnRef<Integer> EMP_ID = ColumnRef.of("ID", Integer.class);
Table employee = Tables.builder("Employee", "E")
    .column(EMP_ID)
    .column("ACTIVE", "isActive")
    .build();
```

`Tables.builder(...).build()` binds descriptors and returns immutable tables; rebuild to change columns.

### Typed rows

```java
var facet = schema.facets().facet(Customer.class);
CustomerColumns columns = schema.facets().columns(Customer.class, CustomerColumns.class);
TableRow row = facet.rowBuilder()
    .set(columns.ID(), 42L)
    .set(columns.FIRST_NAME(), "Ada")
    .build();
```

## Integration modules

- **PostgreSQL integration**: see `integration/` for dialect and examples wired to Postgres.
- **Spring Boot demo API**: sample REST API showing query construction + execution.
- **Spring JDBC integration**: adapters for `JdbcTemplate` using `CompiledQuery` / `SqlAndParams`.

## Reference

- `render()` returns SQL with placeholders; `SqlFormatter.inlineLiterals(sp, dialect)` inlines values when you truly need raw SQL (loses bind safety).
- `prettyPrint()` formats the current query with one clause per line for debugging.
- Stage widening: `SqlQuery.asQuery(stage)` widens staged builders when you need clauses outside the current stage.
- Validation surfaces grouping/alias/parameter issues early; use it in tests or startup checks.
- The builder is not thread-safe; compiled queries are immutable and cache-friendly.
```java
SqlParameter<Integer> minSalary = SqlParameters.param("minSalary");

CompiledQuery salaryFilter = SqlQuery.newQuery()
    .select(Employee.C_FIRST_NAME)
    .join(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
    .where(Job.C_SALARY).supOrEqTo(minSalary)
    .compile();

SqlAndParams firstRun = salaryFilter.bind(Map.of("minSalary", 80_000));
SqlAndParams secondRun = salaryFilter.bind(90_000); // positional binding
```
> Map-based binding now rejects unknown parameter names, and positional (`bind(...)`) bindings are only allowed when each placeholder name appears once. Prefer `bind(Map)` when parameters repeat.


### 1. Simple Projection

```java
SqlQuery.newQuery()
    .select(employee) // or rely on descriptor shortcuts
    .render().sql();
```

Expected SQL:

```
SELECT Employee.ID, Employee.FIRST_NAME, ...
 FROM Employee
```

### 2. Joins with Filters

```java
String sql = Query.newQuery()
    .select(Employee.C_FIRST_NAME, Job.C_DESCRIPTION)
    .leftJoin(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
    .where(Job.C_SALARY).supOrEqTo(50000)
    .render().sql();
```

### 3. Aggregations with GROUP BY / HAVING

```java
String sql = Query.newQuery()
    .select(Employee.C_FIRST_NAME)
    .select(AggregateOperator.AVG, Job.C_SALARY)
    .join(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
    .groupBy(Employee.C_FIRST_NAME)
    .having(Job.C_SALARY).avg(Job.C_SALARY).supTo(60000)
    .orderBy(Employee.C_FIRST_NAME)
    .render().sql();
```

### 4. Pagination (Oracle-style)

```java
String sql = Query.newQuery()
    .select(Job.C_DESCRIPTION)
    .from(job)
    .orderBy(Job.C_SALARY, SortDirection.DESC)
    .limitAndOffset(10, 20)
    .render().sql();
```

### 5. Quick Count / Pretty Print

```java
String sql = SqlQuery.countAll().render().sql();             // SELECT COUNT(*)

Query printable = SqlQuery.newQuery()
    .select(Employee.C_FIRST_NAME)
    .from(employee)
    .asQuery();
printable.where(Employee.C_FIRST_NAME).eq("Alice");

System.out.println(printable.prettyPrint());
/*
SELECT
  Employee.FIRST_NAME as firstName
FROM
  Employee
WHERE
  Employee.FIRST_NAME = ?
*/
```

### 6. Set Operations

```java
String sql = SqlQuery.newQuery()
    .select(employee)
    .union(
        SqlQuery.newQuery()
            .select(job)
            .asQuery()
    )
    .render().sql();
```

This renders `UNION` between the two subqueries. Use `unionAll`, `intersect`, or `except` for the other set operators. The default Oracle-oriented dialect maps `except` to `MINUS`; `exceptAll` currently throws because `MINUS ALL` is not available.

### 7. Derived Tables (FROM Subqueries)

Build a subquery once, expose its columns, and reuse it as a table source:

```java
Query salarySummary = SqlQuery.newQuery()
    .select(Employee.C_ID)
    .select(AggregateOperator.AVG, Job.C_SALARY)
    .from(employee)
    .join(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
    .groupBy(Employee.C_ID)
    .asQuery();

Table salaryAvg = SqlQuery.toTable(salarySummary, "SALARY_AVG", "EMPLOYEE_ID", "AVG_SALARY");

String sql = SqlQuery.newQuery()
    .select(Employee.C_FIRST_NAME)
    .from(employee)
    .join(salaryAvg).on(Employee.C_ID, salaryAvg.get("EMPLOYEE_ID"))
    .where(salaryAvg.get("AVG_SALARY")).supOrEqTo(60000)
    .render().sql();
```

Call `SqlQuery.toTable(query)` to auto-generate aliases (or supply your own as above). Each column alias you provide or that is inferred is available via `salaryAvg.get("ALIAS")`, so subsequent clauses can reference the derived table just like any other.

### 8. Common Table Expressions (CTEs)

Build reusable subqueries once, give them a name, and reference them like tables via `SqlQuery.with()`:

```java
Query avgSalary = SqlQuery.newQuery()
    .select(Employee.C_ID)
    .select(AggregateOperator.AVG, Job.C_SALARY)
    .from(employee)
    .join(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
    .groupBy(Employee.C_ID)
    .asQuery();

WithBuilder with = SqlQuery.with();
CteRef salaryAverages = with.cte("salary_avg", avgSalary, "EMPLOYEE_ID", "AVG_SALARY");

SqlAndParams sp = with.main(
    SqlQuery.newQuery()
        .select(Employee.C_FIRST_NAME)
        .from(employee)
        .join(salaryAverages).on(Employee.C_ID, salaryAverages.column("EMPLOYEE_ID"))
        .where(salaryAverages.column("AVG_SALARY")).supOrEqTo(80_000)
        .asQuery()
).render();

sp.sql();
// WITH "salary_avg"("EMPLOYEE_ID", "AVG_SALARY") AS (...) SELECT ...
```

`cte(name, query, columnAliases)` captures any query (including joins, groups, optional filters). Each call returns a `CteRef`, which exposes columns via `column("ALIAS")` or `col("ALIAS")`. Bind variables declared inside CTEs are rendered before main-query parameters, so JDBC bindings follow SQL order. Dialects can opt out via `Dialect.supportsCte()`; attempting to render a CTE with an unsupported dialect raises `UnsupportedOperationException`.

### 9. Filtering with Subqueries

```java
Query highSalaryIds = SqlQuery.newQuery()
    .select(Job.C_EMPLOYEE_ID)
    .from(job)
    .where(Job.C_SALARY).supOrEqTo(60000)
    .asQuery();

String sql = SqlQuery.newQuery()
    .select(Employee.C_FIRST_NAME)
    .from(employee)
    .where(Employee.C_ID).in(highSalaryIds)
    .exists(SqlQuery.newQuery().select(Job.C_ID).from(job).asQuery())
    .render().sql();
```

Scalar comparisons, `IN` / `NOT IN`, and `EXISTS` / `NOT EXISTS` all accept subqueries. `exists(subquery)` can be called directly on the fluent query DSL, and it will emit `WHERE EXISTS (...)` without requiring a placeholder column.

### 10. Optional Filters for Compiled Queries

When you need a single compiled SQL statement that conditionally applies filters based on bound parameters, use the `whereOptional*` helpers:

- `whereOptionalEquals(column, param)`
- `whereOptionalLike(column, param)`
- `whereOptionalGreaterOrEqual(column, param)`

Each helper emits `(param IS NULL OR column <op> param)` so the SQL structure never changes.

```java
SqlParameter<String> pName = SqlParameters.param("name");
SqlParameter<Integer> pMinSalary = SqlParameters.param("minSalary");
SqlParameter<String> pPattern = SqlParameters.param("pattern");

CompiledQuery cq = SqlQuery.newQuery()
    .select(Employee.C_FIRST_NAME)
    .from(employee)
    .whereOptionalEquals(Employee.C_FIRST_NAME, pName)
    .whereOptionalLike(Employee.C_LAST_NAME, pPattern)
    .whereOptionalGreaterOrEqual(Employee.C_SALARY, pMinSalary)
    .asQuery()
    .compile();

Map<String, Object> disabled = new HashMap<>();
disabled.put("name", null);
disabled.put("pattern", null);
disabled.put("minSalary", null);
cq.bind(disabled); // emits no filters

Map<String, Object> enabled = new HashMap<>();
enabled.put("name", "Alice");
enabled.put("pattern", null);      // still safe to bind null
enabled.put("minSalary", 80_000);
cq.bind(enabled); // applies NAME + salary filters
```

Each helper references the same `SqlParameter` twice, so the bound value is reused for both `IS NULL` and the real predicate. This keeps plan caching intact while letting you toggle conditions at bind time without rebuilding SQL.

### 11. Grouped Filters (Nested AND / OR Trees)

Use `QueryHelper.group` to build parenthesised predicates that mirror SQL's boolean syntax. `and(...)` / `or(...)` automatically target the active clause (WHERE vs. HAVING), so you can chain grouped expressions fluently:

```java
var stateGroup = QueryHelper.group()
    .where(Employee.C_STATE).eq("CA")
    .or(Employee.C_STATE).eq("OR");

var salaryGroup = QueryHelper.group()
    .where(Job.C_SALARY).supOrEqTo(120_000)
    .orGroup()
        .where(Job.C_SALARY).between(80_000, 90_000)
    .endGroup();

String sql = SqlQuery.newQuery()
    .select(Employee.C_FIRST_NAME)
    .from(employee)
    .join(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
    .where(stateGroup)          // WHERE (E.STATE = 'CA' OR E.STATE = 'OR')
    .and(salaryGroup)           //   AND (J.SALARY >= 120000 OR (J.SALARY BETWEEN 80000 AND 90000))
    .render().sql();
```

Call `QueryHelper.group()` without arguments when you want an inline builder that can be passed straight into `.where(...)`, `.and(...)`, or `.having(...)`. Chain `.andGroup()` / `.orGroup()` whenever you need nested parentheses, then finish the nested block with `.endGroup()`—no lambdas required. (The consumer overload remains available if you prefer that style.)

The same helper works for HAVING clauses: call `query.having(QueryHelper.group(...)).and(...)` to keep aggregates nested under a single `HAVING` block without hand-written parentheses.

### 12. Raw SQL Fragments

When you need to drop all safety rails—for example, to use vendor-specific functions, hints, or hand-written predicates—every clause exposes a `*Raw(...)` overload. Each method comes in three ergonomic forms:

- `selectRaw("expr")`: quick string literal, no parameters.
- `selectRaw("expr = ?", SqlParameters.param("p"))`: string + parameters (the builder captures ordering for you).
- `selectRaw(RawSql.of("expr", param1, param2))`: pass an existing `RawSqlFragment` object if you already built one.

Raw fragments are available on `select`, `from` / all join types, `where` / `and` / `or`, `having`, `groupBy`, `orderBy`, and `with`. They render verbatim, so make sure you include any necessary whitespace. A fragment can also contain bound parameters— they’re appended to the surrounding query in the order the fragments execute.

```java
SqlParameter<Integer> minProjects = SqlParameters.param("minProjects");

String sql = SqlQuery.newQuery()
    .selectRaw("emp.*, COUNT(*) OVER (PARTITION BY emp.DEPARTMENT_ID) AS dept_count")
    .fromRaw("HR.EMPLOYEE emp")
    .whereRaw("EXISTS (SELECT 1 FROM HR.PROJECT p WHERE p.EMP_ID = emp.ID AND p.STATE = 'ACTIVE')")
    .andRaw("emp.PROJECT_COUNT >= ?", minProjects)
    .orderByRaw("emp.HIRE_DATE DESC NULLS LAST")
    .render().sql();
```

### 13. Updates (DML)

`SqlQuery.update(table)` exposes the same predicate DSL while focusing on `SET` assignments. Builders remain non-thread-safe, but compiled `UpdateQuery` artefacts can be cached just like `SELECT` statements.

```java
SqlParameter<Integer> empId = SqlParameters.param("empId");
SqlParameter<Double> newSalary = SqlParameters.param("newSalary");

CompiledQuery updateSalary = SqlQuery.update(employee)
    .set(Employee.C_SALARY, newSalary)
    .set(Employee.C_UPDATED_AT, LocalDateTime.now())
    .where(Employee.C_ID).eq(empId)
    .compile();

SqlAndParams bound = updateSalary.bind(Map.of("empId", 42, "newSalary", 120_000d));
jdbcTemplate.update(bound.sql(), bound.params().toArray());
```

Need a custom expression? Call `setRaw("SALARY = SALARY + ?", SqlParameters.param("bonus"))` to append fragments verbatim (parameters are merged into the compiled placeholder list in order).

### 14. Inserts (DML)

Use `SqlQuery.insertInto(table)` to build single-row, multi-row, or `INSERT ... SELECT` statements. Columns must be declared up front; each `values(...)` call supplies a row of literals or `SqlParameter` placeholders.

```java
SqlParameter<Integer> empId = SqlParameters.param("empId");
SqlParameter<String> empName = SqlParameters.param("empName");

CompiledQuery insertEmp = SqlQuery.insertInto(employee)
    .columns(Employee.C_ID, Employee.C_FIRST_NAME, Employee.C_DEPT_ID)
    .values(empId, empName, 42)
    .compile();

sqlBuilderJdbcTemplate.insert(insertEmp, Map.of("empId", 7, "empName", "Alice"));
```

Need to hydrate from another query? Call `select(subquery)` instead of `values(...)`. Raw fragments are available via `valuesRaw("(<expr>)", params...)` when you need to inject vendor-specific expressions.

### 15. Deletes (DML)

`SqlQuery.deleteFrom(table)` mirrors the predicate API from updates. Use it when you need to build guarded deletes or reuse `OptionalConditions`.

```java
SqlParameter<Integer> dept = SqlParameters.param("dept");

CompiledQuery pruneDept = SqlQuery.deleteFrom(employee)
    .whereOptionalEquals(Employee.C_DEPT_ID, dept)
    .compile();

sqlBuilderJdbcTemplate.delete(pruneDept, Map.of("dept", 42));
```

As with updates, builders are not thread-safe, but compiled deletes are immutable and can be cached across threads.

Use `RawSql.of(sql, params...)` anywhere you want to prebuild a fragment and reuse it across queries. Because raw snippets bypass validation, keep them focused and prefer the typed DSL when possible—the raw APIs are an escape hatch, not the primary authoring style.

## Dialects & SQL Functions

The SQL that `sqlBuilder` emits is *dialect-aware*. A `Dialect` implementation controls:

- Identifier quoting (`"EMPLOYEE"` for Oracle).
- Pagination syntax (e.g., `OFFSET ? ROWS FETCH NEXT ? ROWS ONLY`).
- Set-operator keywords (Oracle maps `EXCEPT` to `MINUS`).
- LIKE escaping (the dialect decides the escape character).
- Function rendering (logical function names → dialect-specific expressions).

### Using a Different Dialect

Pass a custom dialect when constructing a query:

```java
Dialect postgres = new PostgresDialect(); // your implementation

SqlAndParams sp = SqlQuery.newQuery(postgres)
    .select(Employee.C_FIRST_NAME)
    .where(Employee.C_LAST_NAME).like("Do%")
    .render();

sp.sql();    // SELECT "employee"."first_name" ...
sp.params(); // ["Do%"]
```

Implement `Dialect` (see `core/.../OracleDialect` or `PostgresDialect`) to customize quoting, pagination, set operators, and function rendering. Internally the DSL propagates the active dialect through a `ThreadLocal` helper called `DialectContext`; it is an implementation detail, so application code should stick to the public `Dialect` API (`SqlQuery.newQuery(myDialect)`, `schema.setDialect(...)`, etc.) instead of interacting with the context directly. The helpers in `Dialects` expose the built-in implementations (e.g., `Dialects.postgres()`), and you can set a schema-wide default via `schema.setDialect(...)`.

### Registering Functions

Aggregates and helper APIs call `Dialect.renderFunction(logicalName, args)`. Provide mappings for the logical names you care about (e.g., `lower`, `upper`, `coalesce`). Example snippet inside a dialect:

```java
@Override
public String renderFunction(String logicalName, List<String> argsSql) {
    return switch (logicalName) {
        case "lower" -> "LOWER(" + argsSql.get(0) + ")";
        case "coalesce" -> "COALESCE(" + String.join(", ", argsSql) + ")";
        default -> logicalName.toUpperCase(Locale.ROOT) + '(' + String.join(", ", argsSql) + ')';
    };
}
```

Once the dialect knows about a logical function name, the fluent API can expose helpers (e.g., `functions.lower(column)` in future extensions) without sprinkling dialect-specific SQL throughout the code base.

## Notes

- The builder creates SQL strings; execution is left to your JDBC or ORM layer. Use `Query.prettyPrint()` when you need a clause-per-line view for debugging.
- Identifiers are dialect-quoted in the emitted SQL (Oracle uses double quotes). Use aliases or override the dialect to control quoting style.
- Transpilers are pluggable. The default implementations target Oracle syntax (OFFSET/FETCH). Extend the transpiler factories to add other dialects.
- Use the fluent HAVING builder to chain aggregate comparisons (`having(col).sum(col).supTo(100)` etc.).
- WHERE / HAVING now support the full comparator set: `<>`, `LIKE`, `NOT LIKE`, `BETWEEN`, `IN` / `NOT IN`, `IS (NOT) NULL`, plus scalar and set subqueries (`eq`, `in`, `exists`).
- Subqueries can be wrapped into derived tables with `Query.as(alias, columns...)` and reused in any `FROM` / `JOIN` position.
- Prefer typed column descriptors when you need compile-time guards: `ColumnRef<BigDecimal> SALARY = ColumnRef.of("SALARY", BigDecimal.class);` lets the DSL accept `avg(SALARY)` while preventing you from applying numeric aggregates to non-numeric fields. Existing raw descriptors continue to work unchanged.
- Stage interfaces expose typed overloads, so you can call `select(customerColumns.ID())`, `where(customerColumns.LAST_NAME())`, or `like(customerColumns.LAST_NAME(), "%son")` without down-casting to `Query`.
- Annotated POJOs run through `SqlTableProcessor`, which now emits a `<Table>Columns` interface *and* a concrete `<Table>ColumnsImpl`. `TableFacets.columns(...)` automatically instantiates that implementation so you get IDE-friendly accessors without reflection.
- The build disables annotation processing (`-proc:none`) to keep local compilation simple; the processor is still packaged in the jar. Enable annotation processing in your application module (or remove that compiler arg) to have column interfaces generated automatically.
- `LIKE` patterns are escaped automatically (so `%`, `_`, and `\` become `\%`, `\_`, `\\`) and the transpiler appends `ESCAPE '\'` for you—no need to double-escape in client code.
- `EmployeeSchema` auto-discovers tables in the `org.in.media.res.sqlBuilder.example` package. Pass a different base package to scan additional modules, or plug your own schema into `SchemaScanner.scan("com.acme.sales")`.

## Configuration & Integration Tips

- **Dialect selection**: the factory layer bootstraps Oracle-oriented transpilers by default (OFFSET/FETCH pagination, `MINUS` for `EXCEPT`). To use a different dialect, supply custom implementations via `SelectTranspilerFactory`, `WhereTranspilerFactory`, etc., before constructing queries.
- **Schema wiring**: for quick starts, re-use `EmployeeSchema` as a template—create a `ScannedSchema` subclass pointing to your table descriptor package and pass it to application code that needs column handles.
- **Runtime logging**: the DSL produces plain SQL strings. Use your preferred logging framework (or `Query.prettyPrint()`) to emit the final SQL before executing it with JDBC/ORM tooling.
- **Performance baseline**: run `org.in.media.res.sqlBuilder.tools.QueryBenchmark` (`java ... QueryBenchmark <iterations>`) to get a quick feel for transpilation throughput in your environment.

## Defining Tables & Schemas

Annotate plain Java classes to describe tables and their columns. `SchemaScanner` will discover them automatically and wire column descriptors back to the DSL.

```java
@SqlTable(name = "Customer", alias = "C")
public final class Customer {

    @SqlColumn(name = "ID", javaType = Long.class)
    public static ColumnRef<Long> ID;

    @SqlColumn(name = "FIRST_NAME", alias = "firstName", javaType = String.class)
    public static ColumnRef<String> FIRST_NAME;

    @SqlColumn(name = "LAST_NAME", alias = "lastName", javaType = String.class)
    public static ColumnRef<String> LAST_NAME;

    private Customer() {} // prevent instantiation
}

// Alternatively, keep the fields as plain types and let ColumnRef descriptors be derived automatically.
// The annotation processor will generate `CustomerPlainColumns` (plus `CustomerPlainColumnsImpl`) with typed accessors.

@SqlTable(name = "Customer", alias = "C")
public final class CustomerPlain {

    @SqlColumn(name = "ID", javaType = Long.class)
    public static Long ID; // ColumnRef generated via schema facets

    @SqlColumn(name = "FIRST_NAME", alias = "firstName", javaType = String.class)
    public static String FIRST_NAME;

    @SqlColumn(name = "LAST_NAME", alias = "lastName", javaType = String.class)
    public static String LAST_NAME;

    private CustomerPlain() {}
}

CustomerPlainColumns cols = schema.facets().columns(CustomerPlain.class, CustomerPlainColumns.class);
// or, if you already have the facet instance:
CustomerPlainColumns manual = CustomerPlainColumns.of(schema.facets().facet(CustomerPlain.class));
ColumnRef<String> lastName = manual.LAST_NAME();
SqlQuery.newQuery()
    .select(cols.ID(), cols.FIRST_NAME())
    .where(cols.LAST_NAME()).like("%son")
    .render().sql();
```

If you prefer a cleaner POJO, you can also declare plain static fields (e.g., `public static Long ID;`) and specify `@SqlColumn(javaType = Long.class)`. During compilation the `SqlTableProcessor` generates both a `<TableName>Columns` interface and a matching `...ColumnsImpl` implementation with a static `of(TableFacets.Facet)` factory. `TableFacets.columns(...)` will automatically instantiate that implementation (falling back to a dynamic proxy only if no generated class exists), so you can simply call `schema.facets().columns(CustomerPlain.class, CustomerPlainColumns.class)` and stay type-safe without hand-writing any plumbing.

Use `QueryColumns` when you want to keep the table handle and typed columns together in one variable:

```java
QueryColumns<CustomerColumns> customer = QueryColumns.of(schema, CustomerColumns.class);
// or QueryColumns.of(schema, Customer.class, CustomerColumns.class);

String sql = SqlQuery.newQuery()
    .select(customer.columns().ID())
    .from(customer.table())
    .like(customer.columns().LAST_NAME(), "%son")
    .render().sql();
```

> The sample schema ships with the generated `CustomerColumns` / `CustomerColumnsImpl` pair checked into source control so the build remains stable even when annotation processing is disabled. In your own modules you can rely on the processor to emit the same code automatically.

> **Initialization checklist**
> 1. Annotate each table with `@SqlTable`/`@SqlColumn`, declaring either `ColumnRef<T>` fields or plain static fields plus `javaType`.
> 2. Run the `SqlTableProcessor` (enabled in `examples/`) so `<Table>Columns` interfaces/implementations are generated next to your descriptors.
> 3. Instantiate your `ScannedSchema`. It now fails fast if a descriptor cannot be matched to a table (`schema.getTableBy(Foo.class)` throws) and `TableFacets` immediately raises an error when a column name is missing.
> 4. Fetch typed handles via `schema.facets().columns(Foo.class, FooColumns.class)` or `QueryColumns.of(...)` and pass those `ColumnRef`s throughout the DSL.

These guards surface misconfigurations during startup instead of at query execution time.
```

To build a schema from a package (auto-detects classes like `Customer` above):

```java
// Extend ScannedSchema for your application schema
public class PayrollSchema extends ScannedSchema {
    public PayrollSchema() {
        super("com.example.payroll.tables");
    }
}

Schema schema = new PayrollSchema();
// or
List<Table> tables = SchemaScanner.scan("com.example.payroll.tables");
```

Mix-and-match is supported: legacy enum descriptors are still discovered, so you can migrate tables gradually. Call `ScannedSchema.clearCache()` if you hot-reload descriptor classes.

### Custom Schema Quick Start

1. **Model your tables** using `@SqlTable` / `@SqlColumn` annotations. Static `ColumnRef` fields become the handles used throughout the DSL.
2. **Expose a schema** by extending `ScannedSchema` (or instantiating `SchemaScanner` directly) with the package that contains those annotated classes.
3. **Bundle the schema** with your application so callers can ask for a `Table` or `ColumnRef` by descriptor class. Every column must expose its Java type either via `ColumnRef<T>` generics or `@SqlColumn(javaType = ...)`, otherwise scanning will fail early. Example:

```java
public final class SalesSchema extends ScannedSchema {
    public SalesSchema() {
        super("com.acme.sales.schema");
    }
}

SalesSchema schema = new SalesSchema();
Table customer = schema.getTableBy(Customer.class);

// Optional: fetch typed column facets for additional compile-time safety
CustomerColumns customerColumns = schema.facets().columns(Customer.class, CustomerColumns.class);
ColumnRef<String> lastName = customerColumns.LAST_NAME();
```

4. **Use the descriptors** in queries:

```java
String sql = SqlQuery.newQuery()
    .select(Customer.C_ID, Customer.C_FIRST_NAME)
    .from(customer)
    .where(Customer.C_LAST_NAME).like("%son")
    .render().sql();
```

Fluent helpers now accept typed descriptors directly, so you can skip intermediate `where(...)` calls when it reads better. For example:

```java
String sql = SqlQuery.newQuery()
    .select(Customer.C_ID)
    .from(customer)
    .like(Customer.C_LAST_NAME, "%son")
    .isNull(Customer.C_MAIL)
    .render().sql();
```

### ColumnRef lifecycle & schema immutability

`ColumnRef` instances are just descriptors (name + Java type) until they are bound to a `Table`. Binding happens automatically when `SchemaScanner` discovers annotated tables or when you pass the descriptor to `Tables.builder(...).column(...)`. Skip that step and the DSL will throw `IllegalStateException` the first time you try to use the column because the owning table/alias is unknown.

Safe binding checklist:

1. Declare descriptors (`ColumnRef<T>` fields or `@SqlColumn(javaType = ...)` metadata).
2. Bind them via `SchemaScanner` or `Tables.builder(...)`. This step assigns aliases, schema names, and registers the column with a concrete `Table`.
3. Only after binding should the `ColumnRef` be passed into queries.

`ColumnRef.of(...)` is therefore an advanced escape hatch for builders/tests—always feed the result into a schema builder so it becomes usable, rather than piping it straight to `where(...)`.

Once a schema is wired, treat it as effectively immutable. `Tables.builder(...).build()` returns tables with unmodifiable column collections, and `Schema` implementations expose read-only views to discourage runtime mutation. If you need to adjust a schema (add/remove columns or tables), create a new schema instance or table descriptor instead of mutating a shared singleton. This pattern keeps multi-threaded apps safe: bootstrap the schema during application startup, then reuse the descriptors everywhere without worrying about concurrent modifications.

### Typed Rows & Builders

Use `TableFacets` to construct strongly-typed row objects that carry values per column:

```java
var customerFacet = schema.facets().facet(Customer.class);
CustomerColumns columns = schema.facets().columns(Customer.class, CustomerColumns.class);
TableRow row = customerFacet.rowBuilder()
    .set(columns.ID(), 42L)
    .set(columns.FIRST_NAME(), "Ada")
    .build();

String name = row.get(columns.FIRST_NAME());
```

These rows can be useful for fixtures, parameter binding, or integrating with whatever persistence layer you prefer.

> **Classpath scanning caveats**  
> The built-in scanner relies on the application classloader to enumerate resources. In environments that rewrite jars (spring-boot fat jars, shaded archives), use JPMS layers, or lock down classloaders, reflection-based scanning might not see every descriptor. When that happens, fall back to manual table registration.

### Manual Table Registration

If annotation scanning is not an option, declare tables programmatically via `Tables.builder(...)`. Columns can be added by name or by binding existing `ColumnRef` descriptors:

```java
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.Tables;
import org.in.media.res.sqlBuilder.api.model.ColumnRef;

ColumnRef<Integer> EMP_ID = ColumnRef.of("ID", Integer.class);
ColumnRef<String> EMP_FIRST_NAME = ColumnRef.of("FIRST_NAME", String.class);

Table employee = Tables.builder("Employee", "E")
    .column(EMP_ID)                      // ColumnRef binding keeps type information
    .column(EMP_FIRST_NAME)
    .column("ACTIVE", "isActive")
    .build();

// employee.columns() is now unmodifiable; rebuild if you need to add/remove descriptors later.

String sql = SqlQuery.newQuery()
    .select(EMP_FIRST_NAME)
    .from(employee)
    .where(EMP_ID).eq(42)
    .render().sql();
```

> `ColumnRef.of(...)` is shown here solely to create descriptors for the builder. Always let `Tables.builder` (or `SchemaScanner`) bind them before handing the column to `Query`.

This manual approach avoids classpath scanning entirely while still delivering typed ColumnRefs to the DSL. Combine it with `QueryColumns` if you want to distribute table/column bundles throughout your application.

### Integration Module with PostgreSQL

The project now includes an `integration` module that drives `sqlBuilder` against a PostgreSQL container preloaded with a richer commerce-style schema (departments, jobs, products, customers, orders, payments, etc.). The module lives under `integration/` and exposes both a console runner (`IntegrationApp`) and a Spring Boot REST surface for exploring the DSL.

1. Start the backing database (ports, credentials, and initial data are defined in `integration/docker/docker-compose.yml` and `integration/docker/init.sql`):

   ```bash
   docker compose -f integration/docker/docker-compose.yml up -d
   ```

2. Run the integration harness once the container is ready (make sure the PostgreSQL JDBC driver is installed in your local Maven cache, e.g. `mvn dependency:get -Dartifact=org.postgresql:postgresql:42.6.0`):

   ```bash
   mvn -pl integration exec:java
   ```

   `IntegrationApp` picks up `SQLBUILDER_JDBC_URL`, `SQLBUILDER_JDBC_USER`, and `SQLBUILDER_JDBC_PASSWORD` from the environment (`jdbc:postgresql://localhost:5432/sqlbuilder`, `sb_user`, `sb_pass` by default) and prints each DSL-generated SQL statement plus the rows it retrieves.

3. When you finish, stop the database:

   ```bash
   docker compose -f integration/docker/docker-compose.yml down
   ```

You are encouraged to modify `integration/src/main/java/org/in/media/res/sqlBuilder/integration/IntegrationApp.java` and add new queries against the provided schema. All Java code runs on the host machine; the container simply provides a PostgreSQL-backed data source.

### Spring Boot demo API

The `integration` module also ships a lightweight Spring Boot app that exposes the same sample queries over HTTP and executes them directly against the containerized PostgreSQL database.

1. Ensure the Postgres container is running (see steps above).
2. Start the app:

   ```bash
   mvn -pl integration spring-boot:run
   ```

   Configuration uses the same environment variables as the console runner (`SQLBUILDER_JDBC_URL`, `SQLBUILDER_JDBC_USER`, `SQLBUILDER_JDBC_PASSWORD`). The server binds to `${PORT:-8080}`.

3. Explore the endpoints:

   - `GET /queries` — lists the available demos (id, title, description).
   - `GET /queries/{id}` — runs the selected query and returns `sql`, `params`, and `rows` as JSON.

The catalog includes the original integration scenarios (projections, joins, aggregates, pagination, CTEs, grouped/optional filters, raw fragments) plus new demos such as department salary totals, top-paid employees, order/customer joins, and per-product revenue. See `integration/SPRING_BOOT_APP.md` for a concise module guide. Optionally set `SQLBUILDER_IT=true` and run `mvn -pl integration test` to exercise the REST layer against the live database.

### Spring JDBC integration

The new `spring-jdbc` module exposes `SqlBuilderJdbcTemplate`, a minimal wrapper around Spring's `JdbcTemplate` that accepts `SqlAndParams` / `CompiledQuery` pairs without forcing you to rehydrate arrays yourself. Add the dependency:

```xml
<dependency>
  <groupId>org.in.media.res</groupId>
  <artifactId>sqlbuilder-spring-jdbc</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

> The module is built and tested against Spring Framework 6.1.x / Jakarta EE 9+ baselines, so it aligns with Spring Boot 3.x out of the box.

Configuration is just wiring:

```java
@Bean
SqlBuilderJdbcTemplate sqlBuilderJdbcTemplate(JdbcTemplate jdbcTemplate) {
    return new SqlBuilderJdbcTemplate(jdbcTemplate);
}
```

Then reuse the DSL output like this:

```java
CompiledQuery cq = SqlQuery.newQuery()
    .select(...)
    .from(...)
    .compile();

SqlAndParams sap = cq.bind(Map.of("id", 42));

List<MyDto> rows = sqlBuilderJdbcTemplate.query(sap, (rs, rowNum) -> new MyDto(...));
```

#### Debug logging with `SqlFormatter.inlineLiterals`

When reporting issues or reviewing SQL locally, render the query once, inline the literals strictly for debugging, and keep the original `SqlAndParams` for JDBC execution:

```java
SqlAndParams sap = query.render();
String debugSql = SqlFormatter.inlineLiterals(sap, dialect);
log.debug("Executing sqlBuilder query: {}", debugSql);

List<MyDto> rows = sqlBuilderJdbcTemplate.query(sap, rowMapper);
```

The string returned by `inlineLiterals(...)` is **not** meant to be executed directly because it inlines parameters and disables JDBC binding. Restrict it to logging/troubleshooting so you avoid SQL injection risks while still sharing readable SQL in bug reports or support tickets.

Need to run DML? The same facade works with the `UpdateQuery` DSL:

```java
SqlParameter<Integer> id = SqlParameters.param("id");
SqlParameter<BigDecimal> salary = SqlParameters.param("salary");

UpdateQuery updateSalary = SqlQuery.update(employee)
    .set(Employee.C_SALARY, salary)
    .where(Employee.C_ID).eq(id);

sqlBuilderJdbcTemplate.update(updateSalary, Map.of("salary", new BigDecimal("90000"), "id", 42));

DeleteQuery deleteInactive = SqlQuery.deleteFrom(employee)
    .where(Employee.C_STATUS).eq("INACTIVE");

sqlBuilderJdbcTemplate.delete(deleteInactive);
```

All methods simply delegate to the underlying `JdbcTemplate` / `NamedParameterJdbcTemplate`, so transactions, error handling, and exceptions behave exactly like Spring's APIs.

## Running Tests

```
mvn -o test
```

## Coverage

JaCoCo is wired into the parent build via `jacoco-maven-plugin`. Run the tests normally and the HTML report will be generated under `target/site/jacoco/index.html` for each module (core, examples, integration, spring-jdbc). On the next build `mvn test`, coverage data is refreshed automatically.

This executes the regression suite in `src/test/java` that covers the examples above.

## Appendix: Thread Safety Contract

`sqlBuilder` follows a clear separation between mutable builders and immutable artefacts (see `THREAD_SAFETY.md` for the detailed contract):

- **Builders are not thread-safe.** Each `Query`, `With/CTE`, `ConditionGroup`, etc. instance must live on a single thread/request. Create them per call (or via prototype-scoped beans/factories) instead of registering them as singletons.
- **Compiled artefacts are safe to share.** `CompiledQuery` and `SqlParameter` instances are immutable, so you can publish them as Spring beans or cache them globally without synchronization.
- **Bindings are disposable.** Every `bind(...)` call returns a fresh `SqlAndParams` object. Use it for one JDBC execution and then drop it; parameter lists are immutable to avoid accidental mutation.
- **Dialect scope relies on an internal `ThreadLocal`.** Each call to `render()`/`compile()` captures the dialect that was passed to `SqlQuery.newQuery(...)` (or the schema default) and stores it in `DialectContext` behind the scenes. Do not poke the context directly; instead, build a fresh query per thread with the dialect you need so the internal scope stays consistent.
- **Recommended lifecycle:** build (per request) → compile (once) → bind (per execution) → execute with JDBC. This is the pattern that keeps the DSL thread-safe while letting you reuse precompiled SQL templates.
