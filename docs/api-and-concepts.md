# API Surface & Core Concepts

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

## Window functions

- `select(WindowFunction)` renders `... OVER (...)`; helpers live in `org.in.media.res.sqlBuilder.api.query.window.WindowFunctions` (`rowNumber()`, `rank()`, `denseRank()`, `sum(col)`, `lag(col, offset)`, etc.).
- Partition/order/frame are configured on the function: `partitionBy(...)`, `orderBy(...)`, and `rowsBetween(...)` / `rangeBetween(...)` using `WindowFrame.Bound` helpers (`unboundedPreceding()`, `preceding(n)`, `currentRow()`, `following(n)`, `unboundedFollowing()`).
- Column expressions auto-register their tables. Raw expressions go through `WindowExpression.raw(sql, params)`; their parameters flow into `compile()/render()` like other raw fragments.

```java
import static org.in.media.res.sqlBuilder.api.query.window.WindowFunctions.*;
import org.in.media.res.sqlBuilder.api.query.window.WindowFrame;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.constants.SortDirection;

var runningTotal = sum(EmployeesTable.C_SALARY)
        .partitionBy(EmployeesTable.C_DEPARTMENT_ID)
        .orderBy(EmployeesTable.C_HIRED_AT, SortDirection.ASC)
        .rowsBetween(WindowFrame.Bound.unboundedPreceding(), WindowFrame.Bound.currentRow())
        .as("running_total");

var query = SqlQuery.query()
        .select(EmployeesTable.C_FIRST_NAME, EmployeesTable.C_LAST_NAME)
        .select(runningTotal)
        .from(IntegrationSchema.employees());
// SELECT ... SUM(e.salary) OVER(PARTITION BY e.department_id ORDER BY e.hired_at ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS running_total
```

Use `WindowExpression.raw("expr + ?", param)` or `WindowOrdering.desc(expression)` when partitions/orders come from computed expressions; alias window projections when turning a windowed subquery into a derived table/CTE.
