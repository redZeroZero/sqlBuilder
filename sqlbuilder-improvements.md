# sqlBuilder – Improvement Proposals

Version: 0.1 (draft)  
Scope: only improvement / refactoring proposals, not the full specification of current behavior.

This document lists the proposed improvements for sqlBuilder, structured by theme.  
The goal is to act as a **refactoring roadmap** rather than end-user documentation.

---

## 1. API Entry Point & Staging

### 1.1 New entry point: `SqlQuery.query()`

**Proposal**

Add a simple static method:

```java
public final class SqlQuery {

    public static Query query() {
        return (Query) QueryImpl.newQuery();
    }

    // newQuery() can remain for advanced/staged usage
}
```

**Goal**

- Simplify the most common use case:  
  Instead of:

  ```java
  Query q = SqlQuery.newQuery().asQuery();
  ```

  target:

  ```java
  Query q = SqlQuery.query()
      .select(...)
      .from(...)
      .where(...);
  ```

- Keep the `SelectStage` / `FromStage` interfaces for advanced use (strict staging, internal extensions), but provide direct access to `Query` for 90% of cases.

---

## 2. Public vs Internal Surface

### 2.1 Clarify the public API boundary

**Proposal**

- **Main public API** (stability promise):
  - `org.in.media.res.sqlBuilder.api.model`
  - `org.in.media.res.sqlBuilder.api.model.annotation`
  - `org.in.media.res.sqlBuilder.api.query`
  - `org.in.media.res.sqlBuilder.api.query.dialect`
  - `org.in.media.res.sqlBuilder.api.query.params`
  - `org.in.media.res.sqlBuilder.api.query.helper`
  - `org.in.media.res.sqlBuilder.api.format`
- **Extension / advanced API (more volatile)**:
  - `org.in.media.res.sqlBuilder.api.query.spi`
    - Clearly state in Javadoc: “for framework / integration use, may change between minor versions”.
- **Internal (no compatibility guarantees)**:
  - `org.in.media.res.sqlBuilder.core.*`
  - `org.in.media.res.sqlBuilder.processor.*`
  - `org.in.media.res.sqlBuilder.examples.*`

### 2.2 Mark SPI as “internal-ish”

**Proposal**

- In Javadocs of `api.query.spi.*`:
  - Explicitly document that these interfaces are for **advanced integrators** and may change between releases.
- Avoid using SPI types in public examples, so that users are not encouraged to depend on them.

**Goal**

- Give freedom to refactor `core.*` and the SPI without breaking standard application code.
- Clearly state where the library’s “stability promise” stops.

---

## 3. Dialects, LIMIT/OFFSET & Set Operations

### 3.1 Remove `LimitTranspiler` & centralize logic in `Dialect`

**Proposal**

- Remove the `LimitTranspiler` interface and any code that depends on it.
- Turn `LimitImpl` into a simple data holder:

  ```java
  final class LimitImpl implements Limit {
      private final Integer limit;
      private final Integer offset;
      // getters only
  }
  ```

- When building the query, let the **Dialect only** decide the SQL:

  ```java
  PaginationClause pagination = dialect.renderLimitOffset(
      limitImpl.getLimitAsLongOrNull(),
      limitImpl.getOffsetAsLongOrNull()
  );
  builder.append(pagination.sql());
  ```

**Goal**

- Single source of truth for dialect-specific pagination logic.
- Adding a new dialect only requires implementing `renderLimitOffset(...)`, not touching the query core.

---

### 3.2 Move EXCEPT/MINUS handling into Dialect

**Proposal**

- Introduce an enum for set operations:

  ```java
  public enum SetOperator {
      UNION,
      UNION_ALL,
      INTERSECT,
      INTERSECT_ALL,
      EXCEPT,
      EXCEPT_ALL
  }
  ```

- Add to `Dialect`:

  ```java
  String setOperator(SetOperator op);
  ```

- Implement e.g.:

  ```java
  // PostgreSQL
  public String setOperator(SetOperator op) {
      return switch (op) {
          case UNION         -> "UNION";
          case UNION_ALL     -> "UNION ALL";
          case INTERSECT     -> "INTERSECT";
          case INTERSECT_ALL -> "INTERSECT ALL";
          case EXCEPT        -> "EXCEPT";
          case EXCEPT_ALL    -> "EXCEPT ALL";
      };
  }

  // Oracle
  public String setOperator(SetOperator op) {
      return switch (op) {
          case UNION         -> "UNION";
          case UNION_ALL     -> "UNION ALL";
          case INTERSECT     -> "INTERSECT";
          case INTERSECT_ALL -> "INTERSECT ALL";
          case EXCEPT        -> "MINUS";
          case EXCEPT_ALL    -> throw new UnsupportedOperationException("EXCEPT ALL not supported");
      };
  }
  ```

- In `QueryImpl`, replace the logic with something like:

  ```java
  builder.append(dialect.setOperator(setOperatorEnum));
  ```

**Goal**

- Avoid hard-coded `if (oracle) MINUS else EXCEPT` in the query core.
- Handle limitations (`EXCEPT ALL` unsupported) entirely within the dialect implementation.

---

### 3.3 Clarify `DialectContext` usage (internal ThreadLocal)

**Proposal**

- Keep `DialectContext` as an internal utility (ThreadLocal) for transpilers.
- Document that:
  - `Query` instances are **not thread-safe** while `render()` / `compile()` is running.
  - Callers should use one query per thread during SQL generation.

**Goal**

- Prevent users from relying on `DialectContext` directly.
- Keep dialect handling encapsulated within the internal pipeline.

---

## 4. `ColumnRef`, Binding, and Schema Model

### 4.1 Make the `ColumnRef` lifecycle explicit

**Proposal**

- Document clearly:

  - `ColumnRef` is a **descriptor** (name + type) that only becomes fully usable **after binding**.
  - Binding happens:
    - via `SchemaScanner` + `ScannedSchema` (annotations `@SqlTable` / `@SqlColumn`), or
    - via `Tables.builder(...).column("NAME", type, columnRef)`.

- Forbid or strongly discourage using `ColumnRef.of(...)` in application code without going through a builder or scanner.

Possible follow-ups:

- Make the default `ColumnRef` constructor/factory package-private.
- Provide public “safe” factories via:
  - generated code (annotation processor), or
  - `Tables` (for programmatic schema).

**Goal**

- Reduce the risk of having never-bound `ColumnRef` that blow up at runtime (`IllegalStateException`).
- Encourage safe patterns: descriptors + scanning or table builders.

---

### 4.2 “Soft immutability” for `Schema` / `Table`

**Proposal**

- Treat `Schema` / `Table` as:

  - *Mutable during bootstrap* (building, scanning).
  - *Effectively immutable* afterwards.

- Concretely:
  - Limit setters in the public API.
  - Return unmodifiable collections from getters (`Collections.unmodifiableList` / `Map`).
  - Document that the schema must not be modified after initialization.

**Goal**

- Prepare for multi-threaded, long-lived environments (Spring, microservices).
- Prevent late modifications to a shared schema instance.

---

## 5. Spring JDBC: DX & Ergonomics

### 5.1 `SqlBuilderJdbcTemplate` overloads taking `Query` directly

**Proposal**

Right now, users often write:

```java
SqlAndParams sap = query.render();
jdbcTemplate.query(sap.sql(), sap.params(), rowMapper);
```

or:

```java
CompiledQuery cq = query.compile();
SqlAndParams sap = cq.bind(params);
jdbcTemplate.query(sap.sql(), sap.params(), rowMapper);
```

Provide helper overloads:

```java
// Without named params: simple render()
public <T> List<T> query(Query query, RowMapper<T> rowMapper) {
    SqlAndParams sap = query.render();
    return query(sap, rowMapper);
}

public int update(Query query) {
    SqlAndParams sap = query.render();
    return update(sap);
}

// With named params: compile() + bind(map)
public <T> List<T> query(Query query, Map<String, ?> params, RowMapper<T> rowMapper) {
    CompiledQuery cq = query.compile();
    SqlAndParams sap = cq.bind(params);
    return query(sap, rowMapper);
}

public int update(Query query, Map<String, ?> params) {
    CompiledQuery cq = query.compile();
    SqlAndParams sap = cq.bind(params);
    return update(sap);
}
```

And equivalents for `UpdateQuery`, `InsertQuery`, `DeleteQuery`.

**Goal**

- Reduce boilerplate in consumer code.
- Provide a “natural” usage path:
  - `query()` → `SqlBuilderJdbcTemplate.query(query, rowMapper)`  
  - `query()` + params → `SqlBuilderJdbcTemplate.query(query, params, rowMapper)`.

---

### 5.2 Standard logging pattern with `SqlFormatter`

**Proposal**

- Document and promote a standard logging pattern:

  ```java
  SqlAndParams sap = query.render();
  String debugSql = SqlFormatter.inlineLiterals(sap, dialect);
  log.debug("Executing SQL: {}", debugSql);

  List<Foo> results = sqlBuilderJdbcTemplate.query(sap, rowMapper);
  ```

- Make it explicit in docs:
  - `inlineLiterals` is **for debugging only**, not for generating SQL to execute.

**Goal**

- Make it easier to debug queries (especially when reporting bugs).
- Avoid temptations to combine `inlineLiterals` with manual execution and introduce SQL injection risks.

---

## 6. Naming & Internal Simplifications

### 6.1 Harmonize vocabulary: `render()` / `compile()` vs `transpile()`

**Proposal**

- Keep:

  - `render()` to produce a fully bound `SqlAndParams`.
  - `compile()` to produce a reusable `CompiledQuery`.

- Reserve `transpile()` for SPI / internal code (clauses, conditions, etc.).
- Avoid exposing `transpile()` paths in user-facing examples or documentation.

**Goal**

- Clarify the mental model:
  - Public API: you “render” or “compile” a query.
  - Internal: clauses “transpile” their AST into SQL.

---

### 6.2 Simplify `ValueType` / `ConditionValue` where possible

**Proposal**

- Currently, `ValueType` distinguishes multiple types (INT, DBL, STR, DATE, PARAM, SUBQUERY).
- In practice, transpilers mostly treat everything as `?` except subqueries.

Options:

- Either reduce `ValueType` to:

  ```java
  enum ValueType {
      LITERAL,
      PARAM,
      SUBQUERY
  }
  ```

- Or keep the existing granularity, but:
  - Use it only internally.
  - Avoid overcomplicating code that does not need per-type behavior.

**Goal**

- Slightly simplify the internal AST without affecting the public surface.

---

## 7. Examples & Tests as “Executable Spec”

### 7.1 Strengthen examples

**Proposal**

Ensure the `examples` module covers:

- Multiple and nested CTEs.
- Set operations:
  - UNION / UNION ALL
  - INTERSECT / INTERSECT ALL
  - EXCEPT / MINUS (depending on dialect)
- Pagination (LIMIT/OFFSET) for at least 2 dialects (Oracle/Postgres).
- Optional conditions with `SqlParameter` + `OptionalConditions`.

**Goal**

- Make examples double as readable specifications for developers.
- Provide a natural base for integration tests.

---

### 7.2 Targeted SQL assertion tests

**Proposal**

For each dialect-sensitive feature:

- Write tests that:

  ```java
  Query q = ...;
  String sql = q.render(dialectX).sql();
  assertEquals("SELECT ...", sql);
  ```

- Cases to cover:
  - CTEs.
  - Derived tables.
  - Unions / intersects / excepts.
  - Pagination.
  - Optional conditions.

**Goal**

- Protect against regressions when refactoring internals (especially in `core.*` / `spi`).

---

## 8. Roadmap Summary Based on These Improvements

### 8.1 Wave 1 – API & Dialect

1. Add `SqlQuery.query()` as primary entry point.
2. Document public API boundary (packages) and mark SPI as “advanced”.
3. Introduce `SetOperator` + `Dialect.setOperator(...)`.
4. Route all pagination SQL through `Dialect.renderLimitOffset(...)`.
5. Deprecate then remove `LimitTranspiler`.

### 8.2 Wave 2 – Spring Integration & Schema

6. Add `SqlBuilderJdbcTemplate` overloads that take `Query` (and param maps).
7. Document logging pattern with `SqlFormatter.inlineLiterals`.
8. Clarify and document the `ColumnRef` lifecycle and safe binding patterns.
9. Strengthen effective immutability of the schema after initialization.

### 8.3 Wave 3 – Internal Hygiene & Tests

10. Harmonize use of `render` / `compile` vs `transpile` in docs and examples.
11. Optionally simplify `ValueType` / `ConditionValue`.
12. Expand examples and tests to fully cover dialect behavior, CTEs, set operations, and pagination.

---

These proposals do not change the philosophy of sqlBuilder (fluent DSL, explicit dialects, Spring integration), but they clean up the edges, reduce future maintenance cost, and make the library more pleasant to use in real-world production codebases.

---

## 9. Complexity-Ordered To‑Do List

1. **Add `SqlQuery.query()` entry point** — quick API convenience method exposing the `Query` view without staged casting.
2. **Document API boundaries / SPI warnings** — update README/Javadocs to flag public vs. advanced packages.  
   - Add a “Public Surface” table to README referencing the `api.model`, `api.query`, and `api.format` packages along with the guarantee level.  
   - Under `api.query.spi`, add a banner Javadoc (and package-info) stating it targets framework integrations and may change without notice; link to AGENTS guidelines.  
   - Call out that anything under `core.*`, `processor.*`, or `examples.*` is unsupported for consumers, to discourage accidental dependencies.
3. **Document logging pattern (`SqlFormatter.inlineLiterals`)** — simple doc/example additions.  
   - Extend the Spring JDBC section with a snippet that renders `SqlAndParams`, feeds it to `SqlFormatter.inlineLiterals`, logs the debug SQL, and then hands the same `SqlAndParams` to `SqlBuilderJdbcTemplate`.  
   - Highlight “debug only” caveats inline so users avoid piping `inlineLiterals` output to JDBC drivers.  
   - Reference the helper from the troubleshooting section so bug reporters follow a consistent template when sharing SQL.
4. **Clarify `ColumnRef` lifecycle & schema immutability** — write guidance and minor guard rails around schema builders.  
   - Document in `Table`/`Schema` Javadocs that descriptors must be bound via `SchemaScanner` or `Tables.builder(...)` before use, and that mutation after initialization is unsupported.  
   - Make builders return unmodifiable views (and mention it here) so consumers notice when they attempt to mutate post-bootstrap.  
   - Provide a short “safe binding” How-To that contrasts the discouraged `ColumnRef.of(...)` shortcut with the preferred scanning/builder approaches.
5. **Add `SqlBuilderJdbcTemplate` overloads (Query, param maps)** — extend Spring helper signatures and tests.
6. **Introduce `SetOperator` + dialect mapping** — refactor set-operation rendering to go through dialect hooks.
7. **Route pagination solely through `Dialect.renderLimitOffset(...)` & remove `LimitTranspiler`** — centralize pagination rendering logic.
8. **Harmonize `render`/`compile` vs. `transpile` terminology** — tighten docs and code comments.
9. **Expand dialect-sensitive tests/examples** — add integration coverage for CTEs, set ops, pagination, optional filters.
10. **Simplify `ValueType`/`ConditionValue` internals** — refactor AST literals to reduce unused granularity.
