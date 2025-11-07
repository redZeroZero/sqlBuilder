package org.in.media.res.sqlBuilder.api.query;

import java.util.Date;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.constants.SortDirection;
import org.in.media.res.sqlBuilder.core.model.ColumnRef;

/**
 * Stage exposing predicate, grouping, ordering, and pagination operations.
 */
public interface PredicateStage extends QueryStage, Where, GroupBy, OrderBy, Having, Limit {

	@Override
	PredicateStage condition(Condition condition);

	PredicateStage where(Condition condition);

	@Override
	PredicateStage where(Column column);

	@Override
	default PredicateStage where(TableDescriptor<?> descriptor) {
		return where(descriptor.column());
	}

	default PredicateStage where(ColumnRef<?> descriptor) {
		return where(descriptor.column());
	}

	PredicateStage and(Condition condition);

	PredicateStage or(Condition condition);

	@Override
	PredicateStage and();

	@Override
	PredicateStage or();

	@Override
	PredicateStage eq();

	@Override
	PredicateStage supTo();

	@Override
	PredicateStage infTo();

	@Override
	PredicateStage supOrEqTo();

	@Override
	PredicateStage infOrEqTo();

	@Override
	PredicateStage in();

	@Override
	PredicateStage eq(Column column);

	@Override
	PredicateStage notEq(Column column);

	@Override
	PredicateStage supTo(Column column);

	@Override
	PredicateStage infTo(Column column);

	@Override
	PredicateStage supOrEqTo(Column column);

	@Override
	PredicateStage infOrEqTo(Column column);

	@Override
	PredicateStage eq(String value);

	@Override
	PredicateStage notEq(String value);

	@Override
	PredicateStage like(String value);

	@Override
	PredicateStage notLike(String value);

	@Override
	PredicateStage between(String lower, String upper);

	@Override
	PredicateStage supTo(String value);

	@Override
	PredicateStage infTo(String value);

@Override
	PredicateStage supOrEqTo(String value);

	@Override
	PredicateStage infOrEqTo(String value);

	@Override
	PredicateStage in(String... value);

	@Override
	PredicateStage notIn(String... value);

	@Override
	PredicateStage eq(Integer value);

	@Override
	PredicateStage notEq(Integer value);

	@Override
	PredicateStage between(Integer lower, Integer upper);

	@Override
	PredicateStage supTo(Integer value);

	@Override
	PredicateStage infTo(Integer value);

	@Override
	PredicateStage supOrEqTo(Integer value);

	@Override
	PredicateStage infOrEqTo(Integer value);

	@Override
	PredicateStage in(Integer... value);

	@Override
	PredicateStage notIn(Integer... value);

	@Override
	PredicateStage eq(Date value);

	@Override
	PredicateStage notEq(Date value);

	@Override
	PredicateStage between(Date lower, Date upper);

	@Override
	PredicateStage supTo(Date value);

	@Override
	PredicateStage infTo(Date value);

	@Override
	PredicateStage supOrEqTo(Date value);

	@Override
	PredicateStage infOrEqTo(Date value);

	@Override
	PredicateStage in(Date... value);

	@Override
	PredicateStage notIn(Date... value);

	@Override
	PredicateStage eq(Double value);

	@Override
	PredicateStage notEq(Double value);

@Override
	PredicateStage between(Double lower, Double upper);

	@Override
	PredicateStage supTo(Double value);

	@Override
	PredicateStage infTo(Double value);

	@Override
	PredicateStage supOrEqTo(Double value);

	@Override
	PredicateStage infOrEqTo(Double value);

	@Override
	PredicateStage in(Double... value);

	@Override
	PredicateStage notIn(Double... value);

	@Override
	PredicateStage isNull();

	@Override
	PredicateStage isNotNull();

	@Override
	PredicateStage eq(Query subquery);

	@Override
	PredicateStage notEq(Query subquery);

	@Override
	PredicateStage in(Query subquery);

	@Override
	PredicateStage notIn(Query subquery);

	@Override
	PredicateStage supTo(Query subquery);

	@Override
	PredicateStage infTo(Query subquery);

	@Override
	PredicateStage supOrEqTo(Query subquery);

	@Override
	PredicateStage infOrEqTo(Query subquery);

	@Override
	PredicateStage exists(Query subquery);

	@Override
	PredicateStage notExists(Query subquery);

	default PredicateStage like(ColumnRef<? extends CharSequence> descriptor, String value) {
		return where(descriptor).like(value);
	}

	default PredicateStage notLike(ColumnRef<? extends CharSequence> descriptor, String value) {
		return where(descriptor).notLike(value);
	}

	default PredicateStage eq(ColumnRef<String> descriptor, String value) {
		return where(descriptor).eq(value);
	}

	default PredicateStage notEq(ColumnRef<String> descriptor, String value) {
		return where(descriptor).notEq(value);
	}

	default PredicateStage eq(ColumnRef<Integer> descriptor, Integer value) {
		return where(descriptor).eq(value);
	}

	default PredicateStage notEq(ColumnRef<Integer> descriptor, Integer value) {
		return where(descriptor).notEq(value);
	}

	default PredicateStage eq(ColumnRef<Double> descriptor, Double value) {
		return where(descriptor).eq(value);
	}

	default PredicateStage notEq(ColumnRef<Double> descriptor, Double value) {
		return where(descriptor).notEq(value);
	}

	default PredicateStage eq(ColumnRef<Date> descriptor, Date value) {
		return where(descriptor).eq(value);
	}

	default PredicateStage notEq(ColumnRef<Date> descriptor, Date value) {
		return where(descriptor).notEq(value);
	}

	default PredicateStage supTo(ColumnRef<Integer> descriptor, Integer value) {
		return where(descriptor).supTo(value);
	}

	default PredicateStage supOrEqTo(ColumnRef<Integer> descriptor, Integer value) {
		return where(descriptor).supOrEqTo(value);
	}

	default PredicateStage infTo(ColumnRef<Integer> descriptor, Integer value) {
		return where(descriptor).infTo(value);
	}

	default PredicateStage infOrEqTo(ColumnRef<Integer> descriptor, Integer value) {
		return where(descriptor).infOrEqTo(value);
	}

	default PredicateStage supTo(ColumnRef<Double> descriptor, Double value) {
		return where(descriptor).supTo(value);
	}

	default PredicateStage supOrEqTo(ColumnRef<Double> descriptor, Double value) {
		return where(descriptor).supOrEqTo(value);
	}

	default PredicateStage infTo(ColumnRef<Double> descriptor, Double value) {
		return where(descriptor).infTo(value);
	}

	default PredicateStage infOrEqTo(ColumnRef<Double> descriptor, Double value) {
		return where(descriptor).infOrEqTo(value);
	}

	default PredicateStage between(ColumnRef<Integer> descriptor, Integer lower, Integer upper) {
		return where(descriptor).between(lower, upper);
	}

	default PredicateStage between(ColumnRef<Double> descriptor, Double lower, Double upper) {
		return where(descriptor).between(lower, upper);
	}

	default PredicateStage between(ColumnRef<Date> descriptor, Date lower, Date upper) {
		return where(descriptor).between(lower, upper);
	}

	default PredicateStage in(ColumnRef<String> descriptor, String... values) {
		return where(descriptor).in(values);
	}

	default PredicateStage notIn(ColumnRef<String> descriptor, String... values) {
		return where(descriptor).notIn(values);
	}

	default PredicateStage in(ColumnRef<Integer> descriptor, Integer... values) {
		return where(descriptor).in(values);
	}

	default PredicateStage notIn(ColumnRef<Integer> descriptor, Integer... values) {
		return where(descriptor).notIn(values);
	}

	default PredicateStage in(ColumnRef<Double> descriptor, Double... values) {
		return where(descriptor).in(values);
	}

	default PredicateStage notIn(ColumnRef<Double> descriptor, Double... values) {
		return where(descriptor).notIn(values);
	}

	default PredicateStage in(ColumnRef<Date> descriptor, Date... values) {
		return where(descriptor).in(values);
	}

	default PredicateStage notIn(ColumnRef<Date> descriptor, Date... values) {
		return where(descriptor).notIn(values);
	}

	default PredicateStage isNull(ColumnRef<?> descriptor) {
		return where(descriptor).isNull();
	}

	default PredicateStage isNotNull(ColumnRef<?> descriptor) {
		return where(descriptor).isNotNull();
	}

	@Override
	PredicateStage groupBy(Column column);

	@Override
	PredicateStage groupBy(Column... columns);

	@Override
default PredicateStage groupBy(TableDescriptor<?> descriptor) {
		return groupBy(descriptor.column());
	}

	@Override
	default PredicateStage groupBy(TableDescriptor<?>... descriptors) {
		for (TableDescriptor<?> descriptor : descriptors) {
			groupBy(descriptor.column());
		}
		return this;
	}

	@Override
	PredicateStage orderBy(Column column);

	@Override
	default PredicateStage orderBy(TableDescriptor<?> descriptor) {
		return orderBy(descriptor.column());
	}

	@Override
	PredicateStage orderBy(Column column, SortDirection direction);

	@Override
	default PredicateStage orderBy(TableDescriptor<?> descriptor, SortDirection direction) {
		return orderBy(descriptor.column(), direction);
	}

	@Override
	PredicateStage asc(Column column);

	@Override
	default PredicateStage asc(TableDescriptor<?> descriptor) {
		return asc(descriptor.column());
	}

	@Override
	PredicateStage desc(Column column);

	@Override
	default PredicateStage desc(TableDescriptor<?> descriptor) {
		return desc(descriptor.column());
	}

	@Override
	PredicateStage limit(int limit);

	@Override
	PredicateStage offset(int offset);

	@Override
	PredicateStage limitAndOffset(int limit, int offset);

	@Override
	PredicateStage having(Condition condition);

	@Override
	QueryHavingBuilder having(Column column);

	PredicateStage union(Query other);

	PredicateStage unionAll(Query other);

	PredicateStage intersect(Query other);

	PredicateStage intersectAll(Query other);

	PredicateStage except(Query other);

	PredicateStage exceptAll(Query other);
}
