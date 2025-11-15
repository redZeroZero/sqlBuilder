# Binding Strict & Safety — Specification

## Scope
Defines binding-time guarantees for parameters used in `CompiledQuery.bind(...)`, safety around repeated placeholders, empty `IN` predicates, and raw SQL fragments.

## Terminology
- **Placeholder**: a `SqlParameter<?>` captured in `CompiledQuery` in the exact order it appears in the rendered SQL (`?` order).
- **Name**: the logical parameter name (e.g., `"id"`).
- **Occurrence**: each appearance of a placeholder in SQL; duplicates by name are allowed.

## Configuration

### Modes
- **Non-strict (default)**: tolerant extras in `bind(Map)`, no checks on RawSql literals, empty `IN` rewritten if enabled.
- **Strict**: enforce all checks; fail fast with clear errors.

### API
- Global flag (optional): `SqlBuilderFeatures.setStrictBinding(boolean enabled)`
- Per-call variants (recommended):
  - `CompiledQuery bind(Map<String,?> values, BindingOptions opts)`
  - `BindingOptions.strict()` / `BindingOptions.lenient()`
  - Shorthands: `bindStrict(Map<String,?>)` and `bind(Map<String,?>)`

---

## 1) Unknown keys in `bind(Map)`

### Behavior
- **Strict**: if the provided map contains keys not referenced by any placeholder name, fail with:
  - `IllegalArgumentException("Unknown parameters: <list>. Expected: <list>")`
- **Non-strict**: ignore unknown keys.

> **Implementation status**: current code always enforces the strict behavior (unknown keys raise `IllegalArgumentException` unconditionally).

### Always enforced (both modes)
- Missing required keys → `IllegalArgumentException("Missing parameters: <list>")`
- Name matching is **exact and case-sensitive** (documented).

---

## 2) Repeated parameters (same name, multiple `?`)

### Behavior
- `bind(Map)`:
  - Each occurrence with the same name receives the **same value** from the map.
  - If the value is missing → standard “Missing parameter” error.
- `bind(Object...)`:
  - **Strict**: disallow varargs when any name occurs more than once →  
    `IllegalArgumentException("Varargs binding disallowed with repeated placeholders: <name>")`
  - **Non-strict**: allowed **only** if the number of supplied values equals the total **occurrence count**; values are bound positionally.
    - If counts differ → error: `IllegalArgumentException("Expected <n> values, got <m>")`

> **Implementation status**: current code always rejects varargs binding when placeholders repeat; strict/lenient modes are not yet configurable.

**Recommendation**: prefer `bind(Map)` whenever repeated names exist.

---

## 3) Empty `IN` predicates

### Detection
An `IN` predicate is considered **empty** if the bound collection/array has size 0 **after** binding.

### Policies (configurable)
- **Strict (default in strict mode)**: **fail**  
  `IllegalArgumentException("Empty IN clause for parameter '<name>' is not allowed")`
- **Safe rewrite**: rewrite `col IN ()` to a dialect-neutral **always-false** predicate:
  - `1=0` (preferred), rendered via the dialect’s boolean literal support.
- **Lenient “skip predicate”** (optional, off by default): drop the predicate entirely.

Document the chosen default; typical default is **Strict** or **Safe rewrite**.

**Notes**
- `null` vs empty:
  - `null` → treated like missing value (error).
  - empty → handled by the selected policy above.

---

## 4) RawSql: ban value concatenation

### Allowed
- Fixed SQL fragments known at compile time.
- Parameters passed **only** via a typed parameter API, e.g.:
  ```java
  RawSql.text("price > ").param(SqlParameter.of("minPrice", BigDecimal.class))
  ```

### Disallowed
- String concatenation or interpolation of dynamic values into SQL fragments, e.g.:
  ```java
  RawSql.text("price > " + userInput) // ❌ forbidden
  ```

### Enforcement
- **Strict**: the RawSql builder rejects fragments that attempt to embed values directly; throw:
  - `IllegalArgumentException("Unparameterized literal detected in RawSql; use .param(...)")`
- **Non-strict**: allow but emit a WARN-level log in DEV builds (feature flag), never inline values in production logs.

---

## Error messages (normative)
- Must list **unknown**, **missing**, and (for varargs) **expected vs provided counts**.
- Include the **ordered placeholder list** (`[name1, name2, name1, ...]`) in debug messages when strict mode is on.
- Never print actual bound values in production logs; show only parameter **names**.

---

## Pseudocode (binding with Map)

```java
List<SqlParameter<?>> placeholders = compiled.placeholders(); // ordered with duplicates
Set<String> expected = placeholders.stream().map(SqlParameter::name).collect(toSet());

if (opts.strict()) {
  Set<String> unknown = values.keySet() - expected;
  if (!unknown.isEmpty()) throw IAE("Unknown parameters: " + unknown + "; Expected: " + expected);
}

List<Object> out = new ArrayList<>(placeholders.size());
for (SqlParameter<?> p : placeholders) {
  if (!values.containsKey(p.name())) throw IAE("Missing parameters: " + (expected - values.keySet()));
  Object v = values.get(p.name());
  // optional type check: if (v != null && p.type()!=Object.class && !p.type().isInstance(v)) throw IAE(...)
  out.add(v);
}
return new SqlAndParams(sql, out);
```

---

## Tests (must pass)

1. **Unknown keys (strict)**: map has extra `"x"` → error listing `"x"`.
2. **Unknown keys (lenient)**: extra key ignored.
3. **Missing keys**: error lists all missing names.
4. **Repeated names with Map**: same value applied to all occurrences.
5. **Varargs + repeated names (strict)**: error.
6. **Varargs + repeated names (lenient)**: positional ok when counts match.
7. **Empty IN (strict)**: error on empty list/array.
8. **Empty IN (safe rewrite)**: SQL contains `1=0`.
9. **RawSql concat (strict)**: error when detecting dynamic concatenation.
10. **RawSql param (strict)**: allowed via `.param(...)`.

---

## Compatibility

- Defaults keep existing behavior **except** where you enable strict mode.
- Introducing `bindStrict(Map)` allows adoption without breaking current callers.
- Varargs behavior remains, but stricter rules apply only in strict mode.

---

## Performance

- Binding is O(N) in the number of placeholder **occurrences**; strict unknown-key check adds set membership cost O(M), typically negligible.
