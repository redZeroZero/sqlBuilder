package org.in.media.res.sqlBuilder.api.query;

import java.util.Date;
import java.util.List;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.SortDirection;
import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;

public interface Query extends Select, From, Where, GroupBy, OrderBy, Having, Limit {

	public List<Clause> clauses();

	Table as(String alias, String... columnAliases);

	Query select(Column column);

	Query select(TableDescriptor<?> descriptor);

	Query select(Column... columns);

	Query select(TableDescriptor<?>... descriptors);

	Query select(Table table);

	Query select(AggregateOperator agg, Column column);

	Query select(AggregateOperator agg, TableDescriptor<?> descriptor);

	Query where(Condition condition);

	@Override
	Query distinct();

	Query count();

	Query count(Column column);

	Query count(TableDescriptor<?> descriptor);

	String prettyPrint();

	Query on(Column c1, Column c2);

	Query on(TableDescriptor<?> left, TableDescriptor<?> right);

	Query from(Table table);

Query from(Table... tables);
Query join(Table t);
Query innerJoin(Table t);
Query leftJoin(Table t);
Query rightJoin(Table t);
Query crossJoin(Table t);
Query fullOuterJoin(Table t);

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
	default Query where(TableDescriptor<?> descriptor) {
		return where(descriptor.column());
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
	Query orderBy(Column column);

	Query orderBy(TableDescriptor<?> descriptor);

	@Override
	Query orderBy(Column column, SortDirection direction);

	Query orderBy(TableDescriptor<?> descriptor, SortDirection direction);

	@Override
	Query asc(Column column);

	Query asc(TableDescriptor<?> descriptor);

	@Override
	Query desc(Column column);

	Query desc(TableDescriptor<?> descriptor);

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
