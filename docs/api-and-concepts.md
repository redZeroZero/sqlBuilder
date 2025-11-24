# API Surface & Core Concepts

## Public API Surface & Stability

| Package / Namespace | Purpose | Stability |
| --- | --- | --- |
| `org.in.media.res.sqlBuilder.api.model` (incl. `.annotation`) | Table/column/schema descriptors and annotation processor contracts. | **Stable** |
| `org.in.media.res.sqlBuilder.api.query` (+ subpackages except `.spi`) | Fluent SQL builders, dialect abstractions, helper utilities, formatters. | **Stable** |
| `org.in.media.res.sqlBuilder.api.query.spi` | Extension hooks for custom clauses/transpilers. | **Advanced / SPI** |
| `org.in.media.res.sqlBuilder.api.query.params`, `.helper`, `.format` | Parameter helpers, optional-condition builders, `SqlFormatter`. | **Stable** |
| `org.in.media.res.sqlBuilder.core.*`, `.processor.*`, `.examples.*` | Internal implementations and docs fixtures. | **Internal** |

## Core concepts

- **Entry points:** `SqlQuery.newQuery()` yields staged builders (`SelectStage` → `FromStage`) with compile-time clause hints. `SqlQuery.query()` widens immediately to `Query`; both converge once you reach `Query`.
- **Rendering:** `render()` returns `SqlAndParams` (SQL with `?` placeholders + ordered params). Use `.sql()` for the SQL string, `.params()` for the values. `transpile()` exists for SPI code but is deprecated for user flows.
- **Validation:** `SqlQuery.validate(query)` runs structural checks (grouping, aliases, parameters) without executing SQL.
- **Dialect propagation:** Builders carry the active `Dialect`. Use `SqlQuery.newQuery(myDialect)` or set it on your schema. Dialect controls quoting, pagination, set operators, LIKE escaping, and function rendering.
- **Compiled queries:** `compile()` freezes SQL + placeholders; later `bind(...)` supplies values (map binding rejects unknown names).
