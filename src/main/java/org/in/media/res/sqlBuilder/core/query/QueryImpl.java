package org.in.media.res.sqlBuilder.core.query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.SetOperator;
import org.in.media.res.sqlBuilder.constants.SortDirection;
import org.in.media.res.sqlBuilder.core.query.FromImpl.Joiner;
import org.in.media.res.sqlBuilder.core.query.factory.CLauseFactory;
import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.api.query.Clause;
import org.in.media.res.sqlBuilder.api.query.Condition;
import org.in.media.res.sqlBuilder.api.query.From;
import org.in.media.res.sqlBuilder.api.query.GroupBy;
import org.in.media.res.sqlBuilder.api.query.Having;
import org.in.media.res.sqlBuilder.api.query.HavingBuilder;
import org.in.media.res.sqlBuilder.api.query.QueryHavingBuilder;
import org.in.media.res.sqlBuilder.api.query.Limit;
import org.in.media.res.sqlBuilder.api.query.OrderBy;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.Select;
import org.in.media.res.sqlBuilder.api.query.Where;

public class QueryImpl implements Query {

	private Select selectClause = CLauseFactory.instanciateSelect();

	private From fromClause = CLauseFactory.instanciateFrom();

	private Where whereClause = CLauseFactory.instanciateWhere();

	private GroupBy groupByClause = CLauseFactory.instanciateGroupBy();

	private OrderBy orderByClause = CLauseFactory.instanciateOrderBy();

	private Having havingClause = CLauseFactory.instanciateHaving();

	private Limit limitClause = CLauseFactory.instanciateLimit();

	private final List<SetOperation> setOperations = new ArrayList<>();

	private static final Column STAR = StarColumn.INSTANCE;

	public static Query newQuery() {
		return new QueryImpl();
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

	private void registerBaseTable(Column column) {
		if (column != null && column.table() != null) {
			this.fromClause.from(column.table());
		}
	}

	public String transpile() {
		StringBuilder builder = new StringBuilder()
				.append(selectClause.transpile())
				.append(fromClause.transpile())
				.append(whereClause.transpile())
				.append(groupByClause.transpile())
				.append(havingClause.transpile())
				.append(orderByClause.transpile())
				.append(limitClause.transpile());

		for (SetOperation operation : setOperations) {
			builder.append(' ') 
				.append(resolveSetOperator(operation.operator()))
				.append(' ')
				.append(parenthesize(operation.query()));
		}
		return builder.toString();
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
		this.selectClause = CLauseFactory.instanciateSelect();
		this.fromClause = CLauseFactory.instanciateFrom();
		this.whereClause = CLauseFactory.instanciateWhere();
		this.groupByClause = CLauseFactory.instanciateGroupBy();
		this.orderByClause = CLauseFactory.instanciateOrderBy();
		this.havingClause = CLauseFactory.instanciateHaving();
		this.limitClause = CLauseFactory.instanciateLimit();
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
	public Query select(AggregateOperator agg, TableDescriptor<?> descriptor) {
		return this.select(agg, descriptor.column());
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
	public Query where(Column column) {
		this.whereClause.where(column);
		return this;
	}

	@Override
	public Query eq(Column column) {
		this.whereClause.eq(column);
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
	public Query in(String... value) {
		this.whereClause.in(value);
		return this;
	}

	@Override
	public Query eq(Integer value) {
		this.whereClause.eq(value);
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
	public Query eq(Date value) {
		this.whereClause.eq(value);
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
	public Query eq(Double value) {
		this.whereClause.eq(value);
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
	public Query and(Column column) {
		this.whereClause.and(column);
		return this;
	}

	@Override
	public Query or(Column column) {
		this.whereClause.or(column);
		return this;
	}

	@Override
	public Query and() {
		this.whereClause.and();
		return this;
	}

	@Override
	public Query or() {
		this.whereClause.or();
		return this;
	}

	@Override
	public Query eq() {
		this.whereClause.eq();
		return this;
	}

	@Override
	public Query supTo() {
		this.whereClause.supTo();
		return this;
	}

	@Override
	public Query infTo() {
		this.whereClause.infTo();
		return this;
	}

	@Override
	public Query supOrEqTo() {
		this.whereClause.supOrEqTo();
		return this;
	}

	@Override
	public Query infOrEqTo() {
		this.whereClause.infOrEqTo();
		return this;
	}

	@Override
	public Query in() {
		this.whereClause.in();
		return this;
	}

	@Override
	public Query min(Column column) {
		this.whereClause.min(column);
		return this;
	}

	@Override
	public Query max(Column column) {
		this.whereClause.max(column);
		return this;
	}

	@Override
	public Query sum(Column column) {
		this.whereClause.sum(column);
		return this;
	}

	@Override
	public Query avg(Column column) {
		this.whereClause.avg(column);
		return this;
	}

	@Override
	public Query col(Column column) {
		this.whereClause.col(column);
		return this;
	}

	@Override
	public Query condition(Condition condition) {
		this.whereClause.condition(condition);
		return this;
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
		this.havingClause.having(condition);
		return this;
	}

	@Override
	public Query and(Condition condition) {
		this.havingClause.and(condition);
		return this;
	}

	@Override
	public Query or(Condition condition) {
		this.havingClause.or(condition);
		return this;
	}

	@Override
	public QueryHavingBuilder having(Column column) {
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
		public Query supTo(Number value) {
			this.delegate.supTo(value);
			return QueryImpl.this;
		}

		@Override
		public Query supOrEqTo(Number value) {
			this.delegate.supOrEqTo(value);
			return QueryImpl.this;
		}

		@Override
		public Query infTo(Number value) {
			this.delegate.infTo(value);
			return QueryImpl.this;
		}

		@Override
		public Query infOrEqTo(Number value) {
			this.delegate.infOrEqTo(value);
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
