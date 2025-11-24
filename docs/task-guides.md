# Task Guides

## Selecting & filtering

```java
SqlAndParams selectByName = SqlQuery.newQuery()
    .select(Employee.C_ID)
    .where(Employee.C_FIRST_NAME).eq("Alice")
    .render();
```

Use `like(...)`/`notLike`, `between`, `in`/`notIn`, `isNull`/`isNotNull`, or subqueries (`in(subquery)`, `exists(...)`).

## Joins

```java
String sql = SqlQuery.query()
    .select(Employee.C_FIRST_NAME, Job.C_DESCRIPTION)
    .leftJoin(schema.getTableBy(Job.class)).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
    .where(Job.C_SALARY).supOrEqTo(50_000)
    .render().sql();
```

## Aggregates, GROUP BY, HAVING

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

## Pagination

```java
SqlQuery.query()
    .select(Job.C_DESCRIPTION)
    .from(schema.getTableBy(Job.class))
    .orderBy(Job.C_SALARY, SortDirection.DESC)
    .limitAndOffset(10, 20)
    .render();
```

## Set operations

```java
SqlQuery.newQuery()
    .select(schema.getTableBy(Employee.class))
    .union(SqlQuery.newQuery().select(schema.getTableBy(Job.class)).asQuery())
    .render().sql();
```

`unionAll`, `intersect`, and `except` are available (`except` maps to `MINUS` for the default Oracle dialect).

## Derived tables & CTEs

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

## Optional filters for compiled queries

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

## Grouped filters (nested AND/OR)

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

## Raw fragments

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

## Full raw SQL

When you already have a complete SQL statement and just need an `SqlAndParams`, use:

```java
SqlAndParams raw = SqlQuery.raw(
    "SELECT * FROM employees WHERE status = ? AND salary > ?",
    "ACTIVE", 80_000);
```

This bypasses validation and staging entirely; parameters are positional in the order provided.

## DML (update / insert / delete)

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

---

# Reference Examples

- `render()` returns SQL with placeholders; `SqlFormatter.inlineLiterals(sp, dialect)` inlines values when you truly need raw SQL (loses bind safety).
- `prettyPrint()` formats the current query with one clause per line for debugging.
- Stage widening: `SqlQuery.asQuery(stage)` widens staged builders when you need clauses outside the current stage.
- Validation surfaces grouping/alias/parameter issues early; use it in tests or startup checks.
- The builder is not thread-safe; compiled queries are immutable and cache-friendly.

## 1. Simple Projection

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

## 2. Joins with Filters

```java
String sql = Query.newQuery()
    .select(Employee.C_FIRST_NAME, Job.C_DESCRIPTION)
    .leftJoin(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
    .where(Job.C_SALARY).supOrEqTo(50000)
    .render().sql();
```

## 3. Aggregations with GROUP BY / HAVING

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

## 4. Pagination (Oracle-style)

```java
String sql = Query.newQuery()
    .select(Job.C_DESCRIPTION)
    .from(job)
    .orderBy(Job.C_SALARY, SortDirection.DESC)
    .limitAndOffset(10, 20)
    .render().sql();
```

## 5. Quick Count / Pretty Print

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

## 6. Set Operations

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

`except` maps to `MINUS` for the default Oracle dialect; `exceptAll` currently throws because `MINUS ALL` is not available.

## 7. Derived Tables (FROM Subqueries)

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

## 8. Common Table Expressions (CTEs)

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
```

Alternatives:

- Chained staged CTE: `SqlQuery.withCte("salary_avg").as(avgSalary, "EMPLOYEE_ID", "AVG_SALARY")`
- One-expression chain with multiple CTEs:
  ```java
  SqlAndParams oneLiner = SqlQuery.withChain()
      .cte("salary_avg", avgSalary, "EMPLOYEE_ID", "AVG_SALARY")
      .cte("dept_totals", deptTotals, "DEPT_ID", "TOTAL_SALARY")
      .attach(chain -> SqlQuery.newQuery()
          .select(chain.ref("salary_avg").column("EMPLOYEE_ID"))
          .select(chain.ref("dept_totals").column("TOTAL_SALARY"))
          .from(chain.ref("salary_avg"))
          .join(chain.ref("dept_totals")).on(
              chain.ref("salary_avg").column("EMPLOYEE_ID"),
              chain.ref("dept_totals").column("DEPT_ID"))
          .asQuery())
      .render();
  ```
- No-lambda variant: build chain → build main using `chain.ref(...)` → `chain.attach(main).render()`.

`cte(name, query, columnAliases)` captures any query (including joins, groups, optional filters). Bind variables inside CTEs render before main-query parameters. Dialects can opt out via `Dialect.supportsCte()`.

## 9. Filtering with Subqueries

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

## 10. Optional Filters for Compiled Queries

Use `whereOptional*` helpers to keep SQL stable while toggling filters at bind time.

## 11. Grouped Filters (Nested AND / OR Trees)

Build parenthesised predicates with `QueryHelper.group()`; works for WHERE and HAVING.

## 12. Raw SQL Fragments

`*Raw(...)` overloads exist on all clauses. Raw fragments render verbatim; parameters are merged in execution order.

## 13–15. DML

- Updates: `SqlQuery.update(table).set(...).where(...).compile();`
- Inserts: `insertInto(table).columns(...).values(...)` or `select(subquery)`.
- Deletes: `deleteFrom(table)` mirrors the predicate API.
