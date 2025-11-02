# sqlBuilder

sqlBuilder is a lightweight fluent DSL for assembling SQL statements in Java. It provides composable builders for `SELECT`, `FROM`, `WHERE`, `GROUP BY`, `HAVING`, `ORDER BY`, and `LIMIT / OFFSET` clauses so you can express queries without string concatenation.

## Getting Started

```java
EmployeeSchema schema = new EmployeeSchema();
Table employee = schema.getTableBy(Employee.class);
Table job = schema.getTableBy(Job.class);

String sql = QueryImpl.newQuery()
    .select(Employee.C_FIRST_NAME)
    .select(Employee.C_LAST_NAME)
    .innerJoin(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
    .where(Employee.C_FIRST_NAME).eq("Alice")
    .orderBy(Employee.C_LAST_NAME)
    .limitAndOffset(20, 0)
    .transpile(); // SELECT ... ORDER BY ... FETCH NEXT 20 ROWS ONLY
```

## Sample Queries to Try

The snippets below illustrate common patterns you can run in a REPL or unit test to verify the builder.

### 1. Simple Projection

```java
QueryImpl.newQuery()
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
String sql = Query.newQuery()
    .select(Employee.C_FIRST_NAME, Job.C_DESCRIPTION)
    .leftJoin(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
    .where(Job.C_SALARY).supOrEqTo(50000)
    .transpile();
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
    .transpile();
```

### 4. Pagination (Oracle-style)

```java
String sql = Query.newQuery()
    .select(Job.C_DESCRIPTION)
    .from(job)
    .orderBy(Job.C_SALARY, SortDirection.DESC)
    .limitAndOffset(10, 20)
    .transpile();
```

### 5. Quick Count / Pretty Print

```java
String sql = QueryImpl.countAll().transpile();             // SELECT COUNT(*)

Query printable = Query.newQuery()
    .select(Employee.C_FIRST_NAME)
    .from(employee);
printable.where(Employee.C_FIRST_NAME).eq("Alice");

System.out.println(printable.prettyPrint());
/*
SELECT Employee.FIRST_NAME as firstName
FROM Employee
WHERE Employee.FIRST_NAME = 'Alice'
*/
```

## Notes

- The builder creates SQL strings; execution is left to your JDBC or ORM layer. Use `Query.prettyPrint()` when you need a clause-per-line view for debugging.
- Transpilers are pluggable. The default implementations target Oracle syntax (OFFSET/FETCH). Extend the transpiler factories to add other dialects.
- Use the fluent HAVING builder to chain aggregate comparisons (`having(col).sum(col).supTo(100)` etc.).
- `EmployeeSchema` auto-discovers tables in the `org.in.media.res.sqlBuilder.example` package. Pass a different base package to scan additional modules, or plug your own schema into `SchemaScanner.scan("com.acme.sales")`.

## Defining Tables & Schemas

Annotate plain Java classes to describe tables and their columns. `SchemaScanner` will discover them automatically and wire column descriptors back to the DSL.

```java
@SqlTable(name = "Customer", alias = "C")
public final class Customer {

    @SqlColumn(name = "ID")
    public static ColumnRef ID;

    @SqlColumn(name = "FIRST_NAME", alias = "firstName")
    public static ColumnRef FIRST_NAME;

    @SqlColumn(name = "LAST_NAME", alias = "lastName")
    public static ColumnRef LAST_NAME;

    private Customer() {} // prevent instantiation
}
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

## Running Tests

```
mvn -o test
```

This executes the regression suite in `src/test/java` that covers the examples above.
