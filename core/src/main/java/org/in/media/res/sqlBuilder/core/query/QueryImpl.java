package org.in.media.res.sqlBuilder.core.query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.SetOperator;
import org.in.media.res.sqlBuilder.constants.SortDirection;
import org.in.media.res.sqlBuilder.core.model.DerivedTableImpl;
import org.in.media.res.sqlBuilder.core.query.predicate.ConditionGroup;
import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.api.query.spi.Clause;
import org.in.media.res.sqlBuilder.api.query.CompiledQuery;
import org.in.media.res.sqlBuilder.api.query.Condition;
import org.in.media.res.sqlBuilder.api.query.ConditionValue;
import org.in.media.res.sqlBuilder.api.query.Dialect;
import org.in.media.res.sqlBuilder.api.query.spi.From;
import org.in.media.res.sqlBuilder.api.query.spi.GroupBy;
import org.in.media.res.sqlBuilder.api.query.spi.Having;
import org.in.media.res.sqlBuilder.api.query.HavingBuilder;
import org.in.media.res.sqlBuilder.api.query.QueryHavingBuilder;
import org.in.media.res.sqlBuilder.api.query.spi.Limit;
import org.in.media.res.sqlBuilder.api.query.spi.OrderBy;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.RawSqlFragment;
import org.in.media.res.sqlBuilder.api.query.spi.Select;
import org.in.media.res.sqlBuilder.api.query.SqlAndParams;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.api.query.spi.Where;
import org.in.media.res.sqlBuilder.core.query.dialect.DialectContext;
import org.in.media.res.sqlBuilder.core.query.dialect.Dialects;

public class QueryImpl implements Query {

	private final Dialect dialect;

	private Select selectClause;

	private From fromClause;

	private Where whereClause;

	private GroupBy groupByClause;

	private OrderBy orderByClause;

	private Having havingClause;

	private Limit limitClause;

	private final List<SetOperation> setOperations = new ArrayList<>();
	private List<CteDeclaration> ctes = List.of();

	private PredicateContext activePredicateContext = PredicateContext.WHERE;

	private static final Column STAR = StarColumn.INSTANCE;

	private Dialect.PaginationClause paginationClause = Dialect.PaginationClause.empty();

	public static Query newQuery() {
		return new QueryImpl(Dialects.defaultDialect());
	}

	public static Query newQuery(Dialect dialect) {
		return new QueryImpl(dialect);
	}

	public static Query newQuery(org.in.media.res.sqlBuilder.api.model.Schema schema) {
		return new QueryImpl(schema.getDialect());
	}

	private QueryImpl(Dialect dialect) {
		this.dialect = Objects.requireNonNull(dialect, "dialect");
		this.selectClause = ClauseFactory.instantiateSelect(dialect);
		this.fromClause = ClauseFactory.instantiateFrom(dialect);
		this.whereClause = ClauseFactory.instantiateWhere(dialect);
		this.groupByClause = ClauseFactory.instantiateGroupBy(dialect);
		this.orderByClause = ClauseFactory.instantiateOrderBy(dialect);
		this.havingClause = ClauseFactory.instantiateHaving(dialect);
		this.limitClause = ClauseFactory.instantiateLimit(dialect);
	}

	public static Query fromTable(Table table) {
		return newQuery().from(table);
	}

	public static Query selecting(Column... columns) {
		Query query = newQuery();
		query.select(columns);
		return query;
	}

	public static Query selectingDescriptors(TableDescriptor<?>... descriptors) {
		Query query = newQuery();
		query.select(descriptors);
		return query;
	}

	public static Query countAll() {
		return newQuery().count();
	}

	public static Table toTable(Query query, String alias, String... columnAliases) {
		return deriveTable(query, alias, columnAliases);
	}

	public static Table toTable(Query query) {
		return deriveTable(query, null);
	}

	private static Table deriveTable(Query query, String alias, String... columnAliases) {
		return new DerivedTableImpl(Objects.requireNonNull(query, "query"), alias, columnAliases);
	}

	private void registerBaseTable(Column column) {
		if (column != null && column.table() != null) {
			this.fromClause.from(column.table());
		}
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
				throw new IllegalStateException(
						"Unbound parameter '" + placeholder.parameter().name() + "'. Use compile().bind(...) instead.");
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
		StringBuilder builder = new StringBuilder();
		appendWithClauses(builder);
		builder.append(selectClause.transpile())
				.append(fromClause.transpile())
				.append(whereClause.transpile())
				.append(groupByClause.transpile())
				.append(havingClause.transpile())
				.append(orderByClause.transpile());

		this.paginationClause = dialect.renderLimitOffset(
				limitClause.limitValue() == null ? null : limitClause.limitValue().longValue(),
				limitClause.offsetValue() == null ? null : limitClause.offsetValue().longValue());
		builder.append(paginationClause.sql());

		for (SetOperation operation : setOperations) {
			builder.append(' ')
					.append(resolveSetOperator(operation.operator()))
					.append(' ')
					.append(parenthesize(operation.query()));
		}
		return builder.toString();
		}
	}

	public String prettyPrint() {
		String sql = transpile();
		return sql.replace(" FROM ", "\nFROM ")
				.replace(" WHERE ", "\nWHERE ")
				.replace(" GROUP BY ", "\nGROUP BY ")
				.replace(" HAVING ", "\nHAVING ")
				.replace(" ORDER BY ", "\nORDER BY ")
				.replace(" OFFSET ", "\nOFFSET ")
				.replace(" FETCH ", "\nFETCH ");
	}

	public void reset() {
		this.selectClause = ClauseFactory.instantiateSelect(dialect);
		this.fromClause = ClauseFactory.instantiateFrom(dialect);
		this.whereClause = ClauseFactory.instantiateWhere(dialect);
		this.groupByClause = ClauseFactory.instantiateGroupBy(dialect);
		this.orderByClause = ClauseFactory.instantiateOrderBy(dialect);
		this.havingClause = ClauseFactory.instantiateHaving(dialect);
		this.limitClause = ClauseFactory.instantiateLimit(dialect);
		this.setOperations.clear();
		this.ctes = List.of();
	}

	@Override
	public Query select(Column column) {
		registerBaseTable(column);
		this.selectClause.select(column);
		return this;
	}

	@Override
	public Query select(TableDescriptor<?> descriptor) {
		return this.select(descriptor.column());
	}

	@Override
	public Query select(Column... columns) {
		this.selectClause.select(columns);
		for (Column column : columns)
			registerBaseTable(column);
		return this;
	}

	@Override
	public Query select(TableDescriptor<?>... descriptors) {
		for (TableDescriptor<?> descriptor : descriptors)
			this.select(descriptor);
		return this;
	}

	@Override
	public Query select(Table table) {
		this.fromClause.from(table);
		this.selectClause.select(table);
		return this;
	}

	@Override
	public Query select(AggregateOperator agg, Column column) {
		registerBaseTable(column);
		this.selectClause.select(agg, column);
		return this;
	}

	@Override
	public Query selectRaw(String sql) {
		this.selectClause.selectRaw(sql);
		return this;
	}

	@Override
	public Query selectRaw(String sql, SqlParameter<?>... params) {
		this.selectClause.selectRaw(sql, params);
		return this;
	}

	@Override
	public Query selectRaw(RawSqlFragment fragment) {
		this.selectClause.selectRaw(fragment);
		return this;
	}

	@Override
	public Query hint(String hintSql) {
		this.selectClause.hint(hintSql);
		return this;
	}

	@Override
	public List<String> hints() {
		return this.selectClause.hints();
	}

	@Override
	public Query select(AggregateOperator agg, TableDescriptor<?> descriptor) {
		return this.select(agg, descriptor.column());
	}

	@Override
	public Query distinct() {
		this.selectClause.distinct();
		return this;
	}

	public Query count() {
		this.selectClause.select(AggregateOperator.COUNT, STAR);
		return this;
	}

	public Query count(Column column) {
		this.select(AggregateOperator.COUNT, column);
		return this;
	}

	public Query count(TableDescriptor<?> descriptor) {
		return count(descriptor.column());
	}

	@Override
	public Query union(Query other) {
		return appendSetOperation(SetOperator.UNION, other);
	}

	@Override
	public Query unionAll(Query other) {
		return appendSetOperation(SetOperator.UNION_ALL, other);
	}

	@Override
	public Query intersect(Query other) {
		return appendSetOperation(SetOperator.INTERSECT, other);
	}

	@Override
	public Query intersectAll(Query other) {
		return appendSetOperation(SetOperator.INTERSECT_ALL, other);
	}

	@Override
	public Query except(Query other) {
		return appendSetOperation(SetOperator.EXCEPT, other);
	}

	@Override
	public Query exceptAll(Query other) {
		return appendSetOperation(SetOperator.EXCEPT_ALL, other);
	}

	@Override
	public Query on(Column c1, Column c2) {
		this.fromClause.on(c1, c2);
		return this;
	}

	@Override
	public Query on(TableDescriptor<?> left, TableDescriptor<?> right) {
		return this.on(left.column(), right.column());
	}

	@Override
	public Query from(Table table) {
		this.fromClause.from(table);
		return this;
	}

	@Override
	public Query from(Table... tables) {
		this.fromClause.from(tables);
		return this;
	}

	@Override
	public Query join(Table t) {
		this.fromClause.join(t);
		return this;
	}

	@Override
	public Query innerJoin(Table t) {
		this.fromClause.innerJoin(t);
		return this;
	}

	@Override
	public Query leftJoin(Table t) {
		this.fromClause.leftJoin(t);
		return this;
	}

	@Override
	public Query rightJoin(Table t) {
		this.fromClause.rightJoin(t);
		return this;
	}

	@Override
	public Query crossJoin(Table t) {
		this.fromClause.crossJoin(t);
		return this;
	}

	@Override
	public Query fromRaw(String sql) {
		this.fromClause.fromRaw(sql);
		return this;
	}

	@Override
	public Query fromRaw(String sql, SqlParameter<?>... params) {
		this.fromClause.fromRaw(sql, params);
		return this;
	}

	@Override
	public Query fromRaw(RawSqlFragment fragment) {
		this.fromClause.fromRaw(fragment);
		return this;
	}

	@Override
	public Query joinRaw(String sql) {
		this.fromClause.joinRaw(sql);
		return this;
	}

	@Override
	public Query joinRaw(String sql, SqlParameter<?>... params) {
		this.fromClause.joinRaw(sql, params);
		return this;
	}

	@Override
	public Query joinRaw(RawSqlFragment fragment) {
		this.fromClause.joinRaw(fragment);
		return this;
	}

	@Override
	public Query leftJoinRaw(String sql) {
		this.fromClause.leftJoinRaw(sql);
		return this;
	}

	@Override
	public Query leftJoinRaw(String sql, SqlParameter<?>... params) {
		this.fromClause.leftJoinRaw(sql, params);
		return this;
	}

	@Override
	public Query leftJoinRaw(RawSqlFragment fragment) {
		this.fromClause.leftJoinRaw(fragment);
		return this;
	}

	@Override
	public Query rightJoinRaw(String sql) {
		this.fromClause.rightJoinRaw(sql);
		return this;
	}

	@Override
	public Query rightJoinRaw(String sql, SqlParameter<?>... params) {
		this.fromClause.rightJoinRaw(sql, params);
		return this;
	}

	@Override
	public Query rightJoinRaw(RawSqlFragment fragment) {
		this.fromClause.rightJoinRaw(fragment);
		return this;
	}

	@Override
	public Query crossJoinRaw(String sql) {
		this.fromClause.crossJoinRaw(sql);
		return this;
	}

	@Override
	public Query crossJoinRaw(String sql, SqlParameter<?>... params) {
		this.fromClause.crossJoinRaw(sql, params);
		return this;
	}

	@Override
	public Query crossJoinRaw(RawSqlFragment fragment) {
		this.fromClause.crossJoinRaw(fragment);
		return this;
	}

	@Override
	public Query fullOuterJoinRaw(String sql) {
		this.fromClause.fullOuterJoinRaw(sql);
		return this;
	}

	@Override
	public Query fullOuterJoinRaw(String sql, SqlParameter<?>... params) {
		this.fromClause.fullOuterJoinRaw(sql, params);
		return this;
	}

	@Override
	public Query fullOuterJoinRaw(RawSqlFragment fragment) {
		this.fromClause.fullOuterJoinRaw(fragment);
		return this;
	}

	@Override
	public Query fullOuterJoin(Table t) {
		this.fromClause.fullOuterJoin(t);
		return this;
	}

	@Override
	public List<Clause> clauses() {
		List<Clause> c = new ArrayList<>();
		c.add(selectClause);
		c.add(fromClause);
		c.add(whereClause);
		c.add(groupByClause);
		c.add(havingClause);
		c.add(orderByClause);
		c.add(limitClause);
		return c;
	}

	@Override
	public Table as(String alias, String... columnAliases) {
		return deriveTable(this, alias, columnAliases);
	}

	@Override
	public Query where(Condition condition) {
		return withWhere(where -> where.condition(condition));
	}

	@Override
	public Query where(Column column) {
		return withWhere(where -> where.where(column));
	}

	@Override
	public Query whereRaw(String sql) {
		return withWhere(where -> where.whereRaw(sql));
	}

	@Override
	public Query whereRaw(String sql, SqlParameter<?>... params) {
		return withWhere(where -> where.whereRaw(sql, params));
	}

	@Override
	public Query whereRaw(RawSqlFragment fragment) {
		return withWhere(where -> where.whereRaw(fragment));
	}

	@Override
	public Query eq(Column column) {
		return withWhere(where -> where.eq(column));
	}

	@Override
	public Query notEq(Column column) {
		return withWhere(where -> where.notEq(column));
	}

	@Override
	public Query supTo(Column column) {
		return withWhere(where -> where.supTo(column));
	}

	@Override
	public Query infTo(Column column) {
		return withWhere(where -> where.infTo(column));
	}

	@Override
	public Query supOrEqTo(Column column) {
		return withWhere(where -> where.supOrEqTo(column));
	}

	@Override
	public Query infOrEqTo(Column column) {
		return withWhere(where -> where.infOrEqTo(column));
	}

	@Override
	public Query eq(String value) {
		return withWhere(where -> where.eq(value));
	}

	@Override
	public Query notEq(String value) {
		return withWhere(where -> where.notEq(value));
	}

	@Override
	public Query eq(SqlParameter<?> parameter) {
		return withWhere(where -> where.eq(parameter));
	}

	@Override
	public Query notEq(SqlParameter<?> parameter) {
		return withWhere(where -> where.notEq(parameter));
	}

	@Override
	public Query like(String value) {
		return withWhere(where -> where.like(value));
	}

	@Override
	public Query like(SqlParameter<?> parameter) {
		return withWhere(where -> where.like(parameter));
	}

	@Override
	public Query notLike(String value) {
		return withWhere(where -> where.notLike(value));
	}

	@Override
	public Query notLike(SqlParameter<?> parameter) {
		return withWhere(where -> where.notLike(parameter));
	}

	@Override
	public Query between(String lower, String upper) {
		return withWhere(where -> where.between(lower, upper));
	}

	@Override
	public Query between(SqlParameter<?> lower, SqlParameter<?> upper) {
		return withWhere(where -> where.between(lower, upper));
	}

	@Override
	public Query supTo(String value) {
		return withWhere(where -> where.supTo(value));
	}

	@Override
	public Query infTo(String value) {
		return withWhere(where -> where.infTo(value));
	}

	@Override
	public Query supOrEqTo(String value) {
		return withWhere(where -> where.supOrEqTo(value));
	}

	@Override
	public Query infOrEqTo(String value) {
		return withWhere(where -> where.infOrEqTo(value));
	}

	@Override
	public Query supTo(SqlParameter<?> parameter) {
		return withWhere(where -> where.supTo(parameter));
	}

	@Override
	public Query infTo(SqlParameter<?> parameter) {
		return withWhere(where -> where.infTo(parameter));
	}

	@Override
	public Query supOrEqTo(SqlParameter<?> parameter) {
		return withWhere(where -> where.supOrEqTo(parameter));
	}

	@Override
	public Query infOrEqTo(SqlParameter<?> parameter) {
		return withWhere(where -> where.infOrEqTo(parameter));
	}

	@Override
	public Query in(String... value) {
		return withWhere(where -> where.in(value));
	}

	@Override
	public Query notIn(String... value) {
		return withWhere(where -> where.notIn(value));
	}

	@Override
	public Query eq(Integer value) {
		return withWhere(where -> where.eq(value));
	}

	@Override
	public Query notEq(Integer value) {
		return withWhere(where -> where.notEq(value));
	}

	@Override
	public Query between(Integer lower, Integer upper) {
		return withWhere(where -> where.between(lower, upper));
	}

	@Override
	public Query supTo(Integer value) {
		return withWhere(where -> where.supTo(value));
	}

	@Override
	public Query infTo(Integer value) {
		return withWhere(where -> where.infTo(value));
	}

	@Override
	public Query supOrEqTo(Integer value) {
		return withWhere(where -> where.supOrEqTo(value));
	}

	@Override
	public Query infOrEqTo(Integer value) {
		return withWhere(where -> where.infOrEqTo(value));
	}

	@Override
	public Query in(Integer... value) {
		return withWhere(where -> where.in(value));
	}

	@Override
	public Query notIn(Integer... value) {
		return withWhere(where -> where.notIn(value));
	}

	@Override
	public Query eq(Date value) {
		return withWhere(where -> where.eq(value));
	}

	@Override
	public Query notEq(Date value) {
		return withWhere(where -> where.notEq(value));
	}

	@Override
	public Query between(Date lower, Date upper) {
		return withWhere(where -> where.between(lower, upper));
	}

	@Override
	public Query supTo(Date value) {
		return withWhere(where -> where.supTo(value));
	}

	@Override
	public Query infTo(Date value) {
		return withWhere(where -> where.infTo(value));
	}

	@Override
	public Query supOrEqTo(Date value) {
		return withWhere(where -> where.supOrEqTo(value));
	}

	@Override
	public Query infOrEqTo(Date value) {
		return withWhere(where -> where.infOrEqTo(value));
	}

	@Override
	public Query in(Date... value) {
		return withWhere(where -> where.in(value));
	}

	@Override
	public Query notIn(Date... value) {
		return withWhere(where -> where.notIn(value));
	}

	@Override
	public Query eq(Double value) {
		return withWhere(where -> where.eq(value));
	}

	@Override
	public Query notEq(Double value) {
		return withWhere(where -> where.notEq(value));
	}

	@Override
	public Query between(Double lower, Double upper) {
		return withWhere(where -> where.between(lower, upper));
	}

	@Override
	public Query supTo(Double value) {
		return withWhere(where -> where.supTo(value));
	}

	@Override
	public Query infTo(Double value) {
		return withWhere(where -> where.infTo(value));
	}

	@Override
	public Query supOrEqTo(Double value) {
		return withWhere(where -> where.supOrEqTo(value));
	}

	@Override
	public Query infOrEqTo(Double value) {
		return withWhere(where -> where.infOrEqTo(value));
	}

	@Override
	public Query in(Double... value) {
		return withWhere(where -> where.in(value));
	}

	@Override
	public Query notIn(Double... value) {
		return withWhere(where -> where.notIn(value));
	}

	@Override
	public Query isNull() {
		return withWhere(Where::isNull);
	}

	@Override
	public Query isNotNull() {
		return withWhere(Where::isNotNull);
	}

	@Override
	public Query eq(Query subquery) {
		return withWhere(where -> where.eq(subquery));
	}

	@Override
	public Query notEq(Query subquery) {
		return withWhere(where -> where.notEq(subquery));
	}

	@Override
	public Query in(Query subquery) {
		return withWhere(where -> where.in(subquery));
	}

	@Override
	public Query notIn(Query subquery) {
		return withWhere(where -> where.notIn(subquery));
	}

	@Override
	public Query supTo(Query subquery) {
		return withWhere(where -> where.supTo(subquery));
	}

	@Override
	public Query infTo(Query subquery) {
		return withWhere(where -> where.infTo(subquery));
	}

	@Override
	public Query supOrEqTo(Query subquery) {
		return withWhere(where -> where.supOrEqTo(subquery));
	}

	@Override
	public Query infOrEqTo(Query subquery) {
		return withWhere(where -> where.infOrEqTo(subquery));
	}

	@Override
	public Query exists(Query subquery) {
		return withWhere(where -> where.exists(subquery));
	}

	@Override
	public Query notExists(Query subquery) {
		return withWhere(where -> where.notExists(subquery));
	}

	@Override
	public Query and(Column column) {
		return withWhere(where -> where.and(column));
	}

	@Override
	public Query andRaw(String sql) {
		if (this.activePredicateContext == PredicateContext.HAVING) {
			this.havingClause.andRaw(sql);
			activateHaving();
			return this;
		}
		return withWhere(where -> where.andRaw(sql));
	}

	@Override
	public Query andRaw(String sql, SqlParameter<?>... params) {
		if (this.activePredicateContext == PredicateContext.HAVING) {
			this.havingClause.andRaw(sql, params);
			activateHaving();
			return this;
		}
		return withWhere(where -> where.andRaw(sql, params));
	}

	@Override
	public Query andRaw(RawSqlFragment fragment) {
		if (this.activePredicateContext == PredicateContext.HAVING) {
			this.havingClause.andRaw(fragment);
			activateHaving();
			return this;
		}
		return withWhere(where -> where.andRaw(fragment));
	}

	@Override
	public Query or(Column column) {
		return withWhere(where -> where.or(column));
	}

	@Override
	public Query orRaw(String sql) {
		if (this.activePredicateContext == PredicateContext.HAVING) {
			this.havingClause.orRaw(sql);
			activateHaving();
			return this;
		}
		return withWhere(where -> where.orRaw(sql));
	}

	@Override
	public Query orRaw(String sql, SqlParameter<?>... params) {
		if (this.activePredicateContext == PredicateContext.HAVING) {
			this.havingClause.orRaw(sql, params);
			activateHaving();
			return this;
		}
		return withWhere(where -> where.orRaw(sql, params));
	}

	@Override
	public Query orRaw(RawSqlFragment fragment) {
		if (this.activePredicateContext == PredicateContext.HAVING) {
			this.havingClause.orRaw(fragment);
			activateHaving();
			return this;
		}
		return withWhere(where -> where.orRaw(fragment));
	}

	@Override
	public Query and() {
		return withWhere(Where::and);
	}

	@Override
	public Query or() {
		return withWhere(Where::or);
	}

	@Override
	public Query eq() {
		return withWhere(Where::eq);
	}

	@Override
	public Query supTo() {
		return withWhere(Where::supTo);
	}

	@Override
	public Query infTo() {
		return withWhere(Where::infTo);
	}

	@Override
	public Query supOrEqTo() {
		return withWhere(Where::supOrEqTo);
	}

	@Override
	public Query infOrEqTo() {
		return withWhere(Where::infOrEqTo);
	}

	@Override
	public Query in() {
		return withWhere(Where::in);
	}

	@Override
	public Query min(Column column) {
		return withWhere(where -> where.min(column));
	}

	@Override
	public Query max(Column column) {
		return withWhere(where -> where.max(column));
	}

	@Override
	public Query sum(Column column) {
		return withWhere(where -> where.sum(column));
	}

	@Override
	public Query avg(Column column) {
		return withWhere(where -> where.avg(column));
	}

	@Override
	public Query col(Column column) {
		return withWhere(where -> where.col(column));
	}

	@Override
	public Query condition(Condition condition) {
		return where(condition);
	}

	@Override
	public List<Column> columns() {
		return this.selectClause.columns();
	}

	@Override
	public Map<Column, AggregateOperator> aggColumns() {
		return this.selectClause.aggColumns();
	}

	@Override
	public boolean isDistinct() {
		return this.selectClause.isDistinct();
	}

	@Override
	public Map<Table, JoinSpec> joins() {
		return this.fromClause.joins();
	}

	@Override
	public List<Condition> conditions() {
		return this.whereClause.conditions();
	}

	@Override
	public Query groupBy(Column column) {
		this.groupByClause.groupBy(column);
		return this;
	}

	@Override
	public Query groupBy(TableDescriptor<?> descriptor) {
		return this.groupBy(descriptor.column());
	}

	@Override
	public Query groupBy(Column... columns) {
		this.groupByClause.groupBy(columns);
		return this;
	}

	@Override
	public Query groupBy(TableDescriptor<?>... descriptors) {
		for (TableDescriptor<?> descriptor : descriptors)
			this.groupByClause.groupBy(descriptor.column());
		return this;
	}

	@Override
	public Query groupByRaw(String sql) {
		this.groupByClause.groupByRaw(sql);
		return this;
	}

	@Override
	public Query groupByRaw(String sql, SqlParameter<?>... params) {
		this.groupByClause.groupByRaw(sql, params);
		return this;
	}

	@Override
	public Query groupByRaw(RawSqlFragment fragment) {
		this.groupByClause.groupByRaw(fragment);
		return this;
	}

	@Override
	public List<Column> groupByColumns() {
		return this.groupByClause.groupByColumns();
	}

	@Override
	public Query orderBy(Column column) {
		this.orderByClause.orderBy(column);
		return this;
	}

	@Override
	public Query orderBy(TableDescriptor<?> descriptor) {
		return this.orderBy(descriptor.column());
	}

	@Override
	public Query orderBy(Column column, SortDirection direction) {
		this.orderByClause.orderBy(column, direction);
		return this;
	}

	@Override
	public Query orderBy(TableDescriptor<?> descriptor, SortDirection direction) {
		return this.orderBy(descriptor.column(), direction);
	}

	@Override
	public Query orderByRaw(String sql) {
		this.orderByClause.orderByRaw(sql);
		return this;
	}

	@Override
	public Query orderByRaw(String sql, SqlParameter<?>... params) {
		this.orderByClause.orderByRaw(sql, params);
		return this;
	}

	@Override
	public Query orderByRaw(RawSqlFragment fragment) {
		this.orderByClause.orderByRaw(fragment);
		return this;
	}

	@Override
	public Query asc(Column column) {
		this.orderByClause.asc(column);
		return this;
	}

	@Override
	public Query asc(TableDescriptor<?> descriptor) {
		return this.asc(descriptor.column());
	}

	@Override
	public Query desc(Column column) {
		this.orderByClause.desc(column);
		return this;
	}

	@Override
	public Query desc(TableDescriptor<?> descriptor) {
		return this.desc(descriptor.column());
	}

	@Override
	public List<OrderBy.Ordering> orderings() {
		return this.orderByClause.orderings();
	}

	@Override
	public Query having(Condition condition) {
		return withHaving(having -> having.having(condition));
	}

	@Override
	public Query and(Condition condition) {
		if (this.activePredicateContext == PredicateContext.HAVING) {
			return withHaving(having -> having.and(condition));
		}
		return withWhere(where -> where.and(condition));
	}

	@Override
	public Query or(Condition condition) {
		if (this.activePredicateContext == PredicateContext.HAVING) {
			return withHaving(having -> having.or(condition));
		}
		return withWhere(where -> where.or(condition));
	}

	@Override
	public QueryHavingBuilder having(Column column) {
		activateHaving();
		return new FluentHavingBuilder(this.havingClause.having(column));
	}

	@Override
	public Query havingRaw(String sql) {
		return withHaving(having -> having.havingRaw(sql));
	}

	@Override
	public Query havingRaw(String sql, SqlParameter<?>... params) {
		return withHaving(having -> having.havingRaw(sql, params));
	}

	@Override
	public Query havingRaw(RawSqlFragment fragment) {
		return withHaving(having -> having.havingRaw(fragment));
	}

	@Override
	public List<Condition> havingConditions() {
		return this.havingClause.havingConditions();
	}

	@Override
	public Query limit(int limit) {
		this.limitClause.limit(limit);
		return this;
	}

	@Override
	public Query offset(int offset) {
		this.limitClause.offset(offset);
		return this;
	}

	@Override
	public Query limitAndOffset(int limit, int offset) {
		this.limitClause.limitAndOffset(limit, offset);
		return this;
	}

	@Override
	public Integer limitValue() {
		return this.limitClause.limitValue();
	}

	@Override
	public Integer offsetValue() {
		return this.limitClause.offsetValue();
	}

	private Query appendSetOperation(SetOperator operator, Query other) {
		if (!(other instanceof QueryImpl queryImpl)) {
			throw new IllegalArgumentException("Unsupported Query implementation: " + other.getClass());
		}
		this.setOperations.add(new SetOperation(operator, queryImpl));
		return this;
	}

	private void activateWhere() {
		this.activePredicateContext = PredicateContext.WHERE;
	}

	private void activateHaving() {
		this.activePredicateContext = PredicateContext.HAVING;
	}

	private Query withWhere(Consumer<Where> consumer) {
		activateWhere();
		consumer.accept(this.whereClause);
		return this;
	}

	private Query withHaving(Consumer<Having> consumer) {
		activateHaving();
		consumer.accept(this.havingClause);
		return this;
	}

	private List<CompiledQuery.Placeholder> collectPlaceholders() {
		List<CompiledQuery.Placeholder> placeholders = new ArrayList<>();
		appendCtePlaceholders(placeholders);
		appendSelectPlaceholders(placeholders);
		appendFromPlaceholders(placeholders);
		appendConditionPlaceholders(whereClause.conditions(), placeholders);
		appendGroupByPlaceholders(placeholders);
		appendConditionPlaceholders(havingClause.havingConditions(), placeholders);
		appendOrderByPlaceholders(placeholders);
		appendLimitPlaceholders(placeholders);
		appendSetOperationPlaceholders(placeholders);
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

	private void appendSelectPlaceholders(List<CompiledQuery.Placeholder> placeholders) {
		if (selectClause instanceof SelectProjectionSupport support) {
			for (SelectProjectionSupport.SelectProjection projection : support.projections()) {
				if (projection.type() == SelectProjectionSupport.ProjectionType.RAW) {
					appendRawFragment(projection.fragment(), placeholders);
				}
			}
		}
	}

	private void appendFromPlaceholders(List<CompiledQuery.Placeholder> placeholders) {
		if (fromClause instanceof FromRawSupport rawSupport) {
			RawSqlFragment rawBase = rawSupport.rawBaseFragment();
			if (rawBase != null) {
				appendRawFragment(rawBase, placeholders);
			}
			for (FromRawSupport.RawJoinFragmentView joinFragment : rawSupport.rawJoinFragments()) {
				appendRawFragment(joinFragment.fragment(), placeholders);
			}
		}
	}

	private void appendGroupByPlaceholders(List<CompiledQuery.Placeholder> placeholders) {
		if (groupByClause instanceof GroupByRawSupport groupByRawSupport) {
			for (RawSqlFragment fragment : groupByRawSupport.groupByFragments()) {
				appendRawFragment(fragment, placeholders);
			}
		}
	}

	private void appendOrderByPlaceholders(List<CompiledQuery.Placeholder> placeholders) {
		if (orderByClause instanceof OrderByRawSupport orderByRawSupport) {
			for (RawSqlFragment fragment : orderByRawSupport.orderByFragments()) {
				appendRawFragment(fragment, placeholders);
			}
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

	private void appendLimitPlaceholders(List<CompiledQuery.Placeholder> placeholders) {
		for (Object value : paginationClause.params()) {
			placeholders.add(new CompiledQuery.Placeholder(null, value));
		}
	}

	private void appendSetOperationPlaceholders(List<CompiledQuery.Placeholder> placeholders) {
		for (SetOperation operation : setOperations) {
			placeholders.addAll(operation.query().collectPlaceholders());
		}
	}

	private void appendCtePlaceholders(List<CompiledQuery.Placeholder> placeholders) {
		for (CteDeclaration declaration : ctes) {
			placeholders.addAll(declaration.query().collectPlaceholders());
		}
	}

	private String resolveSetOperator(SetOperator operator) {
		return switch (operator) {
		case UNION -> SetOperator.UNION.sql();
		case UNION_ALL -> SetOperator.UNION_ALL.sql();
		case INTERSECT -> SetOperator.INTERSECT.sql();
		case INTERSECT_ALL -> SetOperator.INTERSECT_ALL.sql();
		case EXCEPT -> supportsExcept() ? SetOperator.EXCEPT.sql() : "MINUS";
		case EXCEPT_ALL -> {
			if (supportsExceptAll()) {
				yield SetOperator.EXCEPT_ALL.sql();
			}
			throw new UnsupportedOperationException("EXCEPT ALL/MINUS ALL is not supported by the default dialect");
		}
		};
	}

	private boolean supportsExcept() {
		return false;
	}

	private boolean supportsExceptAll() {
		return false;
	}

	public void withClauses(List<CteDeclaration> declaredCtes) {
		if (declaredCtes == null || declaredCtes.isEmpty()) {
			this.ctes = List.of();
		} else {
			this.ctes = List.copyOf(declaredCtes);
		}
	}

	private void appendWithClauses(StringBuilder builder) {
		if (ctes.isEmpty()) {
			return;
		}
		if (!dialect.supportsCte()) {
			throw new UnsupportedOperationException(
					"Dialect '" + dialect.id() + "' does not support common table expressions");
		}
		builder.append("WITH ");
		for (int i = 0; i < ctes.size(); i++) {
			CteDeclaration cte = ctes.get(i);
			builder.append(dialect.quoteIdent(cte.name()));
			appendCteColumns(builder, cte.columnAliases());
			builder.append(" AS (")
					.append(cte.query().transpile())
					.append(')');
			if (i < ctes.size() - 1) {
				builder.append(", ");
			} else {
				builder.append(' ');
			}
		}
	}

	private void appendCteColumns(StringBuilder builder, List<String> aliases) {
		if (aliases.isEmpty()) {
			return;
		}
		builder.append('(');
		for (int i = 0; i < aliases.size(); i++) {
			if (i > 0) {
				builder.append(", ");
			}
			builder.append(dialect.quoteIdent(aliases.get(i)));
		}
		builder.append(')');
	}

	public static final class CteDeclaration {
		private final String name;
		private final QueryImpl query;
		private final List<String> columnAliases;

		public CteDeclaration(String name, QueryImpl query, List<String> columnAliases) {
			this.name = Objects.requireNonNull(name, "name");
			this.query = Objects.requireNonNull(query, "query");
			this.columnAliases = List.copyOf(Objects.requireNonNull(columnAliases, "columnAliases"));
		}

		String name() {
			return name;
		}

		QueryImpl query() {
			return query;
		}

		List<String> columnAliases() {
			return columnAliases;
		}
	}

	private String parenthesize(QueryImpl query) {
		return "(" + query.transpile() + ")";
	}

	private record SetOperation(SetOperator operator, QueryImpl query) {
	}

	private enum PredicateContext {
		WHERE, HAVING
	}

	private final class FluentHavingBuilder implements QueryHavingBuilder {

		private HavingBuilder delegate;

		private FluentHavingBuilder(HavingBuilder delegate) {
			this.delegate = delegate;
		}

		@Override
		public Query eq(String value) {
			this.delegate.eq(value);
			return QueryImpl.this;
		}

		@Override
		public Query eq(Number value) {
			this.delegate.eq(value);
			return QueryImpl.this;
		}

		@Override
		public Query eq(Date value) {
			this.delegate.eq(value);
			return QueryImpl.this;
		}

		@Override
		public Query eq(SqlParameter<?> parameter) {
			this.delegate.eq(parameter);
			return QueryImpl.this;
		}

		@Override
		public Query notEq(String value) {
			this.delegate.notEq(value);
			return QueryImpl.this;
		}

		@Override
		public Query notEq(Number value) {
			this.delegate.notEq(value);
			return QueryImpl.this;
		}

		@Override
		public Query notEq(Date value) {
			this.delegate.notEq(value);
			return QueryImpl.this;
		}

		@Override
		public Query notEq(SqlParameter<?> parameter) {
			this.delegate.notEq(parameter);
			return QueryImpl.this;
		}

		@Override
		public Query in(String... values) {
			this.delegate.in(values);
			return QueryImpl.this;
		}

		@Override
		public Query in(Number... values) {
			this.delegate.in(values);
			return QueryImpl.this;
		}

		@Override
		public Query in(Date... values) {
			this.delegate.in(values);
			return QueryImpl.this;
		}

		@Override
		public Query notIn(String... values) {
			this.delegate.notIn(values);
			return QueryImpl.this;
		}

		@Override
		public Query notIn(Number... values) {
			this.delegate.notIn(values);
			return QueryImpl.this;
		}

		@Override
		public Query notIn(Date... values) {
			this.delegate.notIn(values);
			return QueryImpl.this;
		}

		@Override
		public Query like(String value) {
			this.delegate.like(value);
			return QueryImpl.this;
		}

		@Override
		public Query like(SqlParameter<?> parameter) {
			this.delegate.like(parameter);
			return QueryImpl.this;
		}

		@Override
		public Query notLike(String value) {
			this.delegate.notLike(value);
			return QueryImpl.this;
		}

		@Override
		public Query notLike(SqlParameter<?> parameter) {
			this.delegate.notLike(parameter);
			return QueryImpl.this;
		}

		@Override
		public Query between(Number lower, Number upper) {
			this.delegate.between(lower, upper);
			return QueryImpl.this;
		}

		@Override
		public Query between(Date lower, Date upper) {
			this.delegate.between(lower, upper);
			return QueryImpl.this;
		}

		@Override
		public Query between(SqlParameter<?> lower, SqlParameter<?> upper) {
			this.delegate.between(lower, upper);
			return QueryImpl.this;
		}

		@Override
		public Query isNull() {
			this.delegate.isNull();
			return QueryImpl.this;
		}

		@Override
		public Query isNotNull() {
			this.delegate.isNotNull();
			return QueryImpl.this;
		}

		@Override
		public Query eq(Query subquery) {
			this.delegate.eq(subquery);
			return QueryImpl.this;
		}

		@Override
		public Query notEq(Query subquery) {
			this.delegate.notEq(subquery);
			return QueryImpl.this;
		}

		@Override
		public Query in(Query subquery) {
			this.delegate.in(subquery);
			return QueryImpl.this;
		}

		@Override
		public Query notIn(Query subquery) {
			this.delegate.notIn(subquery);
			return QueryImpl.this;
		}

		@Override
		public Query supTo(Query subquery) {
			this.delegate.supTo(subquery);
			return QueryImpl.this;
		}

		@Override
		public Query supOrEqTo(Query subquery) {
			this.delegate.supOrEqTo(subquery);
			return QueryImpl.this;
		}

		@Override
		public Query infTo(Query subquery) {
			this.delegate.infTo(subquery);
			return QueryImpl.this;
		}

		@Override
		public Query infOrEqTo(Query subquery) {
			this.delegate.infOrEqTo(subquery);
			return QueryImpl.this;
		}

		@Override
		public Query exists(Query subquery) {
			this.delegate.exists(subquery);
			return QueryImpl.this;
		}

		@Override
		public Query notExists(Query subquery) {
			this.delegate.notExists(subquery);
			return QueryImpl.this;
		}

		@Override
		public Query supTo(Number value) {
			this.delegate.supTo(value);
			return QueryImpl.this;
		}

		@Override
		public Query supTo(SqlParameter<?> parameter) {
			this.delegate.supTo(parameter);
			return QueryImpl.this;
		}

		@Override
		public Query supOrEqTo(Number value) {
			this.delegate.supOrEqTo(value);
			return QueryImpl.this;
		}

		@Override
		public Query supOrEqTo(SqlParameter<?> parameter) {
			this.delegate.supOrEqTo(parameter);
			return QueryImpl.this;
		}

		@Override
		public Query infTo(Number value) {
			this.delegate.infTo(value);
			return QueryImpl.this;
		}

		@Override
		public Query infTo(SqlParameter<?> parameter) {
			this.delegate.infTo(parameter);
			return QueryImpl.this;
		}

		@Override
		public Query infOrEqTo(Number value) {
			this.delegate.infOrEqTo(value);
			return QueryImpl.this;
		}

		@Override
		public Query infOrEqTo(SqlParameter<?> parameter) {
			this.delegate.infOrEqTo(parameter);
			return QueryImpl.this;
		}

		@Override
		public Query supTo(Column column) {
			this.delegate.supTo(column);
			return QueryImpl.this;
		}

		@Override
		public Query supOrEqTo(Column column) {
			this.delegate.supOrEqTo(column);
			return QueryImpl.this;
		}

		@Override
		public Query infTo(Column column) {
			this.delegate.infTo(column);
			return QueryImpl.this;
		}

		@Override
		public Query infOrEqTo(Column column) {
			this.delegate.infOrEqTo(column);
			return QueryImpl.this;
		}

		@Override
		public Query notEq(Column column) {
			this.delegate.notEq(column);
			return QueryImpl.this;
		}

		@Override
		public QueryHavingBuilder and(Column column) {
			this.delegate = this.delegate.and(column);
			return this;
		}

		@Override
		public QueryHavingBuilder or(Column column) {
			this.delegate = this.delegate.or(column);
			return this;
		}

		@Override
		public QueryHavingBuilder min(Column column) {
			this.delegate = this.delegate.min(column);
			return this;
		}

		@Override
		public QueryHavingBuilder max(Column column) {
			this.delegate = this.delegate.max(column);
			return this;
		}

		@Override
		public QueryHavingBuilder sum(Column column) {
			this.delegate = this.delegate.sum(column);
			return this;
		}

		@Override
		public QueryHavingBuilder avg(Column column) {
			this.delegate = this.delegate.avg(column);
			return this;
		}

		@Override
		public QueryHavingBuilder col(Column column) {
			this.delegate = this.delegate.col(column);
			return this;
		}
	}

}
