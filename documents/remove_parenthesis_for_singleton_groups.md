## Title
Remove redundant parentheses when predicate groups contain a single condition.

## Background
- `ConditionGroup` is emitted whenever the DSL bundles predicates via `QueryHelper.group()` or when helper chains (e.g. `query.where(...).and(...)`) need to enforce precedence (`core/src/main/java/org/in/media/res/sqlBuilder/core/query/predicate/ConditionGroup.java:14`). Every group renders as `( ... )` regardless of how many child conditions it contains.
- `SqlFormatter.prettyPrint` preserves those parentheses and additionally formats WHERE clauses by splitting expressions but keeping each group literal (`core/src/main/java/org/in/media/res/sqlBuilder/api/query/SqlFormatter.java:63` and the predicate helpers around lines 188–340). When a group houses a single comparison, the resulting SQL shows superfluous parentheses, harming readability without improving semantics.
- We want to keep parentheses when they reinforce precedence (e.g., `AND (A OR B)`) or when the author explicitly requested a group, but drop them when the group was synthetic and only contains one predicate.

## Goals
1. Detect predicate groups that contain exactly one child and were not explicitly forced, and render them without wrapping parentheses.
2. Preserve parentheses for:
   - Groups with multiple children.
   - Any group created explicitly via `QueryHelper.group()`/`.andGroup()`/`.orGroup()` even if it eventually holds one predicate (for future-proofing and user intent).
3. Keep `SqlFormatter` aligned with the new raw SQL structure so `prettyPrint()` still mirrors actual rendering.

## Non-Goals
- Do not rewrite or reflow user-supplied raw SQL fragments.
- Do not change predicate evaluation semantics; only strip visual parentheses when provably redundant.
- No formatter option flags are introduced in this iteration; behavior should remain deterministic.

## Current Behavior Analysis
1. **Condition creation**
   - `QueryImpl.withWhere(...)` builds `ConditionGroup` instances via `ConditionGroupBuilder` whenever chained predicates or grouped helpers are used (`core/src/main/java/org/in/media/res/sqlBuilder/core/query/ConditionGroupBuilder.java:28`).
   - The builder does not distinguish between explicit groups vs. implicit scaffolding.
2. **Rendering**
   - `ConditionGroup.transpile()` unconditionally wraps children in parentheses (`ConditionGroup.java:47`), even when `children.size() == 1`.
   - Formatter sees `(column = ?)` from `transpile()` and preserves it (recent pretty-print logic even indents the inner expression).

## Proposed Design
### A. Track whether a group was explicitly requested
- Extend `ConditionGroup` (and the builder) with a boolean `forceParentheses` flag:
  - `true` when the developer called `QueryHelper.group()` (explicit intent).
  - `false` for implicit groups created by the builder simply to hold one predicate or to attach connectors.
- Modify `ConditionGroupBuilder` so:
  - DSL methods like `.group(...)` pass `forceParentheses = true`.
  - Internal builder use (e.g., chaining with `.and()`) defaults to false unless more than one child is added.
- Adjust `ConditionGroup.transpile()` to emit parentheses when:
  - `forceParentheses` is true, or
  - `children.size() > 1`.
  - For the single-child + `forceParentheses == false` case, just emit the child’s `transpile()` string, prepending the `startOperator` if present but skipping parentheses.

### B. Formatter alignment
- When parentheses are removed at the transpiler level, prettyPrint already sees flattened predicates, so no extra stripping is required.
- For explicit groups that remain (multiple children or forced), the existing formatter logic continues to indent them.

### C. Safeguards
- Provide unit tests ensuring:
  - Implicit single child groups now render without parentheses in both raw SQL (`QueryBehaviourTest.prettyPrintBreaksClausesAcrossLines`) and formatter output.
  - Explicit `QueryHelper.group()` with one predicate still renders parentheses.
  - Multi-predicate implicit groups retain parentheses.

## Implementation Steps
1. **ConditionGroup model changes**
   - Add `forceParentheses` field + accessor.
   - Update constructors and builder usage.
   - Adjust `transpile()` logic per rules above.
2. **ConditionGroupBuilder updates**
   - Track a `force` flag when constructed from `QueryHelper`.
   - When materializing the group, pass `forceParentheses = force || children.size() > 1`.
3. **QueryHelper and builder APIs**
   - Ensure `QueryHelper.group()` and `andGroup()` / `orGroup()` propagate `force = true`.
   - Review other constructors (e.g., `OptionalConditions`) to confirm expected behavior.
4. **Formatter regression tests**
   - Update expected strings in `examples/src/test/java/org/in/media/res/sqlBuilder/QueryReadmeExamplesTest.java` and `QueryBehaviourTest.java`.
   - Add core-level tests around `ConditionGroupBuilderTest` verifying parentheses behavior.
5. **Documentation**
   - Brief release note in README or `sqlbuilder-public-api-spec.md` explaining the formatting improvement (optional but recommended).

## Risks & Mitigations
- **Risk**: Removing parentheses might change semantics if a group was intended but ended up with one child. Mitigation: only strip when `forceParentheses` is false.
- **Risk**: Failing to propagate the `force` flag from every explicit grouping API could accidentally drop needed parentheses. Mitigation: audit all entry points to `ConditionGroupBuilder`, add tests for each helper (`QueryHelper.group`, `.andGroup`, `.orGroup`, optional filters).
- **Risk**: Formatter/tests mismatch if raw SQL still emits parentheses somewhere. Mitigation: run full suite (`mvn -pl core,examples test`) and inspect SQL samples in `README.md`.

## Acceptance Criteria
- Rendering a simple `WHERE column = ?` produced via `query.where(...).eq(...)` yields no extra parentheses in SQL or prettyPrint outputs.
- `WHERE (column = ?) AND (otherColumn = ?)` remains unchanged when groups are explicit or contain multiple children.
- All existing tests pass, and new assertions cover the singleton group scenarios.
