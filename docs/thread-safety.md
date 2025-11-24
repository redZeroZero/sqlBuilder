# Thread Safety Contract

`sqlBuilder` separates mutable builders from immutable artefacts:

- Builders (`Query`, `With/CTE`, `ConditionGroup`, etc.) are **not thread-safe**—create per call/request.
- Compiled artefacts (`CompiledQuery`, `SqlParameter`) are immutable and safe to share/cache.
- Each `bind(...)` call returns a fresh `SqlAndParams`; treat it as disposable per execution.
- Dialect scope is captured per render/compile based on the dialect you passed in; do not touch internal contexts directly.
- Recommended lifecycle: build (per request) → compile (once) → bind (per execution) → execute with JDBC.

See `documents/THREAD_SAFETY.md` for the detailed contract.
