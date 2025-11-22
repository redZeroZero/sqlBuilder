# sqlBuilder Functional Specification — Optional Conditions with Compiled Queries

**Title:** Optional, Reusable, Param-Driven Filters (`whereOptional*`)  
**Version:** 0.1  
**Audience:** sqlBuilder contributors / advanced consumers  
**Scope:** Define how to express *optional filters* in a **single compiled query** using `SqlParameter` and `CompiledQuery`, without dynamic SQL rewriting at bind-time and without breaking caching.

This builds on:

- `SqlAndParams`
- `SqlParameter<T>`
- `CompiledQuery`
- `Dialect`
- Standard predicate DSL (`where(...)`, `and(...)`, etc.)

---

## 1. Problem Statement

We want to support this usage pattern:

> Build **one** compiled SQL query that:
> - can be reused,
> - sometimes applies a filter when a parameter is provided,
> - sometimes skips that filter when the parameter is `null` (or “inactive”),
> - without rebuilding SQL, and without any magic inside `bind()`.

Constraints:

1. SQL structure MUST be stable once compiled.
2. `CompiledQuery.bind(...)` MUST:
   - only bind values,
   - never modify or drop predicates.
3. Behavior MUST be explicit in both DSL and generated SQL.
4. Must be compatible with JDBC-style bind parameters (`?`) and the existing placeholder model.

---

## 2. Core Idea

Optional behavior is encoded directly in SQL as a **stable boolean expression**:

```sql
(:param IS NULL OR <predicate-using-:param>)
```

Examples:

- Optional equality:
  ```sql
  (:name IS NULL OR e.name = :name)
  ```
- Optional LIKE:
  ```sql
  (:name IS NULL OR e.name LIKE :name)
  ```
- Optional minimum:
  ```sql
  (:minSalary IS NULL OR e.salary >= :minSalary)
  ```

At runtime:

- If param is `NULL`:
  - `param IS NULL` → `TRUE` → the `OR` group is `TRUE` → filter neutralized.
- If param is non-null:
  - `param IS NULL` → `FALSE` → expression reduces to the real predicate.

This:

- keeps SQL fixed,
- moves the “optional” decision into bound data,
- plays nicely with `CompiledQuery` and DB plan caching.

---

## 3. DSL Design

We introduce **explicit optional-condition helpers**, distinct from `whereIf*`.

### 3.1 Principles

1. `whereIf*`:
   - Build-time decision.
   - Adds or skips predicates while building the query.
   - Best for transient queries or a small set of cached variants.

2. `whereOptional*` (this spec):
   - Always emits a condition in the compiled SQL.
   - Uses `IS NULL OR ...` logic.
   - Behavior changes only based on bound parameter values.
   - Designed specifically for reuse with `CompiledQuery`.

3. `bind()`:
   - Never mutates structure.
   - Only supplies values for pre-defined placeholders.

---

## 4. High-Level Optional Helpers

### 4.1 `whereOptionalEquals`

```java
PredicateStage whereOptionalEquals(ColumnRef<T> column, SqlParameter<T> param);
```

**Generated SQL:**

```sql
(:param IS NULL OR column = :param)
```

**Usage:**

```java
SqlParameter<String> pName = SqlParameters.param("name");

CompiledQuery cq = SqlQuery
    .from(Employee.TABLE)
    .select(Employee.ALL)
    .whereOptionalEquals(Employee.NAME, pName)
    .compile(dialect);

// No filter:
cq.bind(Map.of("name", null));

// With filter:
cq.bind(Map.of("name", "Alice"));
```

---

### 4.2 `whereOptionalLike`

```java
PredicateStage whereOptionalLike(ColumnRef<String> column, SqlParameter<String> param);
```

**Generated SQL:**

```sql
(:param IS NULL OR column LIKE :param)
```

Caller is responsible for passing the desired pattern (`"%Alice%"`, etc.).

**Usage:**

```java
SqlParameter<String> pName = SqlParameters.param("name");

CompiledQuery cq = SqlQuery
    .from(Employee.TABLE)
    .select(Employee.ALL)
    .whereOptionalLike(Employee.NAME, pName)
    .compile(dialect);

// Disable:
cq.bind(Map.of("name", null));

// Enable:
cq.bind(Map.of("name", "%ALICE%"));
```

---

### 4.3 `whereOptionalGreaterOrEqual`

```java
PredicateStage whereOptionalGreaterOrEqual(
    ColumnRef<? extends Number> column,
    SqlParameter<? extends Number> param
);
```

**Generated SQL:**

```sql
(:param IS NULL OR column >= :param)
```

---

### 4.4 Additional Helpers (Optional)

May be added later with the same pattern:

- `whereOptionalLessOrEqual`
- `whereOptionalEqualsIgnoreCase`
- etc.

---

## 5. Generic Optional Builder (Advanced)

A generic building block can be offered:

```java
default <T> PredicateStage whereOptional(
        SqlParameter<T> param,
        BiFunction<PredicateStage, SqlParameter<T>, PredicateStage> whenPresent)
```

**Intended semantics:**

This is equivalent to:

```sql
(:param IS NULL OR ( <predicate-built-by-whenPresent> ))
```

**Conceptual implementation:**

1. Build an `IS NULL` condition for `param`.
2. Build predicate via `whenPresent.apply(...)` using the same `SqlParameter`.
3. Wrap both in an `OR` group in the internal condition tree.

**Usage:**

```java
SqlParameter<String> pName = SqlParameters.param("name");

CompiledQuery cq = SqlQuery
    .from(Employee.TABLE)
    .select(Employee.ALL)
    .whereOptional(pName, (q, p) -> q.where(Employee.NAME).like(p))
    .compile(dialect);
```

For most users, `whereOptionalEquals` / `whereOptionalLike` will be easier; `whereOptional` is an expert tool.

---

## 6. Internal Behavior & Placeholders

### 6.1 Multiple Placeholders for One `SqlParameter`

For `whereOptionalEquals(Employee.NAME, pName)`:

Generated (conceptual) SQL:

```sql
WHERE (? IS NULL OR employee.name = ?)
```

Both `?` are linked to the **same** `SqlParameter` (`pName`) in the `CompiledQuery`’s placeholder list.

At bind time:

```java
cq.bind(Map.of("name", "Alice"));
// → params: ["Alice", "Alice"]
```

```java
cq.bind(Map.of("name", null));
// → params: [null, null]
```

`bind(...)`:

- does not inspect SQL,
- just assigns the same value to all placeholders of the same param.

### 6.2 No Structural Mutation in `bind`

`CompiledQuery.bind(...)` MUST:

- never remove or rewrite parts of the SQL,
- never change operators,
- never introspect conditions.

All optional semantics are a product of the boolean logic baked into the compiled SQL.

---

## 7. Examples

### 7.1 Multiple Optional Filters

```java
SqlParameter<String>  pName      = SqlParameters.param("name");
SqlParameter<Integer> pMinSalary = SqlParameters.param("minSalary");

CompiledQuery cq = SqlQuery
    .from(Employee.TABLE)
    .select(Employee.ALL)
    .whereOptionalLike(Employee.NAME, pName)
    .whereOptionalGreaterOrEqual(Employee.SALARY, pMinSalary)
    .compile(dialect);
```

Conceptual SQL:

```sql
SELECT ...
FROM employee e
WHERE
    ( ? IS NULL OR e.name LIKE ? )
AND ( ? IS NULL OR e.salary >= ? )
```

Executions:

- No filters:
  ```java
  cq.bind(Map.of("name", null, "minSalary", null));
  ```
- Only name:
  ```java
  cq.bind(Map.of("name", "%Bob%", "minSalary", null));
  ```
- Only minSalary:
  ```java
  cq.bind(Map.of("name", null, "minSalary", 50000));
  ```
- Both:
  ```java
  cq.bind(Map.of("name", "%Bob%", "minSalary", 50000));
  ```

Always the same SQL; only parameter values change.

---

## 8. Relationship with `whereIf*`

### 8.1 `whereIf*` — Build-Time Optional

Use for:

- readable, minimal SQL,
- classic REST/search filters,
- cases where rebuilding the query per request is acceptable.

Example:

```java
SqlQuery
    .from(Employee.TABLE)
    .select(Employee.ALL)
    .whereIfNotBlank(name, (q, n) -> q.where(Employee.NAME).like('%' + n + '%'))
    .whereIfNotNull(minSalary, (q, s) -> q.where(Employee.SALARY).supOrEqTo(s))
    .render(dialect);
```

SQL varies structurally depending on which filters are present.

### 8.2 `whereOptional*` — Bind-Time Optional

Use for:

- advanced scenarios,
- need for **one compiled SQL** reused across many calls,
- willingness to accept slightly more verbose `(param IS NULL OR ...)` logic.

Docs MUST clearly differentiate the two families.

---

## 9. Non-Goals & Caveats

1. No hidden:
   - “if param is null, auto-skip condition” inside `bind()`.
2. For complex cases like optional `IN` with collections:
   - out of scope of v1,
   - should be handled via `whereIfNotEmpty` (build-time),
   - or dedicated later APIs.
3. Avoid stacking a large number of `whereOptional*`:
   - for many filters, prefer `whereIf*` + a small cache of structural variants
   - instead of one huge `(param IS NULL OR ...)` chain.

---

## 10. Error Handling

- If a required parameter for `whereOptional*` is missing at `bind`:
  - throw a clear exception (e.g. `Missing value for parameter 'name'`).
- If `SqlParameter` or column references are invalid:
  - fail during query build/compile.
- If dialect cannot support a needed construct:
  - fail at compile-time (`UnsupportedOperationException`), not at execution-time.

---

## 11. Summary

This design:

- Provides a **clean, explicit** mechanism for optional filters in compiled queries.
- Preserves:
  - deterministic SQL structure,
  - simple `bind` semantics,
  - compatibility with existing parameter binding and dialects.
- Complements `whereIf*`:
  - `whereIf*` → structural (build-time) options,
  - `whereOptional*` → behavioral (bind-time) options via boolean logic.

It is “sexy” in the right way: powerful, predictable, and debuggable.
