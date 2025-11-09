package org.in.media.res.sqlBuilder.api.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable pair holding the rendered SQL string (with {@code ?} placeholders) and the list of
 * bound parameter values in placeholder order.
 */
public final class SqlAndParams {

    private final String sql;
    private final List<Object> params;

    public SqlAndParams(String sql, List<Object> params) {
        this.sql = Objects.requireNonNull(sql, "sql");
        Objects.requireNonNull(params, "params");
        this.params = Collections.unmodifiableList(new ArrayList<>(params));
    }

    public String sql() {
        return sql;
    }

    public List<Object> params() {
        return Collections.unmodifiableList(params);
    }
}
