package org.in.media.res.sqlBuilder.core.query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.CompiledQuery;
import org.in.media.res.sqlBuilder.api.query.Condition;
import org.in.media.res.sqlBuilder.api.query.ConditionValue;
import org.in.media.res.sqlBuilder.api.query.Dialect;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.RawSqlFragment;
import org.in.media.res.sqlBuilder.api.query.SqlAndParams;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.api.query.UpdateQuery;
import org.in.media.res.sqlBuilder.core.query.predicate.ConditionGroup;
import org.in.media.res.sqlBuilder.api.query.spi.SetClause;
import org.in.media.res.sqlBuilder.api.query.spi.Where;
import org.in.media.res.sqlBuilder.core.query.dialect.DialectContext;
import org.in.media.res.sqlBuilder.core.query.dialect.Dialects;

public final class UpdateQueryImpl implements UpdateQuery {

	private final Dialect dialect;
	private final Table target;
	private final SetClause setClause;
	private final Where whereClause;

	private UpdateQueryImpl(Table target, Dialect dialect) {
		this.target = Objects.requireNonNull(target, "target");
		this.dialect = Objects.requireNonNull(dialect, "dialect");
		this.setClause = ClauseFactory.instantiateSet(dialect);
		this.whereClause = ClauseFactory.instantiateWhere(dialect);
	}

	public static UpdateQuery update(Table table) {
		return new UpdateQueryImpl(table, Dialects.defaultDialect());
	}

	public static UpdateQuery update(Table table, Dialect dialect) {
		return new UpdateQueryImpl(table, dialect);
	}

	@Override
	public Table target() {
		return target;
	}

	@Override
	public UpdateQuery set(Column column, Object value) {
		this.setClause.set(column, value);
		return this;
	}

	@Override
	public UpdateQuery set(Column column, SqlParameter<?> parameter) {
		this.setClause.set(column, parameter);
		return this;
	}

	@Override
	public UpdateQuery setNull(Column column) {
		this.setClause.setNull(column);
		return this;
	}

	@Override
	public UpdateQuery setRaw(String sql) {
		this.setClause.setRaw(sql);
		return this;
	}

	@Override
	public UpdateQuery setRaw(String sql, SqlParameter<?>... params) {
		this.setClause.setRaw(sql, params);
		return this;
	}

	@Override
	public UpdateQuery setRaw(RawSqlFragment fragment) {
		this.setClause.setRaw(fragment);
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
		if (setClause.assignments().isEmpty()) {
			throw new IllegalStateException("UPDATE statements require at least one set(...) call");
		}
		try (DialectContext.Scope ignored = DialectContext.scope(dialect)) {
			StringBuilder builder = new StringBuilder("UPDATE ");
			appendTarget(builder);
			builder.append(setClause.transpile());
			builder.append(whereClause.transpile());
			return builder.toString();
		}
	}

	private void appendTarget(StringBuilder builder) {
		builder.append(DialectContext.current().quoteIdent(target.getName()));
		if (target.hasAlias()) {
			builder.append(' ').append(DialectContext.current().quoteIdent(target.getAlias()));
		}
	}

	@Override
	public UpdateQuery where(Condition condition) {
		return withWhere(where -> where.condition(condition));
	}

	@Override
	public UpdateQuery condition(Condition condition) {
		return withWhere(where -> where.condition(condition));
	}

	@Override
	public UpdateQuery where(Column column) {
		return withWhere(where -> where.where(column));
	}

	@Override
	public UpdateQuery whereRaw(String sql) {
		return withWhere(where -> where.whereRaw(sql));
	}

	@Override
	public UpdateQuery whereRaw(String sql, SqlParameter<?>... params) {
		return withWhere(where -> where.whereRaw(sql, params));
	}

	@Override
	public UpdateQuery whereRaw(RawSqlFragment fragment) {
		return withWhere(where -> where.whereRaw(fragment));
	}

	@Override
	public UpdateQuery and(Condition condition) {
		return withWhere(where -> where.and(condition));
	}

	@Override
	public UpdateQuery or(Condition condition) {
		return withWhere(where -> where.or(condition));
	}

	@Override
	public UpdateQuery and() {
		return withWhere(Where::and);
	}

	@Override
	public UpdateQuery or() {
		return withWhere(Where::or);
	}

	@Override
	public UpdateQuery and(Column column) {
		return withWhere(where -> where.and(column));
	}

	@Override
	public UpdateQuery or(Column column) {
		return withWhere(where -> where.or(column));
	}

	@Override
	public UpdateQuery andRaw(String sql) {
		return withWhere(where -> where.andRaw(sql));
	}

	@Override
	public UpdateQuery andRaw(String sql, SqlParameter<?>... params) {
		return withWhere(where -> where.andRaw(sql, params));
	}

	@Override
	public UpdateQuery andRaw(RawSqlFragment fragment) {
		return withWhere(where -> where.andRaw(fragment));
	}

	@Override
	public UpdateQuery orRaw(String sql) {
		return withWhere(where -> where.orRaw(sql));
	}

	@Override
	public UpdateQuery orRaw(String sql, SqlParameter<?>... params) {
		return withWhere(where -> where.orRaw(sql, params));
	}

	@Override
	public UpdateQuery orRaw(RawSqlFragment fragment) {
		return withWhere(where -> where.orRaw(fragment));
	}

	@Override
	public UpdateQuery eq() {
		return withWhere(Where::eq);
	}

	@Override
	public UpdateQuery supTo() {
		return withWhere(Where::supTo);
	}

	@Override
	public UpdateQuery infTo() {
		return withWhere(Where::infTo);
	}

	@Override
	public UpdateQuery supOrEqTo() {
		return withWhere(Where::supOrEqTo);
	}

	@Override
	public UpdateQuery infOrEqTo() {
		return withWhere(Where::infOrEqTo);
	}

	@Override
	public UpdateQuery in() {
		return withWhere(Where::in);
	}

	@Override
	public UpdateQuery eq(Column column) {
		return withWhere(where -> where.eq(column));
	}

	@Override
	public UpdateQuery notEq(Column column) {
		return withWhere(where -> where.notEq(column));
	}

	@Override
	public UpdateQuery supTo(Column column) {
		return withWhere(where -> where.supTo(column));
	}

	@Override
	public UpdateQuery infTo(Column column) {
		return withWhere(where -> where.infTo(column));
	}

	@Override
	public UpdateQuery supOrEqTo(Column column) {
		return withWhere(where -> where.supOrEqTo(column));
	}

	@Override
	public UpdateQuery infOrEqTo(Column column) {
		return withWhere(where -> where.infOrEqTo(column));
	}

	@Override
	public UpdateQuery eq(String value) {
		return withWhere(where -> where.eq(value));
	}

	@Override
	public UpdateQuery notEq(String value) {
		return withWhere(where -> where.notEq(value));
	}

	@Override
	public UpdateQuery like(String value) {
		return withWhere(where -> where.like(value));
	}

	@Override
	public UpdateQuery notLike(String value) {
		return withWhere(where -> where.notLike(value));
	}

	@Override
	public UpdateQuery eq(SqlParameter<?> parameter) {
		return withWhere(where -> where.eq(parameter));
	}

	@Override
	public UpdateQuery notEq(SqlParameter<?> parameter) {
		return withWhere(where -> where.notEq(parameter));
	}

	@Override
	public UpdateQuery like(SqlParameter<?> parameter) {
		return withWhere(where -> where.like(parameter));
	}

	@Override
	public UpdateQuery notLike(SqlParameter<?> parameter) {
		return withWhere(where -> where.notLike(parameter));
	}

	@Override
	public UpdateQuery between(String lower, String upper) {
		return withWhere(where -> where.between(lower, upper));
	}

	@Override
	public UpdateQuery between(SqlParameter<?> lower, SqlParameter<?> upper) {
		return withWhere(where -> where.between(lower, upper));
	}

	@Override
	public UpdateQuery supTo(String value) {
		return withWhere(where -> where.supTo(value));
	}

	@Override
	public UpdateQuery infTo(String value) {
		return withWhere(where -> where.infTo(value));
	}

	@Override
	public UpdateQuery supOrEqTo(String value) {
		return withWhere(where -> where.supOrEqTo(value));
	}

	@Override
	public UpdateQuery infOrEqTo(String value) {
		return withWhere(where -> where.infOrEqTo(value));
	}

	@Override
	public UpdateQuery supTo(SqlParameter<?> parameter) {
		return withWhere(where -> where.supTo(parameter));
	}

	@Override
	public UpdateQuery infTo(SqlParameter<?> parameter) {
		return withWhere(where -> where.infTo(parameter));
	}

	@Override
	public UpdateQuery supOrEqTo(SqlParameter<?> parameter) {
		return withWhere(where -> where.supOrEqTo(parameter));
	}

	@Override
	public UpdateQuery infOrEqTo(SqlParameter<?> parameter) {
		return withWhere(where -> where.infOrEqTo(parameter));
	}

	@Override
	public UpdateQuery in(String... value) {
		return withWhere(where -> where.in(value));
	}

	@Override
	public UpdateQuery notIn(String... value) {
		return withWhere(where -> where.notIn(value));
	}

	@Override
	public UpdateQuery eq(Integer value) {
		return withWhere(where -> where.eq(value));
	}

	@Override
	public UpdateQuery notEq(Integer value) {
		return withWhere(where -> where.notEq(value));
	}

	@Override
	public UpdateQuery between(Integer lower, Integer upper) {
		return withWhere(where -> where.between(lower, upper));
	}

	@Override
	public UpdateQuery supTo(Integer value) {
		return withWhere(where -> where.supTo(value));
	}

	@Override
	public UpdateQuery infTo(Integer value) {
		return withWhere(where -> where.infTo(value));
	}

	@Override
	public UpdateQuery supOrEqTo(Integer value) {
		return withWhere(where -> where.supOrEqTo(value));
	}

	@Override
	public UpdateQuery infOrEqTo(Integer value) {
		return withWhere(where -> where.infOrEqTo(value));
	}

	@Override
	public UpdateQuery in(Integer... value) {
		return withWhere(where -> where.in(value));
	}

	@Override
	public UpdateQuery notIn(Integer... value) {
		return withWhere(where -> where.notIn(value));
	}

	@Override
	public UpdateQuery eq(Date value) {
		return withWhere(where -> where.eq(value));
	}

	@Override
	public UpdateQuery notEq(Date value) {
		return withWhere(where -> where.notEq(value));
	}

	@Override
	public UpdateQuery between(Date lower, Date upper) {
		return withWhere(where -> where.between(lower, upper));
	}

	@Override
	public UpdateQuery supTo(Date value) {
		return withWhere(where -> where.supTo(value));
	}

	@Override
	public UpdateQuery infTo(Date value) {
		return withWhere(where -> where.infTo(value));
	}

	@Override
	public UpdateQuery supOrEqTo(Date value) {
		return withWhere(where -> where.supOrEqTo(value));
	}

	@Override
	public UpdateQuery infOrEqTo(Date value) {
		return withWhere(where -> where.infOrEqTo(value));
	}

	@Override
	public UpdateQuery in(Date... value) {
		return withWhere(where -> where.in(value));
	}

	@Override
	public UpdateQuery notIn(Date... value) {
		return withWhere(where -> where.notIn(value));
	}

	@Override
	public UpdateQuery eq(Double value) {
		return withWhere(where -> where.eq(value));
	}

	@Override
	public UpdateQuery notEq(Double value) {
		return withWhere(where -> where.notEq(value));
	}

	@Override
	public UpdateQuery between(Double lower, Double upper) {
		return withWhere(where -> where.between(lower, upper));
	}

	@Override
	public UpdateQuery supTo(Double value) {
		return withWhere(where -> where.supTo(value));
	}

	@Override
	public UpdateQuery infTo(Double value) {
		return withWhere(where -> where.infTo(value));
	}

	@Override
	public UpdateQuery supOrEqTo(Double value) {
		return withWhere(where -> where.supOrEqTo(value));
	}

	@Override
	public UpdateQuery infOrEqTo(Double value) {
		return withWhere(where -> where.infOrEqTo(value));
	}

	@Override
	public UpdateQuery in(Double... value) {
		return withWhere(where -> where.in(value));
	}

	@Override
	public UpdateQuery notIn(Double... value) {
		return withWhere(where -> where.notIn(value));
	}

	@Override
	public UpdateQuery isNull() {
		return withWhere(Where::isNull);
	}

	@Override
	public UpdateQuery isNotNull() {
		return withWhere(Where::isNotNull);
	}

	@Override
	public UpdateQuery eq(Query subquery) {
		return withWhere(where -> where.eq(subquery));
	}

	@Override
	public UpdateQuery notEq(Query subquery) {
		return withWhere(where -> where.notEq(subquery));
	}

	@Override
	public UpdateQuery supTo(Query subquery) {
		return withWhere(where -> where.supTo(subquery));
	}

	@Override
	public UpdateQuery infTo(Query subquery) {
		return withWhere(where -> where.infTo(subquery));
	}

	@Override
	public UpdateQuery supOrEqTo(Query subquery) {
		return withWhere(where -> where.supOrEqTo(subquery));
	}

	@Override
	public UpdateQuery infOrEqTo(Query subquery) {
		return withWhere(where -> where.infOrEqTo(subquery));
	}

	@Override
	public UpdateQuery in(Query subquery) {
		return withWhere(where -> where.in(subquery));
	}

	@Override
	public UpdateQuery notIn(Query subquery) {
		return withWhere(where -> where.notIn(subquery));
	}

	@Override
	public UpdateQuery exists(Query subquery) {
		return withWhere(where -> where.exists(subquery));
	}

	@Override
	public UpdateQuery notExists(Query subquery) {
		return withWhere(where -> where.notExists(subquery));
	}

	private UpdateQuery withWhere(Consumer<Where> consumer) {
		consumer.accept(this.whereClause);
		return this;
	}

	private List<CompiledQuery.Placeholder> collectPlaceholders() {
		List<CompiledQuery.Placeholder> placeholders = new ArrayList<>();
		appendSetPlaceholders(placeholders);
		appendConditionPlaceholders(whereClause.conditions(), placeholders);
		return placeholders;
	}

	private void appendSetPlaceholders(List<CompiledQuery.Placeholder> placeholders) {
		for (SetClause.Assignment assignment : setClause.assignments()) {
			if (assignment.isRaw()) {
				appendRawFragment(assignment.rawFragment(), placeholders);
				continue;
			}
			SetClause.AssignmentValue value = assignment.value();
			if (value.isParameter()) {
				placeholders.add(new CompiledQuery.Placeholder(value.parameter(), null));
			} else {
				if (value.literal() != null) {
					placeholders.add(new CompiledQuery.Placeholder(null, value.literal()));
				}
			}
		}
	}

	private void appendConditionPlaceholders(List<Condition> conditions,
			List<CompiledQuery.Placeholder> placeholders) {
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

	private void appendRawFragment(RawSqlFragment fragment, List<CompiledQuery.Placeholder> placeholders) {
		if (fragment == null) {
			return;
		}
		for (SqlParameter<?> parameter : fragment.parameters()) {
			placeholders.add(new CompiledQuery.Placeholder(parameter, null));
		}
	}
}
