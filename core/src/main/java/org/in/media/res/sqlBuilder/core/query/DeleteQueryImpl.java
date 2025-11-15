package org.in.media.res.sqlBuilder.core.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.CompiledQuery;
import org.in.media.res.sqlBuilder.api.query.Condition;
import org.in.media.res.sqlBuilder.api.query.ConditionValue;
import org.in.media.res.sqlBuilder.api.query.DeleteQuery;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.RawSqlFragment;
import org.in.media.res.sqlBuilder.api.query.SqlAndParams;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.api.query.spi.Where;
import org.in.media.res.sqlBuilder.core.query.dialect.DialectContext;
import org.in.media.res.sqlBuilder.core.query.dialect.Dialects;
import org.in.media.res.sqlBuilder.core.query.predicate.ConditionGroup;

public final class DeleteQueryImpl implements DeleteQuery {

	private final Table target;
	private final org.in.media.res.sqlBuilder.api.query.Dialect dialect;
	private final Where whereClause;

	private DeleteQueryImpl(Table target, org.in.media.res.sqlBuilder.api.query.Dialect dialect) {
		this.target = Objects.requireNonNull(target, "target");
		this.dialect = Objects.requireNonNull(dialect, "dialect");
		this.whereClause = ClauseFactory.instantiateWhere(dialect);
	}

	public static DeleteQuery from(Table table) {
		return new DeleteQueryImpl(table, Dialects.defaultDialect());
	}

	public static DeleteQuery from(Table table, org.in.media.res.sqlBuilder.api.query.Dialect dialect) {
		return new DeleteQueryImpl(table, dialect);
	}

	@Override
	public Table target() {
		return target;
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
		try (DialectContext.Scope ignored = DialectContext.scope(dialect)) {
			StringBuilder builder = new StringBuilder("DELETE FROM ");
			builder.append(DialectContext.current().quoteIdent(target.getName()));
			if (target.hasAlias()) {
				builder.append(' ').append(DialectContext.current().quoteIdent(target.getAlias()));
			}
			builder.append(whereClause.transpile());
			return builder.toString();
		}
	}

	@Override
	public DeleteQuery where(Condition condition) {
		return withWhere(where -> where.condition(condition));
	}

	@Override
	public DeleteQuery condition(Condition condition) {
		return withWhere(where -> where.condition(condition));
	}

	@Override
	public DeleteQuery where(Column column) {
		return withWhere(where -> where.where(column));
	}

	@Override
	public DeleteQuery whereRaw(String sql) {
		return withWhere(where -> where.whereRaw(sql));
	}

	@Override
	public DeleteQuery whereRaw(String sql, SqlParameter<?>... params) {
		return withWhere(where -> where.whereRaw(sql, params));
	}

	@Override
	public DeleteQuery whereRaw(RawSqlFragment fragment) {
		return withWhere(where -> where.whereRaw(fragment));
	}

	@Override
	public DeleteQuery and(Condition condition) {
		return withWhere(where -> where.and(condition));
	}

	@Override
	public DeleteQuery or(Condition condition) {
		return withWhere(where -> where.or(condition));
	}

	@Override
	public DeleteQuery and() {
		return withWhere(Where::and);
	}

	@Override
	public DeleteQuery or() {
		return withWhere(Where::or);
	}

	@Override
	public DeleteQuery and(Column column) {
		return withWhere(where -> where.and(column));
	}

	@Override
	public DeleteQuery or(Column column) {
		return withWhere(where -> where.or(column));
	}

	@Override
	public DeleteQuery andRaw(String sql) {
		return withWhere(where -> where.andRaw(sql));
	}

	@Override
	public DeleteQuery andRaw(String sql, SqlParameter<?>... params) {
		return withWhere(where -> where.andRaw(sql, params));
	}

	@Override
	public DeleteQuery andRaw(RawSqlFragment fragment) {
		return withWhere(where -> where.andRaw(fragment));
	}

	@Override
	public DeleteQuery orRaw(String sql) {
		return withWhere(where -> where.orRaw(sql));
	}

	@Override
	public DeleteQuery orRaw(String sql, SqlParameter<?>... params) {
		return withWhere(where -> where.orRaw(sql, params));
	}

	@Override
	public DeleteQuery orRaw(RawSqlFragment fragment) {
		return withWhere(where -> where.orRaw(fragment));
	}

	@Override
	public DeleteQuery eq() {
		return withWhere(Where::eq);
	}

	@Override
	public DeleteQuery supTo() {
		return withWhere(Where::supTo);
	}

	@Override
	public DeleteQuery infTo() {
		return withWhere(Where::infTo);
	}

	@Override
	public DeleteQuery supOrEqTo() {
		return withWhere(Where::supOrEqTo);
	}

	@Override
	public DeleteQuery infOrEqTo() {
		return withWhere(Where::infOrEqTo);
	}

	@Override
	public DeleteQuery in() {
		return withWhere(Where::in);
	}

	@Override
	public DeleteQuery eq(Column column) {
		return withWhere(where -> where.eq(column));
	}

	@Override
	public DeleteQuery notEq(Column column) {
		return withWhere(where -> where.notEq(column));
	}

	@Override
	public DeleteQuery supTo(Column column) {
		return withWhere(where -> where.supTo(column));
	}

	@Override
	public DeleteQuery infTo(Column column) {
		return withWhere(where -> where.infTo(column));
	}

	@Override
	public DeleteQuery supOrEqTo(Column column) {
		return withWhere(where -> where.supOrEqTo(column));
	}

	@Override
	public DeleteQuery infOrEqTo(Column column) {
		return withWhere(where -> where.infOrEqTo(column));
	}

	@Override
	public DeleteQuery eq(String value) {
		return withWhere(where -> where.eq(value));
	}

	@Override
	public DeleteQuery notEq(String value) {
		return withWhere(where -> where.notEq(value));
	}

	@Override
	public DeleteQuery like(String value) {
		return withWhere(where -> where.like(value));
	}

	@Override
	public DeleteQuery notLike(String value) {
		return withWhere(where -> where.notLike(value));
	}

	@Override
	public DeleteQuery eq(SqlParameter<?> parameter) {
		return withWhere(where -> where.eq(parameter));
	}

	@Override
	public DeleteQuery notEq(SqlParameter<?> parameter) {
		return withWhere(where -> where.notEq(parameter));
	}

	@Override
	public DeleteQuery like(SqlParameter<?> parameter) {
		return withWhere(where -> where.like(parameter));
	}

	@Override
	public DeleteQuery notLike(SqlParameter<?> parameter) {
		return withWhere(where -> where.notLike(parameter));
	}

	@Override
	public DeleteQuery between(String lower, String upper) {
		return withWhere(where -> where.between(lower, upper));
	}

	@Override
	public DeleteQuery between(SqlParameter<?> lower, SqlParameter<?> upper) {
		return withWhere(where -> where.between(lower, upper));
	}

	@Override
	public DeleteQuery supTo(String value) {
		return withWhere(where -> where.supTo(value));
	}

	@Override
	public DeleteQuery infTo(String value) {
		return withWhere(where -> where.infTo(value));
	}

	@Override
	public DeleteQuery supOrEqTo(String value) {
		return withWhere(where -> where.supOrEqTo(value));
	}

	@Override
	public DeleteQuery infOrEqTo(String value) {
		return withWhere(where -> where.infOrEqTo(value));
	}

	@Override
	public DeleteQuery supTo(SqlParameter<?> parameter) {
		return withWhere(where -> where.supTo(parameter));
	}

	@Override
	public DeleteQuery infTo(SqlParameter<?> parameter) {
		return withWhere(where -> where.infTo(parameter));
	}

	@Override
	public DeleteQuery supOrEqTo(SqlParameter<?> parameter) {
		return withWhere(where -> where.supOrEqTo(parameter));
	}

	@Override
	public DeleteQuery infOrEqTo(SqlParameter<?> parameter) {
		return withWhere(where -> where.infOrEqTo(parameter));
	}

	@Override
	public DeleteQuery in(String... value) {
		return withWhere(where -> where.in(value));
	}

	@Override
	public DeleteQuery notIn(String... value) {
		return withWhere(where -> where.notIn(value));
	}

	@Override
	public DeleteQuery eq(Integer value) {
		return withWhere(where -> where.eq(value));
	}

	@Override
	public DeleteQuery notEq(Integer value) {
		return withWhere(where -> where.notEq(value));
	}

	@Override
	public DeleteQuery between(Integer lower, Integer upper) {
		return withWhere(where -> where.between(lower, upper));
	}

	@Override
	public DeleteQuery supTo(Integer value) {
		return withWhere(where -> where.supTo(value));
	}

	@Override
	public DeleteQuery infTo(Integer value) {
		return withWhere(where -> where.infTo(value));
	}

	@Override
	public DeleteQuery supOrEqTo(Integer value) {
		return withWhere(where -> where.supOrEqTo(value));
	}

	@Override
	public DeleteQuery infOrEqTo(Integer value) {
		return withWhere(where -> where.infOrEqTo(value));
	}

	@Override
	public DeleteQuery in(Integer... value) {
		return withWhere(where -> where.in(value));
	}

	@Override
	public DeleteQuery notIn(Integer... value) {
		return withWhere(where -> where.notIn(value));
	}

	@Override
	public DeleteQuery eq(java.util.Date value) {
		return withWhere(where -> where.eq(value));
	}

	@Override
	public DeleteQuery notEq(java.util.Date value) {
		return withWhere(where -> where.notEq(value));
	}

	@Override
	public DeleteQuery between(java.util.Date lower, java.util.Date upper) {
		return withWhere(where -> where.between(lower, upper));
	}

	@Override
	public DeleteQuery supTo(java.util.Date value) {
		return withWhere(where -> where.supTo(value));
	}

	@Override
	public DeleteQuery infTo(java.util.Date value) {
		return withWhere(where -> where.infTo(value));
	}

	@Override
	public DeleteQuery supOrEqTo(java.util.Date value) {
		return withWhere(where -> where.supOrEqTo(value));
	}

	@Override
	public DeleteQuery infOrEqTo(java.util.Date value) {
		return withWhere(where -> where.infOrEqTo(value));
	}

	@Override
	public DeleteQuery in(java.util.Date... value) {
		return withWhere(where -> where.in(value));
	}

	@Override
	public DeleteQuery notIn(java.util.Date... value) {
		return withWhere(where -> where.notIn(value));
	}

	@Override
	public DeleteQuery eq(Double value) {
		return withWhere(where -> where.eq(value));
	}

	@Override
	public DeleteQuery notEq(Double value) {
		return withWhere(where -> where.notEq(value));
	}

	@Override
	public DeleteQuery between(Double lower, Double upper) {
		return withWhere(where -> where.between(lower, upper));
	}

	@Override
	public DeleteQuery supTo(Double value) {
		return withWhere(where -> where.supTo(value));
	}

	@Override
	public DeleteQuery infTo(Double value) {
		return withWhere(where -> where.infTo(value));
	}

	@Override
	public DeleteQuery supOrEqTo(Double value) {
		return withWhere(where -> where.supOrEqTo(value));
	}

	@Override
	public DeleteQuery infOrEqTo(Double value) {
		return withWhere(where -> where.infOrEqTo(value));
	}

	@Override
	public DeleteQuery in(Double... value) {
		return withWhere(where -> where.in(value));
	}

	@Override
	public DeleteQuery notIn(Double... value) {
		return withWhere(where -> where.notIn(value));
	}

	@Override
	public DeleteQuery isNull() {
		return withWhere(Where::isNull);
	}

	@Override
	public DeleteQuery isNotNull() {
		return withWhere(Where::isNotNull);
	}

	@Override
	public DeleteQuery eq(Query subquery) {
		return withWhere(where -> where.eq(subquery));
	}

	@Override
	public DeleteQuery notEq(Query subquery) {
		return withWhere(where -> where.notEq(subquery));
	}

	@Override
	public DeleteQuery supTo(Query subquery) {
		return withWhere(where -> where.supTo(subquery));
	}

	@Override
	public DeleteQuery infTo(Query subquery) {
		return withWhere(where -> where.infTo(subquery));
	}

	@Override
	public DeleteQuery supOrEqTo(Query subquery) {
		return withWhere(where -> where.supOrEqTo(subquery));
	}

	@Override
	public DeleteQuery infOrEqTo(Query subquery) {
		return withWhere(where -> where.infOrEqTo(subquery));
	}

	@Override
	public DeleteQuery in(Query subquery) {
		return withWhere(where -> where.in(subquery));
	}

	@Override
	public DeleteQuery notIn(Query subquery) {
		return withWhere(where -> where.notIn(subquery));
	}

	@Override
	public DeleteQuery exists(Query subquery) {
		return withWhere(where -> where.exists(subquery));
	}

	@Override
	public DeleteQuery notExists(Query subquery) {
		return withWhere(where -> where.notExists(subquery));
	}

	private DeleteQuery withWhere(Consumer<Where> consumer) {
		consumer.accept(this.whereClause);
		return this;
	}

	private List<CompiledQuery.Placeholder> collectPlaceholders() {
		List<CompiledQuery.Placeholder> placeholders = new ArrayList<>();
		appendConditionPlaceholders(whereClause.conditions(), placeholders);
		return placeholders;
	}

	private void appendConditionPlaceholders(List<Condition> conditions, List<CompiledQuery.Placeholder> placeholders) {
		for (Condition condition : conditions) {
			collectConditionPlaceholders(condition, placeholders);
		}
	}

	private void collectConditionPlaceholders(Condition condition, List<CompiledQuery.Placeholder> placeholders) {
		if (condition instanceof ConditionGroup group) {
			for (Condition child : group.children()) {
				collectConditionPlaceholders(child, placeholders);
			}
			return;
		}
		for (ConditionValue value : condition.values()) {
			appendValuePlaceholder(value, placeholders);
		}
	}

	private void appendValuePlaceholder(ConditionValue value, List<CompiledQuery.Placeholder> placeholders) {
		switch (value.type()) {
		case TY_SUBQUERY -> {
			Query nested = (Query) value.value();
			if (nested instanceof QueryImpl queryImpl) {
				placeholders.addAll(queryImpl.collectPlaceholders());
			} else {
				throw new IllegalStateException("Unsupported query implementation: " + nested.getClass());
			}
		}
		case TY_PARAM -> placeholders.add(new CompiledQuery.Placeholder((SqlParameter<?>) value.value(), null));
		default -> placeholders.add(new CompiledQuery.Placeholder(null, value.value()));
		}
	}
}
