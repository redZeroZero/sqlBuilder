package org.in.media.res.sqlBuilder.core.query.dialect;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.in.media.res.sqlBuilder.api.query.Dialect;
import org.in.media.res.sqlBuilder.api.query.SetOperator;

final class PostgresDialect implements Dialect {

    @Override
    public String id() {
        return "postgres";
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
    public String setOperator(SetOperator operator) {
        return operator.sql();
    }

    @Override
    public String renderFunction(String logicalName, List<String> argsSql) {
        String normalized = logicalName == null ? "" : logicalName.toUpperCase(Locale.ROOT);
        return normalized + '(' + String.join(", ", argsSql) + ')';
    }

    @Override
    public PaginationClause renderLimitOffset(Long limit, Long offset) {
        if (limit == null && offset == null) {
            return PaginationClause.empty();
        }
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>(2);
        if (limit != null) {
            sql.append(" LIMIT ?");
            params.add(limit);
        }
        if (offset != null) {
            sql.append(" OFFSET ?");
            params.add(offset);
        }
        return new PaginationClause(sql.toString(), params);
    }
}
