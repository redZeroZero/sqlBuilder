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

    /** Options controlling {@link #prettyPrint(String, PrettyPrintOptions)}. */
    public static final class PrettyPrintOptions {

        public enum IndentStyle {
            TABS, SPACES
        }

        private final int indentWidth;
        private final IndentStyle indentStyle;

        private PrettyPrintOptions(int indentWidth, IndentStyle indentStyle) {
            this.indentWidth = indentWidth;
            this.indentStyle = indentStyle;
        }

        public static PrettyPrintOptions tabs(int tabCount) {
            return new PrettyPrintOptions(Math.max(tabCount, 1), IndentStyle.TABS);
        }

        public static PrettyPrintOptions spaces(int width) {
            return new PrettyPrintOptions(Math.max(width, 1), IndentStyle.SPACES);
        }

        static PrettyPrintOptions defaultOptions() {
            return spaces(2);
        }

        String indentString(int level) {
            String unit = indentStyle == IndentStyle.TABS ? "\t" : " ".repeat(indentWidth);
            return unit.repeat(Math.max(level, 0));
        }
    }

    /** Pretty print SQL by inserting new lines/indentation around common clauses. */
    public static String prettyPrint(String sql) {
        return prettyPrint(sql, PrettyPrintOptions.defaultOptions());
    }

    public static String prettyPrint(String sql, PrettyPrintOptions options) {
        if (sql == null || sql.isBlank()) {
            return sql;
        }
        PrettyPrintOptions opts = options == null ? PrettyPrintOptions.defaultOptions() : options;
        StringBuilder builder = new StringBuilder(sql.length() + 32);
        int depth = 0;
        String upper = sql.toUpperCase(Locale.ROOT);
        for (int i = 0; i < sql.length(); ) {
            if (upper.startsWith("SELECT", i) && isClauseBoundary(upper, i)) {
                builder.append('\n').append(opts.indentString(depth)).append("SELECT");
                i += 6;
                continue;
            }
            if (upper.startsWith("WITH", i) && isClauseBoundary(upper, i)) {
                builder.append('\n').append(opts.indentString(depth)).append("WITH");
                i += 4;
                continue;
            }
            if (upper.startsWith("FROM", i) && isClauseBoundary(upper, i)) {
                builder.append('\n').append(opts.indentString(depth)).append("FROM");
                i += 4;
                continue;
            }
            if (upper.startsWith("WHERE", i) && isClauseBoundary(upper, i)) {
                builder.append('\n').append(opts.indentString(depth)).append("WHERE");
                i += 5;
                continue;
            }
            if (upper.startsWith("GROUP BY", i) && isClauseBoundary(upper, i)) {
                builder.append('\n').append(opts.indentString(depth)).append("GROUP BY");
                i += 8;
                continue;
            }
            if (upper.startsWith("HAVING", i) && isClauseBoundary(upper, i)) {
                builder.append('\n').append(opts.indentString(depth)).append("HAVING");
                i += 6;
                continue;
            }
            if (upper.startsWith("ORDER BY", i) && isClauseBoundary(upper, i)) {
                builder.append('\n').append(opts.indentString(depth)).append("ORDER BY");
                i += 8;
                continue;
            }
            if (upper.startsWith("UNION", i) || upper.startsWith("INTERSECT", i) || upper.startsWith("EXCEPT", i)) {
                if (isClauseBoundary(upper, i)) {
                    builder.append('\n').append(opts.indentString(depth)).append(sql, i, Math.min(sql.length(), i + nextKeywordLength(upper, i)));
                    i += nextKeywordLength(upper, i);
                    continue;
                }
            }
            char c = sql.charAt(i);
            builder.append(c);
            if (c == '(') {
                depth++;
                builder.append('\n').append(opts.indentString(depth));
            } else if (c == ')') {
                depth = Math.max(depth - 1, 0);
                if (i + 1 < sql.length()) {
                    builder.append('\n').append(opts.indentString(depth));
                }
            }
            i++;
        }
        return builder.toString();
    }

    private static boolean isClauseBoundary(String upperSql, int index) {
        if (index > 0 && Character.isLetterOrDigit(upperSql.charAt(index - 1))) {
            return false;
        }
        int end = index;
        while (end < upperSql.length() && Character.isLetter(upperSql.charAt(end))) {
            end++;
        }
        return end == upperSql.length() || Character.isWhitespace(upperSql.charAt(end))
                || upperSql.charAt(end) == '(';
    }

    private static int nextKeywordLength(String upperSql, int index) {
        int end = index;
        while (end < upperSql.length() && Character.isLetter(upperSql.charAt(end))) {
            end++;
        }
        return end - index;
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
