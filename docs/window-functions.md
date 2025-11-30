# Window Functions

Fluent helpers live under `org.in.media.res.sqlBuilder.api.query.window`. Combine a function, partitioning, ordering, and optional frames; render with `select(windowFunction)`.

## Running totals per group

```java
import static org.in.media.res.sqlBuilder.api.query.window.WindowFunctions.*;
import org.in.media.res.sqlBuilder.api.query.window.WindowFrame;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.constants.SortDirection;

var runningTotal = sum(OrdersTable.C_TOTAL)
        .partitionBy(OrdersTable.C_CUSTOMER_ID)
        .orderBy(OrdersTable.C_CREATED_AT, SortDirection.ASC)
        .rowsBetween(WindowFrame.Bound.unboundedPreceding(), WindowFrame.Bound.currentRow())
        .as("running_total");

var query = SqlQuery.query()
        .select(CustomersTable.C_FIRST_NAME, CustomersTable.C_LAST_NAME)
        .select(runningTotal)
        .from(IntegrationSchema.orders())
        .join(IntegrationSchema.customers()).on(OrdersTable.C_CUSTOMER_ID, CustomersTable.C_ID);
```

## Top-N per partition

```java
import static org.in.media.res.sqlBuilder.api.query.window.WindowFunctions.*;

var ranked = rowNumber()
        .partitionBy(EmployeesTable.C_DEPARTMENT_ID)
        .orderBy(EmployeesTable.C_SALARY, SortDirection.DESC)
        .as("rn");

var subquery = SqlQuery.query()
        .select(EmployeesTable.C_FIRST_NAME, EmployeesTable.C_LAST_NAME, EmployeesTable.C_SALARY)
        .select(ranked)
        .from(IntegrationSchema.employees())
        .asQuery();

var topEarners = SqlQuery.query()
        .select(subquery.as("ranked", "first", "last", "salary", "rn").get("first"))
        .select(subquery.as("ranked", "first", "last", "salary", "rn").get("last"))
        .select(subquery.as("ranked", "first", "last", "salary", "rn").get("salary"))
        .from(subquery, "ranked", "first", "last", "salary", "rn")
        .whereRaw("\"ranked\".\"rn\" <= 3");
```

## Lag/lead comparisons

```java
import static org.in.media.res.sqlBuilder.api.query.window.WindowFunctions.*;

var salaryDelta = WindowFunctions.lag(EmployeesTable.C_SALARY, 1)
        .orderBy(EmployeesTable.C_HIRED_AT, SortDirection.ASC)
        .as("prev_salary");

var query = SqlQuery.query()
        .select(EmployeesTable.C_FIRST_NAME, EmployeesTable.C_LAST_NAME, EmployeesTable.C_SALARY)
        .select(salaryDelta)
        .from(IntegrationSchema.employees());
```

## Sliding averages

```java
import static org.in.media.res.sqlBuilder.api.query.window.WindowFunctions.*;
import org.in.media.res.sqlBuilder.api.query.window.WindowFrame;

var movingAvg = avg(MetricsTable.C_VALUE)
        .orderBy(MetricsTable.C_TIMESTAMP)
        .rowsBetween(WindowFrame.Bound.preceding(2), WindowFrame.Bound.currentRow())
        .as("avg_last_3");

var query = SqlQuery.query()
        .select(MetricsTable.C_TIMESTAMP, MetricsTable.C_VALUE)
        .select(movingAvg)
        .from(MetricsTable.TABLE);
```

### Tips

- `WindowExpression.raw("expr + ?", param)` lets you partition/order by computed expressions while still collecting parameters.
- `WindowFunctions.countStar()` renders `COUNT(*) OVER(...)`.
- Alias window projections (`.as("alias")`) when projecting from derived tables or CTEs to avoid ambiguous column names.***
