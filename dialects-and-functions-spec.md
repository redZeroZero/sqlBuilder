# sqlBuilder Functional Specification — Dialects & SQL Functions (v1)

**Version:** 0.1  
**Scope:** Introduce a minimal, explicit dialect abstraction and a first-class function model, and route all SQL generation through it.

This builds on the existing parameter-binding & compiled-query mechanics.

---

## 1. Goals

1. Make all SQL generation **dialect-aware** in a single, explicit place.
2. Avoid sprinkling `"MINUS"`, `"LIMIT"`, `"ESCAPE '\'"`, quoting rules, etc. across transpilers.
3. Provide a **minimal function model** that:
   - maps logical function names to dialect-specific syntax, and
   - is easy to extend without rewriting the core DSL.
4. Preserve:
   - the current fluent API,
   - the staged query approach,
   - existing behavior for Oracle as the default.

Non-goal for this iteration: full-blown jOOQ-level expression engine. We keep it lean.

---

## 2. Dialect Abstraction

### 2.1 `Dialect` Interface

Introduce a core `Dialect` interface in `sqlBuilder-core`:

```java
public interface Dialect {

    /**
     * Unique identifier for logging/debugging (e.g. "oracle", "postgres").
     */
    String id();

    /**
     * Quote an identifier (table, column, alias) correctly for this dialect.
     * Implementations MUST NOT assume the input is safe; they are responsible
     * for applying the dialect's quoting rules.
     */
    String quoteIdent(String raw);

    /**
     * Render LIMIT/OFFSET or equivalent pagination for this dialect.
     *
     * @param limit  nullable; if null, no upper bound.
     * @param offset nullable; if null, no offset.
     * @return SQL fragment starting with a space (e.g. " LIMIT ? OFFSET ?"),
     *         or empty string if neither is supported or requested.
     */
    String renderLimitOffset(Long limit, Long offset);

    /**
     * Return the appropriate keyword/operator for EXCEPT-like set operations.
     *
     * @param all whether EXCEPT ALL is requested
     * @return a SQL fragment such as "EXCEPT", "EXCEPT ALL", "MINUS"
     * @throws UnsupportedOperationException if the dialect cannot emulate the requested form
     */
    String exceptOperator(boolean all);

    /**
     * Character to use in ESCAPE clauses for LIKE.
     * Example: '\\' for "ESCAPE '\'"
     */
    char likeEscapeChar();

    /**
     * Map a logical function name + rendered argument SQL to a dialect-specific expression.
     *
     * @param logicalName normalized logical function name (e.g. "lower", "upper", "coalesce").
     * @param argsSql     list of already-rendered argument SQL fragments.
     * @return fully rendered function SQL for this dialect.
     * @throws UnsupportedOperationException if the function is not supported or cannot be emulated.
     */
    String renderFunction(String logicalName, java.util.List<String> argsSql);
}
