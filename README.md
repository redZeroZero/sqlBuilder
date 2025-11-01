# sqlBuilder

sqlBuilder is a lightweight fluent DSL for assembling SQL statements in Java. It provides composable builders for `SELECT`, `FROM`, `WHERE`, `GROUP BY`, `HAVING`, `ORDER BY`, and `LIMIT / OFFSET` clauses so you can express queries without string concatenation.

## Getting Started

```java
EmployeeSchema schema = new EmployeeSchema();
ITable employee = schema.getTableBy(Employee.class);
ITable job = schema.getTableBy(Job.class);

String sql = new Query()
    .select(Employee.C_FIRST_NAME)
    .select(Employee.C_LAST_NAME)
    .innerJoin(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
    .where(Employee.C_FIRST_NAME).eq("Alice")
    .orderBy(Employee.C_LAST_NAME)
    .limitAndOffset(20, 0)
    .transpile();

// SELECT ... ORDER BY ... FETCH NEXT 20 ROWS ONLY
```

## Sample Queries to Try

The snippets below illustrate common patterns you can run in a REPL or unit test to verify the builder.

### 1. Simple Projection

```java
new Query()
    .select(employee) // or rely on descriptor shortcuts
    .transpile();
```

Expected SQL:

```
SELECT Employee.ID, Employee.FIRST_NAME, ...
 FROM Employee
```

### 2. Joins with Filters

```java
new Query()
    .select(Employee.C_FIRST_NAME, Job.C_DESCRIPTION)
    .leftJoin(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
    .where(Job.C_SALARY).supOrEqTo(50000)
    .transpile();
```

### 3. Aggregations with GROUP BY / HAVING

```java
new Query()
    .select(Employee.C_FIRST_NAME)
    .select(AggregateOperator.AVG, Job.C_SALARY)
    .join(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
    .groupBy(Employee.C_FIRST_NAME)
    .having(Job.C_SALARY).avg(Job.C_SALARY).supTo(60000)
    .orderBy(Employee.C_FIRST_NAME)
    .transpile();
```

### 4. Pagination (Oracle-style)

```java
new Query()
    .select(Job.C_DESCRIPTION)
    .from(job)
    .orderBy(Job.C_SALARY, SortDirection.DESC)
    .limitAndOffset(10, 20)
    .transpile();
```

## Notes

- The builder creates SQL strings; execution is left to your JDBC or ORM layer.
- Transpilers are pluggable. The default implementations target Oracle syntax (OFFSET/FETCH). Extend the transpiler factories to add other dialects.
- Use the fluent HAVING builder to chain aggregate comparisons (`having(col).sum(col).supTo(100)` etc.).

## Running Tests

```
mvn -o test
```

This executes the regression suite in `src/test/java` that covers the examples above.
