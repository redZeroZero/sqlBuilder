# Dialects & SQL Functions

The SQL that `sqlBuilder` emits is dialect-aware. A `Dialect` implementation controls:

- Identifier quoting (`"EMPLOYEE"` for Oracle).
- Pagination syntax (e.g., `OFFSET ? ROWS FETCH NEXT ? ROWS ONLY`).
- Set-operator keywords (Oracle maps `EXCEPT` to `MINUS`).
- LIKE escaping (dialect picks the escape char).
- Function rendering (logical function names → dialect-specific expressions).

## Using a Different Dialect

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

Implement `Dialect` (see `core/.../OracleDialect` or `PostgresDialect`) to customize quoting, pagination, set operators, and function rendering. Internally the DSL propagates the active dialect via an internal context; application code should stick to `SqlQuery.newQuery(myDialect)` or schema defaults instead of touching internals.

## Registering Functions

Aggregates and helpers call `Dialect.renderFunction(logicalName, args)`. Provide mappings for logical names you care about (e.g., `lower`, `upper`, `coalesce`):

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

Once a dialect knows about a logical function name, the fluent API can expose helpers without sprinkling vendor SQL throughout your code.
