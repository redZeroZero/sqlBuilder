# sqlBuilder Public API Stabilization Specification

Version: 1.0  
Status: Draft (implementation-ready)  
Audience: sqlBuilder maintainers and internal consumers

---

## 1. Goal

Define a **clear, stable public API surface** for `sqlBuilder` and explicitly separate **internal implementation details**, so that:

- application teams know what they can safely depend on,
- maintainers are free to refactor internals without breaking consumers,
- examples and documentation consistently promote the intended usage.

This document specifies the **Public API v1.0** contract.

---

## 2. Core Rules

1. Packages under `org.in.media.res.sqlBuilder.api.*` constitute the **official public API**.
2. Packages under `org.in.media.res.sqlBuilder.core.*` are **internal implementation details**:
   - They may change without notice.
   - They must not be referenced by application code or official examples (except in explicitly marked internal/showcase code).
3. Official documentation and example code MUST use only the public API, unless a package is clearly marked as internal.

---

## 3. Public API Surface (v1.0)

The following types and packages are considered **stable** once this spec is applied.

### 3.1 Query Entry Point & Staged DSL

**Package:** `org.in.media.res.sqlBuilder.api.query`

**Public, stable types:**

- `SqlQuery`
- `SelectStage`
- `FromStage`
- `PredicateStage`
- `QueryStage`
- `Query` (only as a high-level, user-facing interface if currently exposed)

**Contract:**

- `SqlQuery` is the primary entry point for building queries.
- The staged interfaces (`SelectStage`, `FromStage`, `PredicateStage`, `QueryStage`) guide the user through a valid query-building flow.
- Methods exposed on these interfaces are part of the public contract and must remain backward compatible.
- Implementation classes (e.g. `*Impl`) are **not** part of the public API, even if technically visible.

Any future enhancements to the DSL should be added via new methods on these public interfaces or via new public helper types, preserving compatibility.

---

### 3.2 Mapping & Schema Model

**Package:** `org.in.media.res.sqlBuilder.api.model`  
**Package:** `org.in.media.res.sqlBuilder.api.annotations`

**Public, stable types:**

- `@SqlTable`
- `@SqlColumn`
- `ColumnRef<T>`
- `TableDescriptor`
- `TableRow`

**Contract:**

- These types define how Java-side table/column metadata is modeled for use with the query DSL.
- `ColumnRef<T>` is the primary mechanism to represent typed columns in queries.
- Official examples MUST use `ColumnRef` and annotations rather than ad-hoc string-based column access.
- Behavior of these types must remain stable and documented.

If some schema-scanning utilities are promoted (e.g. a higher-level schema initializer), they should live under `api.*` and be explicitly documented.

---

### 3.3 Parameters & Compilation

**Package:** `org.in.media.res.sqlBuilder.api.query` (or dedicated `api.params` if present)

**Public, stable types:**

- `SqlParameter<T>`
- `SqlParameters` (factory/utility)
- `CompiledQuery`
- `SqlAndParams`

**Contract:**

- All parameterized queries must ultimately compile to a `CompiledQuery`.
- `CompiledQuery`:
  - provides access to the final SQL string with placeholders,
  - exposes a stable API to bind parameter values (e.g. `bind(Map<String, Object>)`).
- `SqlAndParams`:
  - represents bound SQL + ordered parameter values ready for execution.
- The semantics of placeholder ordering and binding are part of the public contract and must be documented.

---

### 3.4 Official Helpers

The following helpers are considered part of the public API, assuming they are present and stable:

- `OptionalConditions`
- `QueryHelper`
- `RawSql` and all `*Raw(...)` methods (once implemented according to the dedicated spec)
- `Dialects` (for accessing officially supported dialect instances)

**Contract:**

- These helpers encapsulate common patterns and should be safe to use in application code.
- Their purpose and supported scenarios must be documented.
- Backwards compatibility should be maintained; breaking changes require a new major version or alternative entry points.

---

## 4. Internal API (Non-Public Surface)

Everything in the following areas is **internal** and not part of the compatibility contract:

**Packages:**

- `org.in.media.res.sqlBuilder.core.query.*`
- `org.in.media.res.sqlBuilder.core.query.impl.*`
- `org.in.media.res.sqlBuilder.core.query.transpiler.*`
- `org.in.media.res.sqlBuilder.core.model.*` (unless specific types are explicitly promoted)
- `org.in.media.res.sqlBuilder.core.utils.*`
- Any package or class name containing `impl`, `internal`, `transpiler`, `factory`, etc.

**Rules:**

1. Internal types MAY remain `public` for technical reasons, but:
   - MUST be documented with Javadoc such as:

     ```java
     /**
      * INTERNAL API.
      * Not part of the supported sqlBuilder public contract and may change without notice.
      */
     ```

2. Implementation details:
   - `QueryImpl`, `SelectImpl`, `WhereImpl`, transpilers, and factories:
     - are free to change,
     - must not be referenced by official example code.
3. Showcases or benchmarks using internal types:
   - must be placed in clearly marked packages such as:
     - `org.in.media.res.sqlBuilder.examples.internal`
     - `org.in.media.res.sqlBuilder.examples.showcase`
   - and clearly labeled as non-API.

This distinction allows aggressive refactoring inside `core.*` without impacting consumers.

---

## 5. Cleanup & Consistency Requirements

To comply with this spec, the following actions are required.

### 5.1 Remove or Fix Incomplete Public Types

- Remove or implement:
  - `QueryBuilder`, `QueryBuilderImpl` and any other “empty” or experimental public types.
- Either:
  - provide a complete, documented behavior,
  - or ensure they are not exposed as part of the public API (move to internal packages or delete).

### 5.2 Naming & Typos

Correct inconsistent or unprofessional naming in the public surface, for example:

- `CLauseFactory` → `ClauseFactory`
- `instanciate*` → `instantiate*`

These changes are part of the stabilization effort and should be completed before announcing the API as stable.

---

## 6. Official Examples Policy

A dedicated `sqlBuilder-examples` module (or equivalent) MUST:

1. Use only:
   - `SqlQuery`
   - public staged interfaces
   - `@SqlTable`, `@SqlColumn`, `ColumnRef`
   - `SqlParameter`, `SqlParameters`, `CompiledQuery`, `SqlAndParams`
   - public helpers (`OptionalConditions`, `QueryHelper`, `RawSql`, `Dialects`, etc.)
2. Avoid:
   - any direct reference to types under `core.*`, except in packages/namespaces explicitly marked as `*.internal` or `*.showcase`.
3. Serve as:
   - the **authoritative reference** for how to use the library.

If it appears in `examples` without an `internal` marker, it is implicitly endorsed as “this is how you should do it”.

---

## 7. Backward Compatibility Promise

Once this spec is applied (Public API v1.0):

- No breaking changes to:
  - package names under `org.in.media.res.sqlBuilder.api.*`,
  - signatures and behavior of public types/methods listed above,
  - semantics of `CompiledQuery` / parameter binding,
  - core DSL flow via `SqlQuery` and staged interfaces,
- unless:
  - the change is released as a new major version
  - or an alternative API is introduced and the old one is deprecated with a migration path.

---

## 8. Definition of Done (for #1)

The Public API is considered “stabilized” when:

1. The codebase reflects the API/internal split:
   - `api.*` contains all intended public types.
   - `core.*` is treated and documented as internal.
2. Internal classes have:
   - appropriate Javadoc warnings if publicly visible.
3. All official examples rely only on the public API.
4. Obvious naming/typo issues in public types are fixed.
5. A short “Public API Overview” document can be produced using only the `api.*` namespace.

At this point, higher-level work (Spring/JDBC integration, subqueries/CTE ergonomics, typing/DX improvements, integration tests & perf) can safely build on a stable, well-defined foundation.

