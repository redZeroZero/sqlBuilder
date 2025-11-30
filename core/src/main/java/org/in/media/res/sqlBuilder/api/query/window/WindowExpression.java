package org.in.media.res.sqlBuilder.api.query.window;

import java.util.Objects;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.query.RawSql;
import org.in.media.res.sqlBuilder.api.query.RawSqlFragment;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;

/**
 * Expression reference that can appear inside a window function (arguments,
 * partition list, or ordering). Supports column references or raw SQL when a
 * complex expression is required.
 */
public final class WindowExpression {

	private final Column column;
	private final RawSqlFragment raw;

	private WindowExpression(Column column, RawSqlFragment raw) {
		this.column = column;
		this.raw = raw;
	}

	public static WindowExpression of(Column column) {
		Objects.requireNonNull(column, "column");
		return new WindowExpression(column, null);
	}

	public static WindowExpression raw(String sql, SqlParameter<?>... params) {
		return raw(RawSql.of(sql, params));
	}

	public static WindowExpression raw(RawSqlFragment fragment) {
		Objects.requireNonNull(fragment, "fragment");
		return new WindowExpression(null, fragment);
	}

	public boolean isColumn() {
		return column != null;
	}

	public Column column() {
		return column;
	}

	public boolean isRaw() {
		return raw != null;
	}

	public RawSqlFragment rawFragment() {
		return raw;
	}
}
