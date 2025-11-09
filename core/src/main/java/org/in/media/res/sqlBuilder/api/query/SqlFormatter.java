package org.in.media.res.sqlBuilder.api.query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

/** Utility that renders {@link SqlAndParams} with inline literal values. */
public final class SqlFormatter {

    private SqlFormatter() {
    }

    public static String inlineLiterals(SqlAndParams sqlAndParams, Dialect dialect) {
        String sql = sqlAndParams.sql();
        StringBuilder builder = new StringBuilder(sql.length() + 32 * sqlAndParams.params().size());
        Iterator<Object> params = sqlAndParams.params().iterator();
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == '?' && params.hasNext()) {
                builder.append(formatLiteral(params.next(), dialect));
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    private static String formatLiteral(Object value, Dialect dialect) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Date date) {
            return "'" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format(date) + "'";
        }
        if (value instanceof SqlParameter<?> parameter) {
            throw new IllegalArgumentException("Unbound parameter '" + parameter.name() + "'");
        }
        return "'" + value.toString().replace("'", "''") + "'";
    }
}
