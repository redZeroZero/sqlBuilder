package org.in.media.res.sqlBuilder.core.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.api.query.CompiledQuery;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.RawSql;
import org.in.media.res.sqlBuilder.api.query.RawSqlFragment;
import org.in.media.res.sqlBuilder.api.query.SqlAndParams;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.api.query.InsertQuery;
import org.in.media.res.sqlBuilder.core.query.dialect.DialectContext;
import org.in.media.res.sqlBuilder.core.query.dialect.Dialects;

public final class InsertQueryImpl implements InsertQuery {

	private final Table target;
	private final org.in.media.res.sqlBuilder.api.query.Dialect dialect;
	private final List<Column> columns = new ArrayList<>();
	private final List<Row> rows = new ArrayList<>();
	private QueryImpl selectQuery;

	private InsertQueryImpl(Table target, org.in.media.res.sqlBuilder.api.query.Dialect dialect) {
		this.target = Objects.requireNonNull(target, "target");
		this.dialect = Objects.requireNonNull(dialect, "dialect");
	}

	public static InsertQuery into(Table table) {
		return new InsertQueryImpl(table, Dialects.defaultDialect());
	}

	public static InsertQuery into(Table table, org.in.media.res.sqlBuilder.api.query.Dialect dialect) {
		return new InsertQueryImpl(table, dialect);
	}

	@Override
	public Table target() {
		return target;
	}

	@Override
	public InsertQuery columns(Column... columns) {
		if (columns == null || columns.length == 0) {
			throw new IllegalArgumentException("columns must not be empty");
		}
		if (!rows.isEmpty() || selectQuery != null) {
			throw new IllegalStateException("Columns cannot be modified after values or select() were added");
		}
		this.columns.addAll(Arrays.asList(columns));
		return this;
	}

	@Override
	public InsertQuery columns(TableDescriptor<?>... descriptors) {
		if (descriptors != null) {
			for (TableDescriptor<?> descriptor : descriptors) {
				columns(descriptor.column());
			}
		}
		return this;
	}

	@Override
	public InsertQuery values(Object... values) {
		requireColumnCount();
		requireSelectAbsent();
		if (values == null || values.length == 0) {
			throw new IllegalArgumentException("values must not be empty");
		}
		if (values.length != columns.size()) {
			throw new IllegalArgumentException("Expected " + columns.size() + " values but received " + values.length);
		}
		List<Value> rowValues = new ArrayList<>(values.length);
		for (Object value : values) {
			if (value instanceof SqlParameter<?> parameter) {
				rowValues.add(Value.parameter(parameter));
			} else {
				rowValues.add(Value.literal(value));
			}
		}
		rows.add(Row.values(rowValues));
		return this;
	}

	@Override
	public InsertQuery valuesRaw(String sql) {
		return valuesRaw(RawSql.of(sql));
	}

	@Override
	public InsertQuery valuesRaw(String sql, SqlParameter<?>... params) {
		return valuesRaw(RawSql.of(sql, params));
	}

	@Override
	public InsertQuery valuesRaw(RawSqlFragment fragment) {
		requireColumnCount();
		requireSelectAbsent();
		rows.add(Row.raw(fragment));
		return this;
	}

	@Override
	public InsertQuery select(Query query) {
		requireColumnCount();
		if (!rows.isEmpty()) {
			throw new IllegalStateException("select(...) cannot be combined with values(...)");
		}
		if (!(query instanceof QueryImpl queryImpl)) {
			throw new IllegalArgumentException("Unsupported Query implementation: " + query.getClass());
		}
		this.selectQuery = queryImpl;
		return this;
	}

	@Override
	public String transpile() {
		return buildSql();
	}

	@Override
	public SqlAndParams render() {
		String sql = buildSql();
		List<CompiledQuery.Placeholder> placeholders = collectPlaceholders();
		List<Object> params = new ArrayList<>(placeholders.size());
		for (CompiledQuery.Placeholder placeholder : placeholders) {
			if (placeholder.parameter() != null) {
				throw new IllegalStateException("Unbound parameter '" + placeholder.parameter().name()
						+ "'. Use compile().bind(...) instead.");
			}
			params.add(placeholder.fixedValue());
		}
		return new SqlAndParams(sql, params);
	}

	@Override
	public CompiledQuery compile() {
		return new CompiledQuery(buildSql(), collectPlaceholders());
	}

	private String buildSql() {
		requireColumnCount();
		if (rows.isEmpty() && selectQuery == null) {
			throw new IllegalStateException("INSERT requires at least one values(...) row or select(...) source");
		}
		try (DialectContext.Scope ignored = DialectContext.scope(dialect)) {
			StringBuilder builder = new StringBuilder("INSERT INTO ");
			appendTarget(builder);
			appendColumns(builder);
			if (selectQuery != null) {
				builder.append(' ').append(selectQuery.transpile());
			} else {
				appendValues(builder);
			}
			return builder.toString();
		}
	}

	private void appendTarget(StringBuilder builder) {
		builder.append(DialectContext.current().quoteIdent(target.getName()));
	}

	private void appendColumns(StringBuilder builder) {
		builder.append(" (");
		for (int i = 0; i < columns.size(); i++) {
			if (i > 0) {
				builder.append(", ");
			}
			builder.append(DialectContext.current().quoteIdent(columns.get(i).getName()));
		}
		builder.append(')');
	}

	private void appendValues(StringBuilder builder) {
		builder.append(" VALUES ");
		for (int i = 0; i < rows.size(); i++) {
			Row row = rows.get(i);
			if (row.isRaw()) {
				builder.append(row.rawFragment().sql());
			} else {
				builder.append('(');
				List<Value> values = row.values();
				for (int j = 0; j < values.size(); j++) {
					if (j > 0) {
						builder.append(", ");
					}
					Value value = values.get(j);
					if (!value.isParameter() && value.literal() == null) {
						builder.append("NULL");
					} else {
						builder.append('?');
					}
				}
				builder.append(')');
			}
			if (i < rows.size() - 1) {
				builder.append(", ");
			}
		}
	}

	private void requireColumnCount() {
		if (columns.isEmpty()) {
			throw new IllegalStateException("Call columns(...) before adding values or select()");
		}
	}

	private void requireSelectAbsent() {
		if (selectQuery != null) {
			throw new IllegalStateException("values(...) cannot be combined with select(...)");
		}
	}

	private List<CompiledQuery.Placeholder> collectPlaceholders() {
		List<CompiledQuery.Placeholder> placeholders = new ArrayList<>();
		for (Row row : rows) {
			if (row.isRaw()) {
				appendRawFragment(row.rawFragment(), placeholders);
				continue;
			}
			for (Value value : row.values()) {
				if (value.isParameter()) {
					placeholders.add(new CompiledQuery.Placeholder(value.parameter(), null));
				} else if (value.literal() != null) {
					placeholders.add(new CompiledQuery.Placeholder(null, value.literal()));
				}
			}
		}
		if (selectQuery != null) {
			placeholders.addAll(selectQuery.collectPlaceholders());
		}
		return placeholders;
	}

	private void appendRawFragment(RawSqlFragment fragment, List<CompiledQuery.Placeholder> placeholders) {
		if (fragment == null) {
			return;
		}
		for (SqlParameter<?> parameter : fragment.parameters()) {
			placeholders.add(new CompiledQuery.Placeholder(parameter, null));
		}
	}

	private record Row(List<Value> values, RawSqlFragment rawFragment) {
		static Row values(List<Value> values) {
			return new Row(List.copyOf(values), null);
		}

		static Row raw(RawSqlFragment fragment) {
			return new Row(List.of(), fragment);
		}

		boolean isRaw() {
			return rawFragment != null;
		}
	}

	private static final class Value {
		private final SqlParameter<?> parameter;
		private final Object literal;

		private Value(SqlParameter<?> parameter, Object literal) {
			this.parameter = parameter;
			this.literal = literal;
		}

		static Value parameter(SqlParameter<?> parameter) {
			return new Value(Objects.requireNonNull(parameter, "parameter"), null);
		}

		static Value literal(Object literal) {
			return new Value(null, literal);
		}

		boolean isParameter() {
			return parameter != null;
		}

		SqlParameter<?> parameter() {
			return parameter;
		}

		Object literal() {
			return literal;
		}
	}
}
