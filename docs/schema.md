# Schema Modeling

## Annotated tables + scanning

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

## Manual table registration

```java
ColumnRef<Integer> EMP_ID = ColumnRef.of("ID", Integer.class);
Table employee = Tables.builder("Employee", "E")
    .column(EMP_ID)
    .column("ACTIVE", "isActive")
    .build();
```

`Tables.builder(...).build()` binds descriptors and returns immutable tables; rebuild to change columns.

## Typed rows

```java
var facet = schema.facets().facet(Customer.class);
CustomerColumns columns = schema.facets().columns(Customer.class, CustomerColumns.class);
TableRow row = facet.rowBuilder()
    .set(columns.ID(), 42L)
    .set(columns.FIRST_NAME(), "Ada")
    .build();
```

## Defining Tables & Schemas

Annotate POJOs or register tables programmatically; `SchemaScanner` can auto-detect classes or you can mix with manual `Tables.builder(...)`. `QueryColumns` keeps table/column bundles together.

### ColumnRef lifecycle & schema immutability

- Declare descriptors (`ColumnRef<T>` fields or `@SqlColumn(javaType = ...)` metadata).
- Bind via `SchemaScanner` or `Tables.builder(...)` before using in queries.
- Treat schema objects as immutable once wired; rebuild to change columns/tables.

### Typed Rows & Builders

Use `TableFacets` to construct strongly-typed row objects for fixtures or binding.

### Manual Table Registration

If annotation scanning is not an option, declare tables programmatically via `Tables.builder(...)`. Bind `ColumnRef` descriptors before use.
