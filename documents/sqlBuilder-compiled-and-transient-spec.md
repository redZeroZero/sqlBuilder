# sqlBuilder Functional Specification — Compiled & Transient Parameter Binding

**Version:** 0.2  
**Scope:** Support both:
1. Reusable compiled queries with placeholders and late binding.
2. Transient queries with immediate values (current style),
   using the same internal parameterization model.

This refines the previous spec to match the desired behavior.

---

## 1. Design Goals

1. A **single internal abstraction** for parameters (no separate logic for “inline” vs “compiled”).
2. Support two ergonomic workflows:
   - **Transient mode**: build → execute with direct values.
   - **Compiled mode**: build once with symbolic parameters → bind many times.
3. Ensure:
   - All dynamic values are represented as JDBC bind parameters (`?`),
   - No user values interpolated into SQL,
   - Straightforward integration with Spring `JdbcTemplate` & friends.
4. Preserve:
   - Fluent DSL,
   - Staged query API,
   - Backward compatibility for existing usage.

---

## 2. Core Types

### 2.1 SqlAndParams

Runtime-executable query:

```java
public final class SqlAndParams {
    private final String sql;
    private final List<Object> params;
}
```

**Requirements**

- `sql` contains only `?` placeholders for dynamic values.
- `params` lists all bound values in placeholder order.
- Immutable from the consumer perspective.

This is the type used to actually hit the database.

---

### 2.2 SqlParameter<T>

A symbolic parameter slot for compiled queries.

```java
public interface SqlParameter<T> {
    String name();
    Class<T> type(); // optional but recommended
}
```

**Requirements**

- `name` uniquely identifies the logical parameter within a query.
- May be referenced multiple times in the same query.

Helper:

```java
static <T> SqlParameter<T> param(String name);
```

---

### 2.3 CompiledQuery

A reusable, compiled form of a query.

```java
public final class CompiledQuery {
    private final String sql;
    private final List<Placeholder> placeholders; // one entry per "?", in order

    public SqlAndParams bind(Object... values);
    public SqlAndParams bind(Map<String, ?> values);
}

record Placeholder(SqlParameter<?> parameter, Object fixedValue) {}
```

**Requirements**

1. `sql` is the final SQL with `?` placeholders.
2. `placeholders` tracks every `?` in-order:
   - if the slot originates from a `SqlParameter`, `parameter` is non-null and `fixedValue` is `null`.
   - if it comes from an inline literal, `parameter` is `null` and `fixedValue` stores the literal value.
3. `bind(...)`:
   - Produces `SqlAndParams` by walking `placeholders` sequentially.
   - Named slots pull their value from the provided arguments; anonymous slots reuse their `fixedValue` automatically.
   - MUST fail with clear exceptions when:
     - a required parameter is missing,
     - an unknown name is provided,
     - or arity/order does not match the template.

`CompiledQuery` is immutable and safe to share.

---

## 3. Usage Modes

### 3.1 Transient Mode (inline values, one-shot)

**Example**

```java
SqlAndParams sp = SqlQuery
    .from(Employee.TABLE)
    .select(Employee.FIRST_NAME)
    .where(Employee.FIRST_NAME).eq("Alice")
    .render();
```

**Semantics**

- Inline values (`"Alice"`) are represented internally as anonymous parameter slots.
- `render()`:
  - Produces `SELECT ... WHERE e.first_name = ?`
  - Params: `["Alice"]`
- No explicit `SqlParameter` or `CompiledQuery` needed.
- Behavior matches existing usage, but now parameterized.

---

### 3.2 Compiled Mode (template + late binding)

**Example**

```java
SqlParameter<String> pName = SqlParameters.param("name");

CompiledQuery cq = SqlQuery
    .from(Employee.TABLE)
    .select(Employee.FIRST_NAME)
    .where(Employee.FIRST_NAME).eq(pName)
    .compile();

SqlAndParams a = cq.bind(Map.of("name", "Alice"));
SqlAndParams b = cq.bind(Map.of("name", "Bob"));
```

**Semantics**

- `eq(pName)` records a parameter slot instead of a value.
- `compile()`:
  - Generates stable SQL with `?`.
  - Records `parameters = [pName]` (in placeholder order).
- Each `bind(...)`:
  - Uses the same SQL,
  - Provides different `params` lists.

This supports the “build once and reuse many times” requirement.

---

## 4. Query API

### 4.1 New Methods

On `Query` / `QueryImpl`:

```java
SqlAndParams render();   // transient & mixed usage
CompiledQuery compile(); // reusable templates
```

**Rules**

- `render()`:
  - Uses all inline values as anonymous parameters.
  - If `SqlParameter` slots are present:
    - MAY accept an additional binding context (future extension),
    - or MUST fail if unbound (for now, keep simple: all parameters must be resolvable).
- `compile()`:
  - Allowed if:
    - all runtime-varying values are expressed via `SqlParameter`,
    - inline constants are acceptable as fixed parts of the template.
  - Fails fast if the query uses patterns incompatible with deterministic templates.

### 4.2 Mixing Inline Constants & Parameters

Allowed and encouraged:

```java
SqlParameter<Integer> pMinAge = param("minAge");

CompiledQuery cq = SqlQuery
    .from(Employee.TABLE)
    .select(Employee.FIRST_NAME)
    .where(Employee.COUNTRY).eq("FR")          // fixed constant (anonymous param)
    .and(Employee.AGE).supOrEqTo(pMinAge)      // dynamic
    .compile();

// Later:
cq.bind(Map.of("minAge", 30));
```

**Semantics**

- `"FR"` becomes an anonymous parameter baked into the template.
- `pMinAge` is provided at bind time.
- `bind(...)` ignores anonymous slots (already have values), resolves named ones.

**Binding semantics**

- `bind(Object... values)` supplies values **only for named placeholders**, in the exact order they appear inside `placeholders`. Anonymous slots do not consume arguments.
- `bind(Map<String, ?> values)` looks up each named placeholder by `SqlParameter.name()` while skipping anonymous entries. Extra map entries are ignored; missing entries trigger an `IllegalArgumentException` that lists the parameter name.

---

## 5. Internal Rendering Rules

### 5.1 Shared Model

All conditions and expressions internally store one of:

1. A **literal/constant** value (used in transient mode or fixed constraints), OR
2. A `SqlParameter<?>` reference (used in compiled mode).

No path is allowed to inject raw string values directly into SQL.

### 5.2 How `render()` Works

For each predicate/expression:

- Literal → append `?`, push its value into `params`.
- `SqlParameter` → must have:
  - either an associated value (if render-with-bindings is supported),
  - or cause a clear error if unresolved in this mode.

For subqueries:

- Recursively call `render()` or `compile()`.
- Embed `(...)` and append subquery params.

### 5.3 How `compile()` Works

- Same traversal as `render`, but instead of producing final `params`:
  - Build `sql` with `?`.
  - Populate `parameters` with, for each `?`:
    - `SqlParameter` if it came from a slot.
    - a synthetic internal entry for inline constants.

---

## 6. Special Value Semantics

Applies equally in both modes.

### 6.1 NULL

- `eq(null)` → `IS NULL` (no parameter).
- `notEq(null)` → `IS NOT NULL`.
- `eq(SqlParameter)` bound with `null` at runtime:
  - NOT automatically converted to `IS NULL`.
  - Recommended behavior: validation error; callers should model explicit NULL semantics.

### 6.2 IN

- For inline values:
  - N ≥ 1: `IN (?, ?, ...)` with N params.
  - N = 0:
    - Either `1 = 0` (preferred),
    - Or fail fast (configurable).
- For `SqlParameter`-based collections:
  - Out of scope for this iteration; require explicit API later (`inCollection(param)`). For now, callers should either inline the values (transient mode) or expand the predicate manually via grouped `OR` conditions using scalar parameters.

### 6.3 BETWEEN

- Exactly 2 non-null operands (literal or parameters).
- No implicit reordering in this iteration.
- Invalid arity or nulls → validation error.

---

## 7. Error Handling

1. Invalid operator/value combinations (e.g. `LIKE` with null) MUST throw clear exceptions.
2. `CompiledQuery.bind(...)` MUST:
   - Report missing parameters by name.
   - Ignore extra map entries in the map-based overload without error.
3. `compile()` MUST:
   - Reject ambiguous queries where required parameter values cannot be determined.

---

## 8. Backward Compatibility

1. Existing DSL usage with inline values and `transpile()` continues to work:
   - Internally implemented via `render()` and parameter binding.
2. New compiled-query features (`SqlParameter`, `compile`, `bind`) are fully additive.
3. Documentation and examples SHOULD:
   - Promote `render(...)` + `SqlAndParams` for executions.
   - Showcase `compile(...)` + `bind(...)` for reusable queries.

---

This specification aligns with your intent:

- You can **create queries once with placeholders** and bind later.
- You can **still build transient queries with inline values** as today.
- Internally, everything is driven by the same parameter-slot model,
  giving you safety, clarity, and a clean path to SQL caching.
