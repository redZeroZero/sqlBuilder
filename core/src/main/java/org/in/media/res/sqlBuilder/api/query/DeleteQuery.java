package org.in.media.res.sqlBuilder.api.query;

import java.util.Date;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.ColumnRef;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.core.query.OptionalConditions;

/**
 * Fluent DSL for building {@code DELETE} statements.
 */
public interface DeleteQuery {

	String transpile();

	SqlAndParams render();

	CompiledQuery compile();

	Table target();

	DeleteQuery where(Condition condition);

	DeleteQuery condition(Condition condition);

	DeleteQuery where(Column column);

	default DeleteQuery where(TableDescriptor<?> descriptor) {
		return where(descriptor.column());
	}

	default DeleteQuery where(ColumnRef<?> descriptor) {
		return where(descriptor.column());
	}

	DeleteQuery whereRaw(String sql);

	DeleteQuery whereRaw(String sql, SqlParameter<?>... params);

	DeleteQuery whereRaw(RawSqlFragment fragment);

	DeleteQuery and(Condition condition);

	DeleteQuery or(Condition condition);

	DeleteQuery and();

	DeleteQuery or();

	DeleteQuery and(Column column);

	default DeleteQuery and(TableDescriptor<?> descriptor) {
		return and(descriptor.column());
	}

	DeleteQuery or(Column column);

	default DeleteQuery or(TableDescriptor<?> descriptor) {
		return or(descriptor.column());
	}

	DeleteQuery andRaw(String sql);

	DeleteQuery andRaw(String sql, SqlParameter<?>... params);

	DeleteQuery andRaw(RawSqlFragment fragment);

	DeleteQuery orRaw(String sql);

	DeleteQuery orRaw(String sql, SqlParameter<?>... params);

	DeleteQuery orRaw(RawSqlFragment fragment);

	DeleteQuery eq();

	DeleteQuery supTo();

	DeleteQuery infTo();

	DeleteQuery supOrEqTo();

	DeleteQuery infOrEqTo();

	DeleteQuery in();

	DeleteQuery eq(Column column);

	DeleteQuery notEq(Column column);

	DeleteQuery supTo(Column column);

	DeleteQuery infTo(Column column);

	DeleteQuery supOrEqTo(Column column);

	DeleteQuery infOrEqTo(Column column);

	default DeleteQuery eq(TableDescriptor<?> descriptor) {
		return eq(descriptor.column());
	}

	default DeleteQuery notEq(TableDescriptor<?> descriptor) {
		return notEq(descriptor.column());
	}

	default DeleteQuery supTo(TableDescriptor<?> descriptor) {
		return supTo(descriptor.column());
	}

	default DeleteQuery infTo(TableDescriptor<?> descriptor) {
		return infTo(descriptor.column());
	}

	default DeleteQuery supOrEqTo(TableDescriptor<?> descriptor) {
		return supOrEqTo(descriptor.column());
	}

	default DeleteQuery infOrEqTo(TableDescriptor<?> descriptor) {
		return infOrEqTo(descriptor.column());
	}

	default DeleteQuery eq(ColumnRef<?> descriptor) {
		return eq(descriptor.column());
	}

	default DeleteQuery notEq(ColumnRef<?> descriptor) {
		return notEq(descriptor.column());
	}

	default DeleteQuery supTo(ColumnRef<?> descriptor) {
		return supTo(descriptor.column());
	}

	default DeleteQuery infTo(ColumnRef<?> descriptor) {
		return infTo(descriptor.column());
	}

	default DeleteQuery supOrEqTo(ColumnRef<?> descriptor) {
		return supOrEqTo(descriptor.column());
	}

	default DeleteQuery infOrEqTo(ColumnRef<?> descriptor) {
		return infOrEqTo(descriptor.column());
	}

	DeleteQuery eq(String value);

	DeleteQuery notEq(String value);

	DeleteQuery like(String value);

	DeleteQuery notLike(String value);

	DeleteQuery eq(SqlParameter<?> parameter);

	DeleteQuery notEq(SqlParameter<?> parameter);

	DeleteQuery like(SqlParameter<?> parameter);

	DeleteQuery notLike(SqlParameter<?> parameter);

	DeleteQuery between(String lower, String upper);

	DeleteQuery between(SqlParameter<?> lower, SqlParameter<?> upper);

	DeleteQuery supTo(String value);

	DeleteQuery infTo(String value);

	DeleteQuery supOrEqTo(String value);

	DeleteQuery infOrEqTo(String value);

	DeleteQuery supTo(SqlParameter<?> parameter);

	DeleteQuery infTo(SqlParameter<?> parameter);

	DeleteQuery supOrEqTo(SqlParameter<?> parameter);

	DeleteQuery infOrEqTo(SqlParameter<?> parameter);

	DeleteQuery in(String... value);

	DeleteQuery notIn(String... value);

	DeleteQuery eq(Integer value);

	DeleteQuery notEq(Integer value);

	DeleteQuery between(Integer lower, Integer upper);

	DeleteQuery supTo(Integer value);

	DeleteQuery infTo(Integer value);

	DeleteQuery supOrEqTo(Integer value);

	DeleteQuery infOrEqTo(Integer value);

	DeleteQuery in(Integer... value);

	DeleteQuery notIn(Integer... value);

	DeleteQuery eq(Date value);

	DeleteQuery notEq(Date value);

	DeleteQuery between(Date lower, Date upper);

	DeleteQuery supTo(Date value);

	DeleteQuery infTo(Date value);

	DeleteQuery supOrEqTo(Date value);

	DeleteQuery infOrEqTo(Date value);

	DeleteQuery in(Date... value);

	DeleteQuery notIn(Date... value);

	DeleteQuery eq(Double value);

	DeleteQuery notEq(Double value);

	DeleteQuery between(Double lower, Double upper);

	DeleteQuery supTo(Double value);

	DeleteQuery infTo(Double value);

	DeleteQuery supOrEqTo(Double value);

	DeleteQuery infOrEqTo(Double value);

	DeleteQuery in(Double... value);

	DeleteQuery notIn(Double... value);

	DeleteQuery isNull();

	DeleteQuery isNotNull();

	DeleteQuery eq(Query subquery);

	DeleteQuery notEq(Query subquery);

	DeleteQuery supTo(Query subquery);

	DeleteQuery infTo(Query subquery);

	DeleteQuery supOrEqTo(Query subquery);

	DeleteQuery infOrEqTo(Query subquery);

	DeleteQuery in(Query subquery);

	DeleteQuery notIn(Query subquery);

	DeleteQuery exists(Query subquery);

	DeleteQuery notExists(Query subquery);

	default DeleteQuery whereOptionalEquals(Column column, SqlParameter<?> parameter) {
		return condition(OptionalConditions.optionalEquals(column, parameter));
	}

	default DeleteQuery whereOptionalEquals(TableDescriptor<?> descriptor, SqlParameter<?> parameter) {
		return whereOptionalEquals(descriptor.column(), parameter);
	}

	default DeleteQuery whereOptionalEquals(ColumnRef<?> descriptor, SqlParameter<?> parameter) {
		return whereOptionalEquals(descriptor.column(), parameter);
	}

	default DeleteQuery whereOptionalLike(Column column, SqlParameter<?> parameter) {
		return condition(OptionalConditions.optionalLike(column, parameter));
	}

	default DeleteQuery whereOptionalLike(TableDescriptor<?> descriptor, SqlParameter<?> parameter) {
		return whereOptionalLike(descriptor.column(), parameter);
	}

	default DeleteQuery whereOptionalLike(ColumnRef<? extends CharSequence> descriptor, SqlParameter<?> parameter) {
		return whereOptionalLike(descriptor.column(), parameter);
	}

	default DeleteQuery whereOptionalGreaterOrEqual(Column column, SqlParameter<?> parameter) {
		return condition(OptionalConditions.optionalGreaterOrEq(column, parameter));
	}

	default DeleteQuery whereOptionalGreaterOrEqual(TableDescriptor<?> descriptor, SqlParameter<?> parameter) {
		return whereOptionalGreaterOrEqual(descriptor.column(), parameter);
	}

	default DeleteQuery whereOptionalGreaterOrEqual(ColumnRef<? extends Number> descriptor,
			SqlParameter<?> parameter) {
		return whereOptionalGreaterOrEqual(descriptor.column(), parameter);
	}
}
