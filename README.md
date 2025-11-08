# sqlBuilder

sqlBuilder is a lightweight fluent DSL for assembling SQL statements in Java. It provides composable builders for `SELECT`, `FROM`, `WHERE`, `GROUP BY`, `HAVING`, `ORDER BY`, and `LIMIT / OFFSET` clauses so you can express queries without string concatenation.

## Getting Started

### Installation

1. Clone this repository (or add it as a Git submodule) and run `mvn -q -DskipTests package` from the repo root to build both modules and install the core jar into your local Maven cache.
2. Add the dependency to your application:

   **Maven**

   ```xml
   <dependency>
     <groupId>org.in.media.res</groupId>
     <artifactId>sqlBuilder-core</artifactId>
     <version>0.0.1-SNAPSHOT</version>
   </dependency>
   ```

   **Gradle (Kotlin DSL)**

   ```kotlin
   implementation("org.in.media.res:sqlBuilder-core:0.0.1-SNAPSHOT")
   ```

   Adjust the version to match the coordinate published in your artifact repository (the examples assume a local install).

3. Import the DSL types you plan to use, e.g. `org.in.media.res.sqlBuilder.api.query.SqlQuery` for fluent query construction and the generated table descriptors from your schema package.

### Repository layout

- `core/` — the distributable DSL, factories, validators, and annotation processor (compiles with `-proc:none` but packages the processor for downstream use).
- `examples/` — the sample schema, `MainApp`, benchmarks, and integration-style tests. This module depends on `sqlBuilder-core`, runs the annotation processor to generate column facades, and mirrors the usage documented below. Run `mvn -pl examples -q test` when you only want to exercise the sample project.

Once the dependency is available, you can start composing queries immediately:

```java
EmployeeSchema schema = new EmployeeSchema();
Table employee = schema.getTableBy(Employee.class);
Table job = schema.getTableBy(Job.class);

SqlAndParams sp = SqlQuery.newQuery()
    .select(Employee.C_FIRST_NAME)
    .select(Employee.C_LAST_NAME)
    .innerJoin(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
    .where(Employee.C_FIRST_NAME).eq("Alice")
    .orderBy(Employee.C_LAST_NAME)
    .limitAndOffset(20, 0)
    .render();

sp.sql();    // SELECT ... WHERE E.FIRST_NAME = ? ORDER BY E.LAST_NAME ASC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
sp.params(); // ["Alice", 0, 20]

// Need only the placeholder SQL? call transpile(), which now delegates to render().sql()
String sql = SqlQuery.newQuery()
    .select(Employee.C_FIRST_NAME)
    .select(Employee.C_LAST_NAME)
    .innerJoin(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
    .where(Employee.C_FIRST_NAME).eq("Alice")
    .orderBy(Employee.C_LAST_NAME)
    .limitAndOffset(20, 0)
    .transpile();
```

## Sample Queries to Try

The snippets below illustrate common patterns you can run in a REPL or unit test to verify the builder. Unless noted otherwise, `.transpile()` returns SQL with `?` placeholders—call `.render()` when you need both SQL and the bound parameter values. Identifiers are quoted according to the active dialect (e.g., Oracle emits `"Employee"`); examples keep the unquoted form for readability.

### Rendering SQL & parameters

`render()` is the primary way to execute a query: it returns a `SqlAndParams` pair containing the SQL text (with `?` placeholders) and the ordered parameter list. `transpile()` is still available when you just need the SQL string, but it now emits placeholders instead of literal values.

```java
SqlAndParams selectByName = SqlQuery.newQuery()
    .select(Employee.C_ID)
    .where(Employee.C_FIRST_NAME).eq("Alice")
    .render();

selectByName.sql();    // SELECT E.ID FROM Employee E WHERE E.FIRST_NAME = ?
selectByName.params(); // ["Alice"]
```

### Compiled queries & named parameters

Use `SqlParameter` when you want to build a template once and bind values later.

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

### 1. Simple Projection

```java
SqlQuery.newQuery()
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
String sql = SqlQuery.countAll().transpile();             // SELECT COUNT(*)

Query printable = SqlQuery.newQuery()
    .select(Employee.C_FIRST_NAME)
    .from(employee)
    .asQuery();
printable.where(Employee.C_FIRST_NAME).eq("Alice");

System.out.println(printable.prettyPrint());
/*
SELECT Employee.FIRST_NAME as firstName
FROM Employee
WHERE Employee.FIRST_NAME = ?
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
    .transpile();
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
    .transpile();
```

Call `SqlQuery.toTable(query)` to auto-generate aliases (or supply your own as above). Each column alias you provide or that is inferred is available via `salaryAvg.get("ALIAS")`, so subsequent clauses can reference the derived table just like any other.

### 8. Filtering with Subqueries

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
    .transpile();
```

Scalar comparisons, `IN` / `NOT IN`, and `EXISTS` / `NOT EXISTS` all accept subqueries. `exists(subquery)` can be called directly on the query builder, and the DSL will emit `WHERE EXISTS (...)` without requiring a placeholder column.

### 9. Grouped Filters (Nested AND / OR Trees)

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
    .transpile();
```

Call `QueryHelper.group()` without arguments when you want an inline builder that can be passed straight into `.where(...)`, `.and(...)`, or `.having(...)`. Chain `.andGroup()` / `.orGroup()` whenever you need nested parentheses, then finish the nested block with `.endGroup()`—no lambdas required. (The consumer overload remains available if you prefer that style.)

The same helper works for HAVING clauses: call `query.having(QueryHelper.group(...)).and(...)` to keep aggregates nested under a single `HAVING` block without hand-written parentheses.

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

Implement `Dialect` (see `core/.../OracleDialect`) to customize quoting, pagination, and function rendering. Because `QueryImpl` and all transpilers consult the dialect via `DialectContext`, the rest of the DSL automatically respects your rules.

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
QueryImpl.newQuery()
    .select(cols.ID(), cols.FIRST_NAME())
    .where(cols.LAST_NAME()).like("%son")
    .transpile();
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
    .transpile();
```

> The sample schema ships with the generated `CustomerColumns` / `CustomerColumnsImpl` pair checked into source control so the build remains stable even when annotation processing is disabled. In your own modules you can rely on the processor to emit the same code automatically.
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
    .transpile();
```

Fluent helpers now accept typed descriptors directly, so you can skip intermediate `where(...)` calls when it reads better. For example:

```java
String sql = SqlQuery.newQuery()
    .select(Customer.C_ID)
    .from(customer)
    .like(Customer.C_LAST_NAME, "%son")
    .isNull(Customer.C_MAIL)
    .transpile();
```

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

If you prefer manual wiring, you can instantiate `TableImpl` and `ColumnImpl` directly—just ensure each column is linked to its owning table before you pass it to the fluent APIs.

## Running Tests

```
mvn -o test
```

This executes the regression suite in `src/test/java` that covers the examples above.
