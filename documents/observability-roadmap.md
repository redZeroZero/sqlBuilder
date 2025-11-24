# Observability & Ergonomics Roadmap

This page sketches the planned observability and ergonomics features to make sqlBuilder production-friendly and traceable.

## Goals

- Give developers visibility into rendered SQL, parameters, timing, and validation results.
- Integrate cleanly with common observability stacks (SLF4J, Micrometer, OpenTelemetry) without forcing a dependency.
- Keep performance overhead negligible and make noisy output opt-in, sampled, and maskable.

## Proposed Features

### 1) Pluggable SQL logger

- New listener/SPI that receives `SqlAndParams` right before execution.
- Default SLF4J logger that pretty-prints SQL and params at DEBUG; INFO only when explicitly enabled.
- Param masking by name/regex for sensitive values; optional inline literals for troubleshooting.
- Sampling to reduce noise under load.

### 2) Tracing integration (Micrometer / OpenTelemetry)

- Optional module that wraps render/compile/bind with timers and spans:
  - Span name: `sqlbuilder.render` / `sqlbuilder.compile` / `sqlbuilder.bind`.
  - Tags: `dialect`, `query.id` (if supplied), parameter count, validation result.
- Metrics: timers for render/compile/bind; counters for validation failures.
- Minimal dependencies: activate only if Micrometer/OTel is on the classpath.

### 3) Spring Boot starter (auto-config)

- Auto-register Dialect + Schema + SqlBuilderJdbcTemplate with observability hooks.
- Properties to toggle pretty-print, param masking, sampling, log level, and tracer enablement.
- Optional actuator endpoint to pretty-print a query by id (from a registered catalog) for debugging.

### 4) Validation-on-startup

- Spring bean that accepts a list of `CompiledQuery` beans and runs `SqlQuery.validate(...)`.
- Fails fast or logs warnings depending on configuration; publishes metrics for failures.

### 5) Diagnostic metadata

- Attach dialect name, schema name, and optional `query.id` to logs and spans.
- Allow callers to propagate a correlation id into hooks for cross-service tracing.

### 6) Sampling and filters

- Configurable sampling rate for logging/tracing.
- Inclusion/exclusion lists for query ids or patterns to avoid noisy diagnostics.

### 7) Developer tooling

- CLI / JBang helper to render + pretty-print queries outside an app.
- Examples showing how to plug a custom logger/tracer without Spring.

## Open Questions

- Do we ship Micrometer/OTel as optional deps in a separate module, or rely purely on SPI for users to bridge?
- Should param masking be default-on for common sensitive names (password, token, email) or opt-in?
- Do we support actuator endpoints by default or keep them in a demo-only profile?

## Next Steps

- Implement the SQL logger hook + default SLF4J implementation.
- Add optional Micrometer/OTel module with timers/spans for render/compile/bind.
- Create Spring Boot starter auto-config wiring (with properties for masking, sampling, pretty-print).
- Add a validation-on-startup bean consuming `CompiledQuery` beans; export metrics.
- Document usage with code samples and property tables.
