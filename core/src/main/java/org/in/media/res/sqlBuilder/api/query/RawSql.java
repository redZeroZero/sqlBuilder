package org.in.media.res.sqlBuilder.api.query;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Factory helpers for {@link RawSqlFragment}. Prefer the {@code of(...)}
 * overloads exposed here when calling the {@code *Raw(...)} DSL methods.
 */
public final class RawSql {

	private RawSql() {
	}

	public static RawSqlFragment of(String sql) {
		return of(sql, Collections.emptyList());
	}

	public static RawSqlFragment of(String sql, SqlParameter<?>... params) {
		return of(sql, Arrays.asList(params));
	}

	public static RawSqlFragment of(String sql, List<SqlParameter<?>> params) {
		return new SimpleRawSqlFragment(sql, params);
	}

	private static final class SimpleRawSqlFragment implements RawSqlFragment {
		private final String sql;
		private final List<SqlParameter<?>> parameters;

		private SimpleRawSqlFragment(String sql, List<SqlParameter<?>> parameters) {
			this.sql = Objects.requireNonNull(sql, "sql");
			this.parameters = List.copyOf(Objects.requireNonNull(parameters, "parameters"));
		}

		@Override
		public String sql() {
			return sql;
		}

		@Override
		public List<SqlParameter<?>> parameters() {
			return parameters;
		}
	}
}
