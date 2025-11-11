# sqlBuilder Internal API Cleanup Specification

Version: 1.0  
Status: Draft (implementation-ready)  
Scope: Align package structure and types with the Public API v1.0 spec

---

## 1. Purpose

This document defines the concrete steps to:

1. Remove internal/engine-level concepts from the `api.*` surface.
2. Clearly mark showcase/benchmark utilities as internal.
3. Eliminate unfinished or misleading public types and fix visible naming issues.

The goal is to ensure that **only the intended DSL and supporting types are recognized as the public, stable API**, while implementation details remain free to evolve.

---

## 2. Target State Overview

After applying this spec:

- `org.in.media.res.sqlBuilder.api.*` contains only:
  - the public DSL (`SqlQuery`, staged interfaces, helpers),
  - the public model & params abstractions.
- Engine, transpiler, rendering, and low-level utilities live under `org.in.media.res.sqlBuilder.core.*` (or are clearly documented as INTERNAL/SPI).
- Examples meant for end users depend only on `api.*`.
- Benchmark/demo utilities are explicitly marked as **INTERNAL**.
- No “zombie” or half-baked classes are exposed as part of the public contract.
- Public names are clean and professional (no obvious typos).

---

## 3. API Package Cleanup: `api.query`

### 3.1 Allowed Public Types in `org.in.media.res.sqlBuilder.api.query`

The following types are part of the **public** API and MUST remain:

- `SqlQuery`
- `SelectStage`
- `FromStage`
- `PredicateStage`
- `QueryStage`
- `Query` (if exposed as a user-facing abstraction)
- `SqlParameter<T>`
- `SqlParameters`
- `CompiledQuery`
- `SqlAndParams`
- Public helper types intentionally exposed (e.g. `OptionalConditions`, `QueryHelper`, `RawSql`, etc., as per the Public API spec)

All other types under `api.query` MUST be either:

- moved to `core.*`, or
- explicitly documented as INTERNAL/SPI.

### 3.2 Types to Move or Mark INTERNAL

The following categories of types are considered **engine-level** and should not be part of the main public DSL:

- `*Transpiler` types:
  - `SelectTranspiler`, `WhereTranspiler`, `QueryTranspiler`, `WithTranspiler`, etc.
- Low-level contracts used only for SQL generation:
  - `Transpilable`
  - `Resetable`
  - `Clause`
  - `Connector`
  - And similar abstractions related to internal query model or rendering.

#### Required Actions

1. **Preferred**: Move these types to `org.in.media.res.sqlBuilder.core.query.*`, e.g.:

   - `org.in.media.res.sqlBuilder.core.query.transpiler.SelectTranspiler`
   - `org.in.media.res.sqlBuilder.core.query.internal.Transpilable`
   - `org.in.media.res.sqlBuilder.core.query.internal.Clause`

2. If a type is intentionally exposed as a Service Provider Interface (SPI) for advanced extensions:

   - Keep it or place it under a dedicated SPI package (e.g. `core.query.spi` or `api.query.spi`).
   - Add mandatory Javadoc:

     ```java
     /**
      * INTERNAL / SPI.
      * Not intended for general application use.
      * This contract may change between minor versions.
      */
     ```

3. Ensure **no official example** (non-internal) imports or relies on these internal/SPI types.

---

## 4. Tools & Showcase Classes

### 4.1 Scope

Types such as:

- `QueryShowcase`
- `QueryBenchmark`
- Any performance/demo/showcase utilities

are not part of the stable API.

### 4.2 Required Actions

1. Move them into an explicitly internal/showcase package, for example:

   - `org.in.media.res.sqlBuilder.examples.internal`
   - or `org.in.media.res.sqlBuilder.tools.internal`

2. Add Javadoc to each:

   ```java
   /**
    * INTERNAL DEMO / BENCHMARK.
    * Not part of the public sqlBuilder API.
    * May change or be removed without notice.
    */
   ```

3. Ensure they are:
   - not referenced from README or official “Getting Started” docs as standard usage,
   - clearly presented (if ever mentioned) as internal tooling only.

---

## 5. Zombie Classes & Cosmetic Fixes

### 5.1 Unfinished / Experimental Public Types

Identify types that are:

- empty or nearly empty,
- undocumented stubs,
- experimental concepts not yet supported.

Examples (to adapt based on current code):

- `QueryBuilder`, `QueryBuilderImpl` (if still present and unused)
- Any public interface/class not referenced by public examples or tests.

**Required Actions:**

- Remove them from the public surface:
  - either delete them,
  - or move to an `internal` package if you want to keep them for experimentation.
- Do **not** leave placeholders in the API; they signal instability to users.

### 5.2 Naming & Typo Corrections

All types and methods visible from `api.*` or examples MUST:

- use correct, conventional naming,
- avoid obvious typos.

**Examples to fix (if still present):**

- `CLauseFactory` → `ClauseFactory`
- `instanciateXxx` → `instantiateXxx`

These are considered part of the cleanup and should be corrected before declaring the API “stable”.

---

## 6. Example Code Policy

### 6.1 Official Examples

For standard/example modules (e.g. `sqlBuilder-examples`):

- Only permitted imports:
  - `org.in.media.res.sqlBuilder.api.*`
  - Model/annotation packages intended as public.
- Not permitted:
  - Direct imports of `core.*`
  - Use of internal/SPI/transpiler classes.

If an example requires touching internals:

- place it under an `*.internal` package,
- annotate it as INTERNAL in Javadoc.

### 6.2 Contract

If it appears as a top-level example and is not explicitly marked internal, it is implicitly treated as “supported usage”. The codebase and docs must reflect that.

---

## 7. Definition of Done

The internal cleanup is complete when all of the following are true:

1. `org.in.media.res.sqlBuilder.api.query` only contains:
   - the public DSL,
   - public parameter & compilation types,
   - clearly documented helpers.
2. All transpiler/engine/low-level constructs:
   - are moved under `core.*` or `*.spi`, or
   - are documented as INTERNAL / SPI.
3. `QueryShowcase`, `QueryBenchmark`, and similar utilities:
   - live under `*.internal` or `*.examples.internal`,
   - have INTERNAL DEMO / BENCHMARK Javadoc.
4. No unfinished stub/zombie classes remain in the public API.
5. All public-facing class/method names are cleaned of obvious typos.
6. Official examples compile and run using **only** the intended public API.

Once these conditions are met, the library presents a clean, trustworthy public surface and is ready for broader adoption and for layering additional features (Spring integration, richer CTE/subquery support, etc.) on top of a stable core.
