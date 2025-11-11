# sqlBuilder Spring JDBC Integration Specification

Version: 1.0  
Status: Draft (implementation-ready)  
Module: `sqlbuilder-spring-jdbc`  
Audience: Backend developers using Spring + JDBC + sqlBuilder

---

## 1. Goals

Provide a **first-class, zero-friction integration** between `sqlBuilder` and Spring's JDBC stack so that developers can:

- Write queries with `SqlQuery` (or `CompiledQuery`) and execute them directly with `JdbcTemplate` / `NamedParameterJdbcTemplate`.
- Avoid manual plumbing from `CompiledQuery` / `SqlAndParams` to `Object[]` / `PreparedStatement`.
- Keep behavior explicit and predictable: **no ORM**, no hidden transactions, no magic session.

Key principles:

1. **Thin and transparent**: the integration is a light wrapper over Spring JDBC.
2. **Typesafe parameters**: reuse `SqlParameter` and `CompiledQuery` semantics.
3. **Composability**: works with existing RowMappers, ResultSetExtractors, etc.
4. **Optional**: core `sqlBuilder` has no Spring dependency; integration lives in a separate module.

---

## 2. Module Layout

Create a dedicated module:

- Artifact: `org.in.media.res:sqlbuilder-spring-jdbc`
- Dependencies:
  - `sqlbuilder-core` (or `sqlbuilder-api` + `sqlbuilder-core`)
  - `spring-jdbc`

No transitive Spring Boot auto-config by default (can be considered later).

---

## 3. Core Abstractions

### 3.1 `SqlBuilderJdbcTemplate`

A minimal wrapper that bridges:

- `SqlQuery` / `CompiledQuery` / `SqlAndParams`
- to
- `JdbcTemplate` / `NamedParameterJdbcTemplate`

**Package:**

```text
org.in.media.res.sqlBuilder.spring.jdbc
```

**Construction:**

```java
public class SqlBuilderJdbcTemplate {

    private final JdbcTemplate jdbcTemplate;

    public SqlBuilderJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate);
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
}
```

Optionally provide an overload with `NamedParameterJdbcTemplate` in a future iteration.

---

## 4. Execution API

The integration should focus on a concise, unsurprising set of methods.

### 4.1 Query (list)

```java
public <T> List<T> query(CompiledQuery compiledQuery, RowMapper<T> rowMapper);

public <T> List<T> query(SqlAndParams sqlAndParams, RowMapper<T> rowMapper);
```

**Behavior:**

- For `CompiledQuery`:
  - Expect it to be bound by the caller or support binding-on-call (see §5).
- For `SqlAndParams`:
  - Directly call `jdbcTemplate.query(sqlAndParams.sql(), sqlAndParams.params(), rowMapper);`

**Notes:**

- Uses Spring's standard `RowMapper<T>`.
- No caching or side effects.

---

### 4.2 Query (single object)

```java
public <T> T queryForObject(CompiledQuery compiledQuery, RowMapper<T> rowMapper);

public <T> T queryForObject(SqlAndParams sqlAndParams, RowMapper<T> rowMapper);
```

**Behavior:**

- Delegate to `jdbcTemplate.query(...)` and enforce:
  - exactly one row,
  - throw standard Spring exceptions (e.g. `IncorrectResultSizeDataAccessException`), consistent with `queryForObject`.

---

### 4.3 Update / Delete / Insert (DML)

```java
public int update(CompiledQuery compiledQuery);

public int update(SqlAndParams sqlAndParams);
```

**Behavior:**

- Use `jdbcTemplate.update(sql, params...)`.
- Return affected row count.

These methods are suitable for:
- `INSERT`, `UPDATE`, `DELETE`, `MERGE`, etc.

---

### 4.4 Convenience Overloads (Optional but recommended)

To reduce boilerplate for typical flows, add:

```java
public <T> List<T> query(Function<SqlQuery, QueryStage> builder, RowMapper<T> rowMapper);

public int update(Function<SqlQuery, QueryStage> builder);
```

**Example:**

```java
List<Employee> employees = sqlBuilderJdbcTemplate.query(
    q -> q
        .selecting(Employee.C_ID, Employee.C_FIRST_NAME)
        .fromTable(Employee.TABLE)
        .where(Employee.C_ACTIVE).isTrue(),
    (rs, rowNum) -> new Employee(
        rs.getInt("ID"),
        rs.getString("FIRST_NAME"))
);
```

**Contract:**

- The lambda:
  - receives a fresh `SqlQuery.newQuery()` instance,
  - returns a terminal `QueryStage` / `Query` ready to be compiled.
- Implementation:
  - build → `compile()` → `bind()` (if needed) → execute.

This is sugar; it must remain thin and readable.

---

## 5. Binding Semantics

### 5.1 Binding Responsibility

To keep things explicit and predictable:

- `CompiledQuery` + `SqlParameter` retain their responsibility for naming and ordering.
- `SqlAndParams` is the execution-ready representation used by `SqlBuilderJdbcTemplate`.

Two valid patterns:

#### Pattern A: Caller binds explicitly

```java
CompiledQuery cq = SqlQuery
    .newQuery()
    .selecting(Employee.C_ID)
    .fromTable(Employee.TABLE)
    .where(Employee.C_LAST_NAME).eq(lastNameParam)
    .compile();

SqlAndParams sap = cq.bind(Map.of("lastName", "Smith"));

List<Employee> result = sqlBuilderJdbcTemplate.query(sap, employeeRowMapper);
```

This pattern is **always supported**.

#### Pattern B: Helper for simple bindings (optional)

Provide a simple method:

```java
public <T> List<T> query(CompiledQuery compiledQuery,
                         Map<String, ?> paramValues,
                         RowMapper<T> rowMapper);

public int update(CompiledQuery compiledQuery,
                  Map<String, ?> paramValues);
```

Internally:

- `SqlAndParams sap = compiledQuery.bind(paramValues);`
- delegate to existing `query/update` methods.

This keeps binding explicit without exposing internals.

---

## 6. Error Handling

Behavior must align with `JdbcTemplate` expectations:

- Propagate:
  - `DataAccessException` and its subclasses,
  - `IncorrectResultSizeDataAccessException` where applicable.
- Do **not** swallow or wrap exceptions into custom unchecked types by default.
- Any sqlBuilder-specific issues (e.g. missing parameter binding) should throw:
  - `IllegalStateException` or a dedicated `SqlBuilderException` **before** calling JDBC.

---

## 7. Transaction Semantics

The integration:

- does **not** manage transactions directly.
- relies entirely on Spring’s existing transaction management (`@Transactional`, `PlatformTransactionManager`, etc.).
- All methods are safe to call in or out of a transactional context.

No additional configuration is required beyond wiring the `JdbcTemplate`.

---

## 8. Configuration & Usage

### 8.1 Typical Spring Configuration

```java
@Configuration
public class SqlBuilderConfig {

    @Bean
    public SqlBuilderJdbcTemplate sqlBuilderJdbcTemplate(JdbcTemplate jdbcTemplate) {
        return new SqlBuilderJdbcTemplate(jdbcTemplate);
    }
}
```

### 8.2 Example Usage

```java
@Service
public class EmployeeRepository {

    private final SqlBuilderJdbcTemplate sql;

    public EmployeeRepository(SqlBuilderJdbcTemplate sql) {
        this.sql = sql;
    }

    public List<Employee> findActiveByDepartment(int departmentId) {
        SqlParameter<Integer> depId = SqlParameters.param("depId", Integer.class);

        CompiledQuery cq = SqlQuery
            .newQuery()
            .selecting(Employee.C_ID, Employee.C_FIRST_NAME, Employee.C_LAST_NAME)
            .fromTable(Employee.TABLE)
            .where(Employee.C_DEPARTMENT_ID).eq(depId)
            .and(Employee.C_ACTIVE).isTrue()
            .compile();

        SqlAndParams sap = cq.bind(Map.of("depId", departmentId));

        return sql.query(sap, (rs, rowNum) -> new Employee(
            rs.getInt("ID"),
            rs.getString("FIRST_NAME"),
            rs.getString("LAST_NAME")
        ));
    }
}
```

This shows the full story:
- sqlBuilder for structure,
- compile + bind,
- Spring for execution & mapping.

---

## 9. Non-Goals (for v1)

The following are intentionally **out of scope** for this first integration:

- Auto-generation of `RowMapper` from `@SqlTable` / reflection.
- JPA-style entity management, sessions, or first-level cache.
- Automatic transaction management.
- Spring Boot auto-configuration (can be a separate small module later).
- Reactive database integration (R2DBC) — potential future module.

The v1 module must remain a **simple, predictable bridge**.

---

## 10. Definition of Done (#2)

The Spring JDBC integration is considered done when:

1. Module `sqlbuilder-spring-jdbc` exists and builds.
2. `SqlBuilderJdbcTemplate` (or equivalent) provides:
   - `query(...)` for lists,
   - `queryForObject(...)` for single results,
   - `update(...)` for DML,
   - overloads for `CompiledQuery` and `SqlAndParams`.
3. There are example usages:
   - in a dedicated `examples-spring` package/module,
   - using only the public `api.*` surface.
4. Behavior is covered by tests:
   - unit tests (delegation & parameter mapping),
   - integration tests with an in-memory DB (e.g. H2/Postgres testcontainer).
5. No additional hidden magic is introduced:
   - all behavior is visible in the API and docs.

This provides a clean, production-friendly bridge between sqlBuilder and the common Spring + JDBC stack without locking you into any ORM semantics.
