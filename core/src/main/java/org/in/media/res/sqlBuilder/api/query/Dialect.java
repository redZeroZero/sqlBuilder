package org.in.media.res.sqlBuilder.api.query;

import java.util.Collections;
import java.util.List;

/**
 * Dialect abstraction responsible for emitting target-specific SQL fragments.
 */
public interface Dialect {

    /** Identifier mainly for debugging/logging purposes (e.g. "oracle"). */
    String id();

    /** Quote an identifier such as a table, column, or alias. */
    String quoteIdent(String raw);

    /** Character to use when rendering {@code ... LIKE ? ESCAPE '<char>'}. */
    char likeEscapeChar();

    /** Resolve the keyword/operator for EXCEPT/MINUS style set operations. */
    String exceptOperator(boolean all);

    /** Render a dialect-specific function call using already-rendered arguments. */
    String renderFunction(String logicalName, List<String> argsSql);

    /**
     * Render pagination fragment given limit/offset. Returning {@code PaginationClause.empty()}
     * indicates no fragment should be appended.
     */
    PaginationClause renderLimitOffset(Long limit, Long offset);

    record PaginationClause(String sql, List<Object> params) {
        public static PaginationClause empty() {
            return new PaginationClause("", List.of());
        }

        public PaginationClause {
            if (sql == null) {
                throw new IllegalArgumentException("sql must not be null");
            }
            params = Collections.unmodifiableList(params == null ? List.of() : params);
        }
    }
}
