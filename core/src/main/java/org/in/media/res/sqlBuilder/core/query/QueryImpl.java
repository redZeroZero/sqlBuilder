package org.in.media.res.sqlBuilder.core.query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.SetOperator;
import org.in.media.res.sqlBuilder.constants.SortDirection;
import org.in.media.res.sqlBuilder.core.model.DerivedTableImpl;
import org.in.media.res.sqlBuilder.core.query.FromImpl.Joiner;
import org.in.media.res.sqlBuilder.core.query.factory.CLauseFactory;
import org.in.media.res.sqlBuilder.core.query.predicate.ConditionGroup;
import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.api.query.Clause;
import org.in.media.res.sqlBuilder.api.query.CompiledQuery;
import org.in.media.res.sqlBuilder.api.query.Condition;
import org.in.media.res.sqlBuilder.api.query.ConditionValue;
import org.in.media.res.sqlBuilder.api.query.Dialect;
import org.in.media.res.sqlBuilder.api.query.From;
import org.in.media.res.sqlBuilder.api.query.GroupBy;
import org.in.media.res.sqlBuilder.api.query.Having;
import org.in.media.res.sqlBuilder.api.query.HavingBuilder;
import org.in.media.res.sqlBuilder.api.query.QueryHavingBuilder;
import org.in.media.res.sqlBuilder.api.query.Limit;
import org.in.media.res.sqlBuilder.api.query.OrderBy;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.Select;
import org.in.media.res.sqlBuilder.api.query.SqlAndParams;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.api.query.Where;
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
		this.selectClause = CLauseFactory.instanciateSelect(dialect);
		this.fromClause = CLauseFactory.instanciateFrom(dialect);
		this.whereClause = CLauseFactory.instanciateWhere(dialect);
		this.groupByClause = CLauseFactory.instanciateGroupBy(dialect);
		this.orderByClause = CLauseFactory.instanciateOrderBy(dialect);
		this.havingClause = CLauseFactory.instanciateHaving(dialect);
		this.limitClause = CLauseFactory.instanciateLimit(dialect);
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
		StringBuilder builder = new StringBuilder()
				.append(selectClause.transpile())
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
		this.selectClause = CLauseFactory.instanciateSelect(dialect);
		this.fromClause = CLauseFactory.instanciateFrom(dialect);
		this.whereClause = CLauseFactory.instanciateWhere(dialect);
		this.groupByClause = CLauseFactory.instanciateGroupBy(dialect);
		this.orderByClause = CLauseFactory.instanciateOrderBy(dialect);
		this.havingClause = CLauseFactory.instanciateHaving(dialect);
		this.limitClause = CLauseFactory.instanciateLimit(dialect);
		this.setOperations.clear();
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
		activateWhere();
		this.whereClause.condition(condition);
		return this;
	}

	@Override
	public Query where(Column column) {
		activateWhere();
		this.whereClause.where(column);
		return this;
	}

	@Override
	public Query eq(Column column) {
		this.whereClause.eq(column);
		return this;
	}

	@Override
	public Query notEq(Column column) {
		this.whereClause.notEq(column);
		return this;
	}

	@Override
	public Query supTo(Column column) {
		this.whereClause.supTo(column);
		return this;
	}

	@Override
	public Query infTo(Column column) {
		this.whereClause.infTo(column);
		return this;
	}

	@Override
	public Query supOrEqTo(Column column) {
		this.whereClause.supOrEqTo(column);
		return this;
	}

	@Override
	public Query infOrEqTo(Column column) {
		this.whereClause.infOrEqTo(column);
		return this;
	}

	@Override
	public Query eq(String value) {
		this.whereClause.eq(value);
		return this;
	}

	@Override
	public Query notEq(String value) {
		this.whereClause.notEq(value);
		return this;
	}

	@Override
	public Query eq(SqlParameter<?> parameter) {
		this.whereClause.eq(parameter);
		return this;
	}

	@Override
	public Query notEq(SqlParameter<?> parameter) {
		this.whereClause.notEq(parameter);
		return this;
	}

	@Override
	public Query like(String value) {
		this.whereClause.like(value);
		return this;
	}

	@Override
	public Query notLike(String value) {
		this.whereClause.notLike(value);
		return this;
	}

	@Override
	public Query between(String lower, String upper) {
		this.whereClause.between(lower, upper);
		return this;
	}

	@Override
	public Query between(SqlParameter<?> lower, SqlParameter<?> upper) {
		this.whereClause.between(lower, upper);
		return this;
	}

	@Override
	public Query supTo(String value) {
		this.whereClause.supTo(value);
		return this;
	}

	@Override
	public Query infTo(String value) {
		this.whereClause.infTo(value);
		return this;
	}

	@Override
	public Query supOrEqTo(String value) {
		this.whereClause.supOrEqTo(value);
		return this;
	}

	@Override
	public Query infOrEqTo(String value) {
		this.whereClause.infOrEqTo(value);
		return this;
	}

	@Override
	public Query supTo(SqlParameter<?> parameter) {
		this.whereClause.supTo(parameter);
		return this;
	}

	@Override
	public Query infTo(SqlParameter<?> parameter) {
		this.whereClause.infTo(parameter);
		return this;
	}

	@Override
	public Query supOrEqTo(SqlParameter<?> parameter) {
		this.whereClause.supOrEqTo(parameter);
		return this;
	}

	@Override
	public Query infOrEqTo(SqlParameter<?> parameter) {
		this.whereClause.infOrEqTo(parameter);
		return this;
	}

	@Override
	public Query in(String... value) {
		this.whereClause.in(value);
		return this;
	}

	@Override
	public Query notIn(String... value) {
		this.whereClause.notIn(value);
		return this;
	}

	@Override
	public Query eq(Integer value) {
		this.whereClause.eq(value);
		return this;
	}

	@Override
	public Query notEq(Integer value) {
		this.whereClause.notEq(value);
		return this;
	}

	@Override
	public Query between(Integer lower, Integer upper) {
		this.whereClause.between(lower, upper);
		return this;
	}

	@Override
	public Query supTo(Integer value) {
		this.whereClause.supTo(value);
		return this;
	}

	@Override
	public Query infTo(Integer value) {
		this.whereClause.infTo(value);
		return this;
	}

	@Override
	public Query supOrEqTo(Integer value) {
		this.whereClause.supOrEqTo(value);
		return this;
	}

	@Override
	public Query infOrEqTo(Integer value) {
		this.whereClause.infOrEqTo(value);
		return this;
	}

	@Override
	public Query in(Integer... value) {
		this.whereClause.in(value);
		return this;
	}

	@Override
	public Query notIn(Integer... value) {
		this.whereClause.notIn(value);
		return this;
	}

	@Override
	public Query eq(Date value) {
		this.whereClause.eq(value);
		return this;
	}

	@Override
	public Query notEq(Date value) {
		this.whereClause.notEq(value);
		return this;
	}

	@Override
	public Query between(Date lower, Date upper) {
		this.whereClause.between(lower, upper);
		return this;
	}

	@Override
	public Query supTo(Date value) {
		this.whereClause.supTo(value);
		return this;
	}

	@Override
	public Query infTo(Date value) {
		this.whereClause.infTo(value);
		return this;
	}

	@Override
	public Query supOrEqTo(Date value) {
		this.whereClause.supOrEqTo(value);
		return this;
	}

	@Override
	public Query infOrEqTo(Date value) {
		this.whereClause.infOrEqTo(value);
		return this;
	}

	@Override
	public Query in(Date... value) {
		this.whereClause.in(value);
		return this;
	}

	@Override
	public Query notIn(Date... value) {
		this.whereClause.notIn(value);
		return this;
	}

	@Override
	public Query eq(Double value) {
		this.whereClause.eq(value);
		return this;
	}

	@Override
	public Query notEq(Double value) {
		this.whereClause.notEq(value);
		return this;
	}

	@Override
	public Query between(Double lower, Double upper) {
		this.whereClause.between(lower, upper);
		return this;
	}

	@Override
	public Query supTo(Double value) {
		this.whereClause.supTo(value);
		return this;
	}

	@Override
	public Query infTo(Double value) {
		this.whereClause.infTo(value);
		return this;
	}

	@Override
	public Query supOrEqTo(Double value) {
		this.whereClause.supOrEqTo(value);
		return this;
	}

	@Override
	public Query infOrEqTo(Double value) {
		this.whereClause.infOrEqTo(value);
		return this;
	}

	@Override
	public Query in(Double... value) {
		this.whereClause.in(value);
		return this;
	}

	@Override
	public Query notIn(Double... value) {
		this.whereClause.notIn(value);
		return this;
	}

	@Override
	public Query isNull() {
		this.whereClause.isNull();
		return this;
	}

	@Override
	public Query isNotNull() {
		this.whereClause.isNotNull();
		return this;
	}

	@Override
	public Query eq(Query subquery) {
		this.whereClause.eq(subquery);
		return this;
	}

	@Override
	public Query notEq(Query subquery) {
		this.whereClause.notEq(subquery);
		return this;
	}

	@Override
	public Query in(Query subquery) {
		this.whereClause.in(subquery);
		return this;
	}

	@Override
	public Query notIn(Query subquery) {
		this.whereClause.notIn(subquery);
		return this;
	}

	@Override
	public Query supTo(Query subquery) {
		this.whereClause.supTo(subquery);
		return this;
	}

	@Override
	public Query infTo(Query subquery) {
		this.whereClause.infTo(subquery);
		return this;
	}

	@Override
	public Query supOrEqTo(Query subquery) {
		this.whereClause.supOrEqTo(subquery);
		return this;
	}

	@Override
	public Query infOrEqTo(Query subquery) {
		this.whereClause.infOrEqTo(subquery);
		return this;
	}

	@Override
	public Query exists(Query subquery) {
		activateWhere();
		this.whereClause.exists(subquery);
		return this;
	}

	@Override
	public Query notExists(Query subquery) {
		activateWhere();
		this.whereClause.notExists(subquery);
		return this;
	}

	@Override
	public Query and(Column column) {
		activateWhere();
		this.whereClause.and(column);
		return this;
	}

	@Override
	public Query or(Column column) {
		activateWhere();
		this.whereClause.or(column);
		return this;
	}

	@Override
	public Query and() {
		activateWhere();
		this.whereClause.and();
		return this;
	}

	@Override
	public Query or() {
		activateWhere();
		this.whereClause.or();
		return this;
	}

	@Override
	public Query eq() {
		activateWhere();
		this.whereClause.eq();
		return this;
	}

	@Override
	public Query supTo() {
		activateWhere();
		this.whereClause.supTo();
		return this;
	}

	@Override
	public Query infTo() {
		activateWhere();
		this.whereClause.infTo();
		return this;
	}

	@Override
	public Query supOrEqTo() {
		activateWhere();
		this.whereClause.supOrEqTo();
		return this;
	}

	@Override
	public Query infOrEqTo() {
		activateWhere();
		this.whereClause.infOrEqTo();
		return this;
	}

	@Override
	public Query in() {
		activateWhere();
		this.whereClause.in();
		return this;
	}

	@Override
	public Query min(Column column) {
		activateWhere();
		this.whereClause.min(column);
		return this;
	}

	@Override
	public Query max(Column column) {
		activateWhere();
		this.whereClause.max(column);
		return this;
	}

	@Override
	public Query sum(Column column) {
		activateWhere();
		this.whereClause.sum(column);
		return this;
	}

	@Override
	public Query avg(Column column) {
		activateWhere();
		this.whereClause.avg(column);
		return this;
	}

	@Override
	public Query col(Column column) {
		activateWhere();
		this.whereClause.col(column);
		return this;
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
	public Map<Table, Joiner> joins() {
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
		activateHaving();
		this.havingClause.having(condition);
		return this;
	}

	@Override
	public Query and(Condition condition) {
		if (this.activePredicateContext == PredicateContext.HAVING) {
			this.havingClause.and(condition);
			activateHaving();
			return this;
		}
		activateWhere();
		this.whereClause.and(condition);
		return this;
	}

	@Override
	public Query or(Condition condition) {
		if (this.activePredicateContext == PredicateContext.HAVING) {
			this.havingClause.or(condition);
			activateHaving();
			return this;
		}
		activateWhere();
		this.whereClause.or(condition);
		return this;
	}

	@Override
	public QueryHavingBuilder having(Column column) {
		activateHaving();
		return new FluentHavingBuilder(this.havingClause.having(column));
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

	private List<CompiledQuery.Placeholder> collectPlaceholders() {
		List<CompiledQuery.Placeholder> placeholders = new ArrayList<>();
		appendConditionPlaceholders(whereClause.conditions(), placeholders);
		appendConditionPlaceholders(havingClause.havingConditions(), placeholders);
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
		public Query notLike(String value) {
			this.delegate.notLike(value);
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
