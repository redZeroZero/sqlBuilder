# sqlBuilder Raw SQL Support Specification

Version: 1.0  
Status: Draft (implementation-ready)  
Audience: Library authors and users of `sqlBuilder`

---

## 1. Purpose

The goal of this feature is to provide a **simple, explicit, and safe escape hatch** to inject raw SQL fragments into queries built with `sqlBuilder`, without:

- bloating the DSL,
- hiding behavior behind magic,
- or compromising the existing `CompiledQuery` / parameter binding model.

Raw SQL must:

- feel natural to application developers,
- integrate seamlessly with the staged DSL,
- remain **truly raw**: inserted as-is into the final SQL string.

---

## 2. Design Principles

1. **Raw means raw**  
   The framework does not parse, transform, escape, or validate raw fragments.

2. **Obvious API**  
   Raw methods mirror existing DSL methods:
   - `select(...)` → `selectRaw(...)`
   - `where(...)` → `whereRaw(...)`
   - `orderBy(...)` → `orderByRaw(...)`
   - etc.

3. **Safe-by-habit**  
   Support `?` placeholders + `SqlParameter<?>` for values to avoid string concatenation of user input.

4. **Non-intrusive**  
   Regular usage does not require understanding additional types. `String` + optional `SqlParameter` is enough.

---

## 3. Public API

All methods below are **part of the public API** once introduced. They must be exposed through the staged interfaces (`SelectStage`, `PredicateStage`, `QueryStage`, `FromStage`, `WithBuilder`) and accessible via the `SqlQuery` façade.

### 3.1 SELECT

**Interface:** `SelectStage`

```java
SelectStage selectRaw(String sql);
SelectStage selectRaw(String sql, SqlParameter<?>... params);
SelectStage selectRaw(RawSqlFragment fragment);
```

**Behavior:**

- Appends `sql` directly to the `SELECT` list.
- If `params` are provided, they are bound positionally to `?` placeholders in `sql`.

**Example:**

```java
SqlQuery
    .newQuery()
    .selecting(Employee.C_ID)
    .selectRaw("JSON_VALUE(E.DATA, '$.customer.name') AS CUSTOMER_NAME")
    .fromTable(Employee.TABLE);
```

---

### 3.2 WHERE / AND / OR

**Interface:** `PredicateStage`

```java
PredicateStage whereRaw(String sql);
PredicateStage whereRaw(String sql, SqlParameter<?>... params);
PredicateStage whereRaw(RawSqlFragment fragment);

PredicateStage andRaw(String sql);
PredicateStage andRaw(String sql, SqlParameter<?>... params);
PredicateStage andRaw(RawSqlFragment fragment);

PredicateStage orRaw(String sql);
PredicateStage orRaw(String sql, SqlParameter<?>... params);
PredicateStage orRaw(RawSqlFragment fragment);
```

**Behavior:**

- Inserts the raw fragment as a boolean expression in the corresponding logical position.
- The framework does not wrap or rewrite the expression beyond what is necessary to keep overall clause structure valid.

**Example:**

```java
SqlParameter<String> status = SqlParameters.param("status", String.class);

SqlQuery
    .newQuery()
    .selecting(Employee.C_ID)
    .fromTable(Employee.TABLE)
    .whereRaw("E.METADATA_JSON ->> 'status' = ?", status)
    .andRaw("E.DELETED_AT IS NULL");
```

---

### 3.3 ORDER BY

**Interface:** `QueryStage` / `Query`

```java
QueryStage orderByRaw(String sql);
QueryStage orderByRaw(String sql, SqlParameter<?>... params);
QueryStage orderByRaw(RawSqlFragment fragment);
```

**Example:**

```java
query.orderByRaw("LOWER(E.LAST_NAME), E.ID");
```

---

### 3.4 GROUP BY

**Interface:** `QueryStage` / `Query`

```java
QueryStage groupByRaw(String sql);
QueryStage groupByRaw(String sql, SqlParameter<?>... params);
QueryStage groupByRaw(RawSqlFragment fragment);
```

**Example:**

```java
query.groupByRaw("E.DEPARTMENT_ID, ROLLUP(E.COUNTRY_CODE)");
```

---

### 3.5 FROM / JOIN

**Interfaces:** `FromStage`, `Query`

```java
FromStage fromRaw(String sql);
FromStage fromRaw(String sql, SqlParameter<?>... params);
FromStage fromRaw(RawSqlFragment fragment);

Query joinRaw(String sql);
Query joinRaw(String sql, SqlParameter<?>... params);
Query joinRaw(RawSqlFragment fragment);

Query leftJoinRaw(String sql);
Query leftJoinRaw(String sql, SqlParameter<?>... params);
Query leftJoinRaw(RawSqlFragment fragment);
// Optionally: rightJoinRaw, crossJoinRaw, etc.
```

**Behavior:**

- `fromRaw`: sets the FROM source to the given raw fragment.
- `joinRaw` variants: append `JOIN <sql>` at the appropriate position.
- The caller is responsible for providing any necessary `ON` clause within the raw fragment when applicable.

**Example:**

```java
SqlQuery
    .newQuery()
    .selectRaw("t.id")
    .selectRaw("t.value")
    .fromRaw("JSON_TABLE(E.DATA, '$.items[*]' COLUMNS (id NUMBER, value VARCHAR2(100))) t");
```

---

### 3.6 CTE (WITH)

**Interface:** `WithBuilder`

```java
WithBuilder withRaw(String name, String sql);
WithBuilder withRaw(String name, String sql, SqlParameter<?>... params);
WithBuilder withRaw(String name, RawSqlFragment fragment);
```

**Behavior:**

- Generates: `WITH name AS ( <sql> )`
- Raw body is inserted as-is.

**Example:**

```java
SqlQuery
    .with()
    .withRaw("recent_emp",
        "SELECT * FROM EMPLOYEE WHERE HIRED_AT > CURRENT_DATE - 30")
    .selectingFrom("recent_emp")
    .asQuery();
```

---

## 4. `RawSqlFragment` Abstraction

`RawSqlFragment` is a lightweight representation of a raw fragment plus its parameters.
It is intended for:

- internal usage in the implementation,
- advanced users or framework integrations.

**Interface:**

```java
package org.in.media.res.sqlBuilder.api.query;

public interface RawSqlFragment {

    /**
     * Returns the raw SQL snippet as-is.
     * This text is injected directly into the generated statement.
     */
    String sql();

    /**
     * Returns parameters associated with '?' placeholders in {@link #sql()}.
     * They are consumed in order and merged into the compiled query parameter list.
     */
    java.util.List<SqlParameter<?>> parameters();
}
```

**Factory:**

```java
package org.in.media.res.sqlBuilder.api.query;

public final class RawSql {

    private RawSql() {}

    public static RawSqlFragment of(String sql) {
        return of(sql, List.of());
    }

    public static RawSqlFragment of(String sql, SqlParameter<?>... params) {
        return of(sql, java.util.Arrays.asList(params));
    }

    public static RawSqlFragment of(String sql, java.util.List<SqlParameter<?>> params) {
        return new SimpleRawSqlFragment(sql, params); // internal implementation
    }
}
```

All `*Raw(String, SqlParameter<?>...)` overloads in the DSL must internally delegate to `RawSql.of(...)`.

---

## 5. Compilation & Binding Semantics

Integration with the existing `CompiledQuery` / `SqlAndParams` model MUST follow these rules:

1. **Emission Order**

   - Raw fragments are written into the SQL buffer at the exact place where the corresponding method was invoked (within their clause).
   - No modification of the `sql` string occurs.

2. **Parameter Binding**

   - Each `RawSqlFragment` contributes its `parameters()` to the global placeholder list in the order fragments are processed.
   - Parameters are matched positionally to `?` inside the raw SQL text.
   - Binding continues to work via existing mechanisms:

     ```java
     CompiledQuery cq = query.compile();
     SqlAndParams bound = cq.bind(Map.of("paramName", value));
     ```

3. **No Validation**

   - The framework does not validate SQL syntax inside raw fragments.
   - Any syntax error is detected by the database at execution time.

4. **Dialect Independence**

   - Raw fragments are not normalized or adapted per dialect.
   - It is the caller's responsibility to ensure that the raw fragment is compatible with the active dialect.

---

## 6. Usage Guidelines (for Documentation)

To be included in public docs / Javadoc:

1. Raw methods (`*Raw`) are **escape hatches**:
   - Use them for vendor-specific expressions, JSON operators, full-text search, hints, and complex constructs not modeled by the DSL.

2. Prefer parameter binding:

   Recommended:
   ```java
   whereRaw("E.SALARY > ?", minSalaryParam);
   ```

   Not recommended:
   ```java
   whereRaw("E.SALARY > " + userInput);
   ```

3. Raw fragments are inserted **as-is**:
   - No quoting of identifiers,
   - No escaping of literals,
   - No automatic parentheses.

4. The typed DSL is the default; raw is for edge cases.

---

## 7. Testing Requirements

1. **Unit Tests**
   - `selectRaw` appends exact SQL.
   - `whereRaw` / `andRaw` / `orRaw` correctly integrate into WHERE.
   - `orderByRaw` / `groupByRaw` produce valid clause strings.
   - `fromRaw` / `joinRaw` appear exactly as passed.
   - Parameters from raw fragments are present in `CompiledQuery` in correct order.

2. **Integration Tests**
   - At least one dialect (e.g., Postgres, Oracle) executing:
     - Raw JSON functions,
     - Raw vendor-specific expressions,
   - Verifying:
     - Correct SQL generation,
     - Successful binding,
     - Runtime execution by the database.

---

## 8. Summary

This feature:

- keeps the core DSL clean and type-safe,
- exposes a minimal, intuitive set of `*Raw` methods,
- guarantees that raw SQL is truly raw,
- reuses `SqlParameter` and `CompiledQuery`,
- and gives developers a natural, zero-surprise way to express complex, dialect-specific SQL without fighting the framework.
