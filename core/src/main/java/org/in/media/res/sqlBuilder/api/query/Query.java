package org.in.media.res.sqlBuilder.api.query;

import java.util.Date;
import java.util.List;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.api.query.spi.Clause;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.SortDirection;
import org.in.media.res.sqlBuilder.api.model.ColumnRef;
import org.in.media.res.sqlBuilder.api.query.window.WindowFunction;

public interface Query extends SelectStage, FromStage {

	public List<Clause> clauses();

	SqlAndParams render();

	CompiledQuery compile();

	/**
	 * Internal/SPI entry point that returns SQL with placeholders without binding.
	 * Application code should call {@link #render()} or {@link #compile()} instead.
	 */
	default String transpile() {
		return render().sql();
	}

	Table as(String alias, String... columnAliases);

	Query select(Column column);

	Query select(TableDescriptor<?> descriptor);

	Query select(Column... columns);

	Query select(TableDescriptor<?>... descriptors);

	Query select(Table table);

	Query select(AggregateOperator agg, Column column);

	Query select(AggregateOperator agg, TableDescriptor<?> descriptor);

	@Override
	Query selectRaw(String sql);

	@Override
	Query selectRaw(String sql, SqlParameter<?>... params);

	@Override
	Query selectRaw(RawSqlFragment fragment);

	Query select(WindowFunction windowFunction);

	default Query select(AggregateOperator agg, ColumnRef<?> descriptor) {
		return select(agg, descriptor.column());
	}

	default Query select(ColumnRef<?> descriptor) {
		select(descriptor.column());
		return this;
	}

	default Query select(ColumnRef<?>... descriptors) {
		for (ColumnRef<?> descriptor : descriptors) {
			select(descriptor);
		}
		return this;
	}

	Query where(Condition condition);

	@Override
	Query distinct();

	Query count();

	Query count(Column column);

	Query count(TableDescriptor<?> descriptor);

	String prettyPrint();

	Query on(Column c1, Column c2);

	Query on(TableDescriptor<?> left, TableDescriptor<?> right);

	default Query on(TableDescriptor<?> left, Column right) {
		return on(left.column(), right);
	}

	default Query on(Column left, TableDescriptor<?> right) {
		return on(left, right.column());
	}

	default Query on(ColumnRef<?> left, ColumnRef<?> right) {
		return on(left.column(), right.column());
	}

Query from(Table table);

Query from(Table... tables);
Query join(Table t);
Query innerJoin(Table t);
Query leftJoin(Table t);
Query rightJoin(Table t);
Query crossJoin(Table t);
Query fullOuterJoin(Table t);

@Override
Query fromRaw(String sql);

@Override
Query fromRaw(String sql, SqlParameter<?>... params);

@Override
Query fromRaw(RawSqlFragment fragment);

@Override
Query joinRaw(String sql);

@Override
Query joinRaw(String sql, SqlParameter<?>... params);

@Override
Query joinRaw(RawSqlFragment fragment);

@Override
Query leftJoinRaw(String sql);

@Override
Query leftJoinRaw(String sql, SqlParameter<?>... params);

@Override
Query leftJoinRaw(RawSqlFragment fragment);

@Override
Query rightJoinRaw(String sql);

@Override
Query rightJoinRaw(String sql, SqlParameter<?>... params);

@Override
Query rightJoinRaw(RawSqlFragment fragment);

@Override
Query fullOuterJoinRaw(String sql);

@Override
Query fullOuterJoinRaw(String sql, SqlParameter<?>... params);

@Override
Query fullOuterJoinRaw(RawSqlFragment fragment);

@Override
Query crossJoinRaw(String sql);

@Override
Query crossJoinRaw(String sql, SqlParameter<?>... params);

@Override
Query crossJoinRaw(RawSqlFragment fragment);

default Query from(Query subquery, String alias, String... columnAliases) {
    return from(subquery.as(alias, columnAliases));
}

default Query join(Query subquery, String alias, String... columnAliases) {
    return join(subquery.as(alias, columnAliases));
}

default Query innerJoin(Query subquery, String alias, String... columnAliases) {
    return innerJoin(subquery.as(alias, columnAliases));
}

default Query leftJoin(Query subquery, String alias, String... columnAliases) {
    return leftJoin(subquery.as(alias, columnAliases));
}

default Query rightJoin(Query subquery, String alias, String... columnAliases) {
    return rightJoin(subquery.as(alias, columnAliases));
}

default Query crossJoin(Query subquery, String alias, String... columnAliases) {
    return crossJoin(subquery.as(alias, columnAliases));
}

default Query fullOuterJoin(Query subquery, String alias, String... columnAliases) {
    return fullOuterJoin(subquery.as(alias, columnAliases));
}

	@Override
	Query condition(Condition condition);

	@Override
	Query where(Column column);

	@Override
	Query whereRaw(String sql);

	@Override
	Query whereRaw(String sql, SqlParameter<?>... params);

	@Override
	Query whereRaw(RawSqlFragment fragment);

	@Override
	default Query where(TableDescriptor<?> descriptor) {
		return where(descriptor.column());
	}

	default Query where(ColumnRef<?> descriptor) {
		where(descriptor.column());
		return this;
	}


	@Override
	Query eq();

	@Override
	Query supTo();

	@Override
	Query infTo();

	@Override
	Query supOrEqTo();

	@Override
	Query infOrEqTo();

	@Override
	Query in();

	@Override
	Query eq(Column column);

	@Override
	Query notEq(Column column);

	@Override
	Query supTo(Column column);

	@Override
	Query infTo(Column column);

	@Override
	Query supOrEqTo(Column column);

	@Override
	Query infOrEqTo(Column column);

	@Override
	Query eq(String value);

	@Override
	Query notEq(String value);

	@Override
	Query like(String value);

	@Override
	Query notLike(String value);

	@Override
	Query between(String lower, String upper);

	@Override
	Query supTo(String value);

	@Override
	Query infTo(String value);

	@Override
	Query supOrEqTo(String value);

	@Override
	Query infOrEqTo(String value);

	@Override
	Query in(String... value);

	@Override
	Query notIn(String... value);

	@Override
	Query eq(Integer value);

	@Override
	Query notEq(Integer value);

	@Override
	Query between(Integer lower, Integer upper);

	@Override
	Query supTo(Integer value);

	@Override
	Query infTo(Integer value);

	@Override
	Query supOrEqTo(Integer value);

	@Override
	Query infOrEqTo(Integer value);

	@Override
	Query in(Integer... value);

	@Override
	Query notIn(Integer... value);

	@Override
	Query eq(Date value);

	@Override
	Query notEq(Date value);

	@Override
	Query between(Date lower, Date upper);

	@Override
	Query supTo(Date value);

	@Override
	Query infTo(Date value);

	@Override
	Query supOrEqTo(Date value);

	@Override
	Query infOrEqTo(Date value);

	@Override
	Query in(Date... value);

	@Override
	Query notIn(Date... value);

	@Override
	Query eq(Double value);

	@Override
	Query notEq(Double value);

	@Override
	Query between(Double lower, Double upper);

	@Override
	Query supTo(Double value);

	@Override
	Query infTo(Double value);

	@Override
	Query supOrEqTo(Double value);

	@Override
	Query infOrEqTo(Double value);

	@Override
	Query in(Double... value);

	@Override
	Query notIn(Double... value);

	default Query like(ColumnRef<? extends CharSequence> descriptor, String value) {
		where(descriptor.column()).like(value);
		return this;
	}

	default Query notLike(ColumnRef<? extends CharSequence> descriptor, String value) {
		where(descriptor.column()).notLike(value);
		return this;
	}

	default Query eq(ColumnRef<String> descriptor, String value) {
		where(descriptor.column()).eq(value);
		return this;
	}

	default Query notEq(ColumnRef<String> descriptor, String value) {
		where(descriptor.column()).notEq(value);
		return this;
	}

	default Query eq(ColumnRef<Integer> descriptor, Integer value) {
		where(descriptor.column()).eq(value);
		return this;
	}

	default Query notEq(ColumnRef<Integer> descriptor, Integer value) {
		where(descriptor.column()).notEq(value);
		return this;
	}

	default Query eq(ColumnRef<Double> descriptor, Double value) {
		where(descriptor.column()).eq(value);
		return this;
	}

	default Query notEq(ColumnRef<Double> descriptor, Double value) {
		where(descriptor.column()).notEq(value);
		return this;
	}

	default Query eq(ColumnRef<Date> descriptor, Date value) {
		where(descriptor.column()).eq(value);
		return this;
	}

	default Query notEq(ColumnRef<Date> descriptor, Date value) {
		where(descriptor.column()).notEq(value);
		return this;
	}

	default Query supTo(ColumnRef<Integer> descriptor, Integer value) {
		where(descriptor.column()).supTo(value);
		return this;
	}

	default Query supOrEqTo(ColumnRef<Integer> descriptor, Integer value) {
		where(descriptor.column()).supOrEqTo(value);
		return this;
	}

	default Query infTo(ColumnRef<Integer> descriptor, Integer value) {
		where(descriptor.column()).infTo(value);
		return this;
	}

	default Query infOrEqTo(ColumnRef<Integer> descriptor, Integer value) {
		where(descriptor.column()).infOrEqTo(value);
		return this;
	}

	default Query supTo(ColumnRef<Double> descriptor, Double value) {
		where(descriptor.column()).supTo(value);
		return this;
	}

	default Query supOrEqTo(ColumnRef<Double> descriptor, Double value) {
		where(descriptor.column()).supOrEqTo(value);
		return this;
	}

	default Query infTo(ColumnRef<Double> descriptor, Double value) {
		where(descriptor.column()).infTo(value);
		return this;
	}

	default Query infOrEqTo(ColumnRef<Double> descriptor, Double value) {
		where(descriptor.column()).infOrEqTo(value);
		return this;
	}

	default Query between(ColumnRef<Integer> descriptor, Integer lower, Integer upper) {
		where(descriptor.column()).between(lower, upper);
		return this;
	}

	default Query between(ColumnRef<Double> descriptor, Double lower, Double upper) {
		where(descriptor.column()).between(lower, upper);
		return this;
	}

	default Query between(ColumnRef<Date> descriptor, Date lower, Date upper) {
		where(descriptor.column()).between(lower, upper);
		return this;
	}

	default Query in(ColumnRef<String> descriptor, String... values) {
		where(descriptor.column()).in(values);
		return this;
	}

	default Query notIn(ColumnRef<String> descriptor, String... values) {
		where(descriptor.column()).notIn(values);
		return this;
	}

	default Query in(ColumnRef<Integer> descriptor, Integer... values) {
		where(descriptor.column()).in(values);
		return this;
	}

	default Query notIn(ColumnRef<Integer> descriptor, Integer... values) {
		where(descriptor.column()).notIn(values);
		return this;
	}

	default Query in(ColumnRef<Double> descriptor, Double... values) {
		where(descriptor.column()).in(values);
		return this;
	}

	default Query notIn(ColumnRef<Double> descriptor, Double... values) {
		where(descriptor.column()).notIn(values);
		return this;
	}

	default Query in(ColumnRef<Date> descriptor, Date... values) {
		where(descriptor.column()).in(values);
		return this;
	}

	default Query notIn(ColumnRef<Date> descriptor, Date... values) {
		where(descriptor.column()).notIn(values);
		return this;
	}

	default Query isNull(ColumnRef<?> descriptor) {
		where(descriptor.column()).isNull();
		return this;
	}

	default Query isNotNull(ColumnRef<?> descriptor) {
		where(descriptor.column()).isNotNull();
		return this;
	}

	@Override
	Query isNull();

	@Override
	Query isNotNull();

	@Override
	Query eq(Query subquery);

	@Override
	Query notEq(Query subquery);

	@Override
	Query in(Query subquery);

	@Override
	Query notIn(Query subquery);

	@Override
	Query supTo(Query subquery);

	@Override
	Query infTo(Query subquery);

	@Override
	Query supOrEqTo(Query subquery);

	@Override
	Query infOrEqTo(Query subquery);

	@Override
	Query exists(Query subquery);

	@Override
	Query notExists(Query subquery);

	@Override
	default Query eq(TableDescriptor<?> descriptor) {
		return eq(descriptor.column());
	}

	@Override
	default Query notEq(TableDescriptor<?> descriptor) {
		return notEq(descriptor.column());
	}

	@Override
	default Query supTo(TableDescriptor<?> descriptor) {
		return supTo(descriptor.column());
	}

	@Override
	default Query infTo(TableDescriptor<?> descriptor) {
		return infTo(descriptor.column());
	}

	@Override
	default Query supOrEqTo(TableDescriptor<?> descriptor) {
		return supOrEqTo(descriptor.column());
	}

	@Override
	default Query infOrEqTo(TableDescriptor<?> descriptor) {
		return infOrEqTo(descriptor.column());
	}

	@Override
	Query and(Column column);

	@Override
	Query or(Column column);

	@Override
	Query and();

	@Override
	Query or();

	@Override
	Query andRaw(String sql);

	@Override
	Query andRaw(String sql, SqlParameter<?>... params);

	@Override
	Query andRaw(RawSqlFragment fragment);

	@Override
	Query orRaw(String sql);

	@Override
	Query orRaw(String sql, SqlParameter<?>... params);

	@Override
	Query orRaw(RawSqlFragment fragment);


	@Override
	Query min(Column column);

	@Override
	Query max(Column column);

	@Override
	Query sum(Column column);

	@Override
	Query avg(Column column);

	@Override
	Query col(Column column);

	@Override
	default Query min(TableDescriptor<?> descriptor) {
		return min(descriptor.column());
	}

	@Override
	default Query max(TableDescriptor<?> descriptor) {
		return max(descriptor.column());
	}

	@Override
	default Query sum(TableDescriptor<?> descriptor) {
		return sum(descriptor.column());
	}

	@Override
	default Query avg(TableDescriptor<?> descriptor) {
		return avg(descriptor.column());
	}

	@Override
	default Query col(TableDescriptor<?> descriptor) {
		return col(descriptor.column());
	}

	@Override
	Query groupBy(Column column);

	Query groupBy(TableDescriptor<?> descriptor);

	@Override
	Query groupBy(Column... columns);

	Query groupBy(TableDescriptor<?>... descriptors);

	@Override
	Query groupByRaw(String sql);

	@Override
	Query groupByRaw(String sql, SqlParameter<?>... params);

	@Override
	Query groupByRaw(RawSqlFragment fragment);

	default Query groupBy(ColumnRef<?> descriptor) {
		return groupBy(descriptor.column());
	}

	default Query groupBy(ColumnRef<?>... descriptors) {
		for (ColumnRef<?> descriptor : descriptors) {
			groupBy(descriptor);
		}
		return this;
	}

	@Override
	Query orderBy(Column column);

	Query orderBy(TableDescriptor<?> descriptor);

	@Override
	Query orderBy(Column column, SortDirection direction);

	Query orderBy(TableDescriptor<?> descriptor, SortDirection direction);

	Query orderByAggregate(AggregateOperator aggregate, Column column);

	Query orderByAggregate(AggregateOperator aggregate, Column column, SortDirection direction);

	Query orderByAlias(String alias);

	Query orderByAlias(String alias, SortDirection direction);

	default Query orderByAggregate(AggregateOperator aggregate, TableDescriptor<?> descriptor) {
		return orderByAggregate(aggregate, descriptor.column());
	}

	default Query orderByAggregate(AggregateOperator aggregate, TableDescriptor<?> descriptor, SortDirection direction) {
		return orderByAggregate(aggregate, descriptor.column(), direction);
	}

	default Query orderByAggregate(AggregateOperator aggregate, ColumnRef<?> ref) {
		return orderByAggregate(aggregate, ref.column());
	}

	default Query orderByAggregate(AggregateOperator aggregate, ColumnRef<?> ref, SortDirection direction) {
		return orderByAggregate(aggregate, ref.column(), direction);
	}

	/**
	 * Convenience to order by the first aggregate in the projection list (if present), defaulting
	 * to ASC. Useful when you selected a single aggregate and want to order by it without repeating
	 * the expression.
	 */
	Query orderByFirstAggregate();

	Query orderByFirstAggregate(SortDirection direction);

	@Override
	Query orderByRaw(String sql);

	@Override
	Query orderByRaw(String sql, SqlParameter<?>... params);

	@Override
	Query orderByRaw(RawSqlFragment fragment);

	default Query orderBy(ColumnRef<?> descriptor) {
		return orderBy(descriptor.column());
	}

	default Query orderBy(ColumnRef<?> descriptor, SortDirection direction) {
		return orderBy(descriptor.column(), direction);
	}

	@Override
	Query asc(Column column);

	Query asc(TableDescriptor<?> descriptor);

	@Override
	Query desc(Column column);

	Query desc(TableDescriptor<?> descriptor);

	default Query asc(ColumnRef<?> descriptor) {
		return asc(descriptor.column());
	}

	default Query desc(ColumnRef<?> descriptor) {
		return desc(descriptor.column());
	}

	@Override
	Query having(Condition condition);

	@Override
	Query and(Condition condition);

	@Override
	Query or(Condition condition);

	@Override
	QueryHavingBuilder having(Column column);

	@Override
	default QueryHavingBuilder having(TableDescriptor<?> descriptor) {
		return having(descriptor.column());
	}

	@Override
	Query havingRaw(String sql);

	@Override
	Query havingRaw(String sql, SqlParameter<?>... params);

	@Override
	Query havingRaw(RawSqlFragment fragment);

	Query union(Query other);

	Query unionAll(Query other);

	Query intersect(Query other);

	Query intersectAll(Query other);

	Query except(Query other);

	Query exceptAll(Query other);

	@Override
	Query limit(int limit);

	@Override
	Query offset(int offset);

	@Override
	Query limitAndOffset(int limit, int offset);

}
