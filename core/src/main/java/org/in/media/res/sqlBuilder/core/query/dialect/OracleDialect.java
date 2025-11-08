package org.in.media.res.sqlBuilder.core.query.dialect;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.in.media.res.sqlBuilder.api.query.Dialect;

final class OracleDialect implements Dialect {

    @Override
    public String id() {
        return "oracle";
    }

    @Override
    public String quoteIdent(String raw) {
        if (raw == null || raw.isBlank()) {
            return raw;
        }
        String escaped = raw.replace("\"", "\"\"");
        return '"' + escaped + '"';
    }

    @Override
    public char likeEscapeChar() {
        return '\\';
    }

    @Override
    public String exceptOperator(boolean all) {
        if (all) {
            throw new UnsupportedOperationException("Oracle does not support EXCEPT ALL / MINUS ALL");
        }
        return "MINUS";
    }

    @Override
    public String renderFunction(String logicalName, List<String> argsSql) {
        String normalized = logicalName == null ? "" : logicalName.toUpperCase(Locale.ROOT);
        String joinedArgs = String.join(", ", argsSql);
        return normalized + '(' + joinedArgs + ')';
    }

    @Override
    public PaginationClause renderLimitOffset(Long limit, Long offset) {
        if (limit == null && offset == null) {
            return PaginationClause.empty();
        }
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>(2);
        if (offset != null) {
            sql.append(" OFFSET ? ROWS");
            params.add(offset);
        }
        if (limit != null) {
            sql.append(" FETCH NEXT ? ROWS ONLY");
            params.add(limit);
        }
        return new PaginationClause(sql.toString(), params);
    }
}
