package org.in.media.res.sqlBuilder.api.query;

import java.util.Date;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.ColumnRef;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.core.query.OptionalConditions;

/**
 * Fluent DSL for building {@code UPDATE} statements with bound parameters.
 */
public interface UpdateQuery {

	String transpile();

	SqlAndParams render();

	CompiledQuery compile();

	UpdateQuery set(Column column, Object value);

	UpdateQuery set(Column column, SqlParameter<?> parameter);

	default UpdateQuery set(TableDescriptor<?> descriptor, Object value) {
		return set(descriptor.column(), value);
	}

	default UpdateQuery set(TableDescriptor<?> descriptor, SqlParameter<?> parameter) {
		return set(descriptor.column(), parameter);
	}

	default UpdateQuery set(ColumnRef<?> descriptor, Object value) {
		return set(descriptor.column(), value);
	}

	default UpdateQuery set(ColumnRef<?> descriptor, SqlParameter<?> parameter) {
		return set(descriptor.column(), parameter);
	}

	UpdateQuery setNull(Column column);

	default UpdateQuery setNull(TableDescriptor<?> descriptor) {
		return setNull(descriptor.column());
	}

	default UpdateQuery setNull(ColumnRef<?> descriptor) {
		return setNull(descriptor.column());
	}

	UpdateQuery setRaw(String sql);

	UpdateQuery setRaw(String sql, SqlParameter<?>... params);

	UpdateQuery setRaw(RawSqlFragment fragment);

	UpdateQuery where(Condition condition);

	UpdateQuery condition(Condition condition);

	UpdateQuery where(Column column);

	default UpdateQuery where(TableDescriptor<?> descriptor) {
		return where(descriptor.column());
	}

	default UpdateQuery where(ColumnRef<?> descriptor) {
		return where(descriptor.column());
	}

	UpdateQuery whereRaw(String sql);

	UpdateQuery whereRaw(String sql, SqlParameter<?>... params);

	UpdateQuery whereRaw(RawSqlFragment fragment);

	UpdateQuery and(Condition condition);

	UpdateQuery or(Condition condition);

	UpdateQuery and();

	UpdateQuery or();

	UpdateQuery and(Column column);

	default UpdateQuery and(TableDescriptor<?> descriptor) {
		return and(descriptor.column());
	}

	UpdateQuery or(Column column);

	default UpdateQuery or(TableDescriptor<?> descriptor) {
		return or(descriptor.column());
	}

	UpdateQuery andRaw(String sql);

	UpdateQuery andRaw(String sql, SqlParameter<?>... params);

	UpdateQuery andRaw(RawSqlFragment fragment);

	UpdateQuery orRaw(String sql);

	UpdateQuery orRaw(String sql, SqlParameter<?>... params);

	UpdateQuery orRaw(RawSqlFragment fragment);

	UpdateQuery eq();

	UpdateQuery supTo();

	UpdateQuery infTo();

	UpdateQuery supOrEqTo();

	UpdateQuery infOrEqTo();

	UpdateQuery in();

	UpdateQuery eq(Column column);

	UpdateQuery notEq(Column column);

	UpdateQuery supTo(Column column);

	UpdateQuery infTo(Column column);

	UpdateQuery supOrEqTo(Column column);

	UpdateQuery infOrEqTo(Column column);

	default UpdateQuery eq(TableDescriptor<?> descriptor) {
		return eq(descriptor.column());
	}

	default UpdateQuery notEq(TableDescriptor<?> descriptor) {
		return notEq(descriptor.column());
	}

	default UpdateQuery supTo(TableDescriptor<?> descriptor) {
		return supTo(descriptor.column());
	}

	default UpdateQuery infTo(TableDescriptor<?> descriptor) {
		return infTo(descriptor.column());
	}

	default UpdateQuery supOrEqTo(TableDescriptor<?> descriptor) {
		return supOrEqTo(descriptor.column());
	}

	default UpdateQuery infOrEqTo(TableDescriptor<?> descriptor) {
		return infOrEqTo(descriptor.column());
	}

	default UpdateQuery eq(ColumnRef<?> descriptor) {
		return eq(descriptor.column());
	}

	default UpdateQuery notEq(ColumnRef<?> descriptor) {
		return notEq(descriptor.column());
	}

	default UpdateQuery supTo(ColumnRef<?> descriptor) {
		return supTo(descriptor.column());
	}

	default UpdateQuery infTo(ColumnRef<?> descriptor) {
		return infTo(descriptor.column());
	}

	default UpdateQuery supOrEqTo(ColumnRef<?> descriptor) {
		return supOrEqTo(descriptor.column());
	}

	default UpdateQuery infOrEqTo(ColumnRef<?> descriptor) {
		return infOrEqTo(descriptor.column());
	}

	UpdateQuery eq(String value);

	UpdateQuery notEq(String value);

	UpdateQuery like(String value);

	UpdateQuery notLike(String value);

	UpdateQuery eq(SqlParameter<?> parameter);

	UpdateQuery notEq(SqlParameter<?> parameter);

	UpdateQuery like(SqlParameter<?> parameter);

	UpdateQuery notLike(SqlParameter<?> parameter);

	UpdateQuery between(String lower, String upper);

	UpdateQuery between(SqlParameter<?> lower, SqlParameter<?> upper);

	UpdateQuery supTo(String value);

	UpdateQuery infTo(String value);

	UpdateQuery supOrEqTo(String value);

	UpdateQuery infOrEqTo(String value);

	UpdateQuery supTo(SqlParameter<?> parameter);

	UpdateQuery infTo(SqlParameter<?> parameter);

	UpdateQuery supOrEqTo(SqlParameter<?> parameter);

	UpdateQuery infOrEqTo(SqlParameter<?> parameter);

	UpdateQuery in(String... value);

	UpdateQuery notIn(String... value);

	UpdateQuery eq(Integer value);

	UpdateQuery notEq(Integer value);

	UpdateQuery between(Integer lower, Integer upper);

	UpdateQuery supTo(Integer value);

	UpdateQuery infTo(Integer value);

	UpdateQuery supOrEqTo(Integer value);

	UpdateQuery infOrEqTo(Integer value);

	UpdateQuery in(Integer... value);

	UpdateQuery notIn(Integer... value);

	UpdateQuery eq(Date value);

	UpdateQuery notEq(Date value);

	UpdateQuery between(Date lower, Date upper);

	UpdateQuery supTo(Date value);

	UpdateQuery infTo(Date value);

	UpdateQuery supOrEqTo(Date value);

	UpdateQuery infOrEqTo(Date value);

	UpdateQuery in(Date... value);

	UpdateQuery notIn(Date... value);

	UpdateQuery eq(Double value);

	UpdateQuery notEq(Double value);

	UpdateQuery between(Double lower, Double upper);

	UpdateQuery supTo(Double value);

	UpdateQuery infTo(Double value);

	UpdateQuery supOrEqTo(Double value);

	UpdateQuery infOrEqTo(Double value);

	UpdateQuery in(Double... value);

	UpdateQuery notIn(Double... value);

	UpdateQuery isNull();

	UpdateQuery isNotNull();

	UpdateQuery eq(Query subquery);

	UpdateQuery notEq(Query subquery);

	UpdateQuery supTo(Query subquery);

	UpdateQuery infTo(Query subquery);

	UpdateQuery supOrEqTo(Query subquery);

	UpdateQuery infOrEqTo(Query subquery);

	UpdateQuery in(Query subquery);

	UpdateQuery notIn(Query subquery);

	UpdateQuery exists(Query subquery);

	UpdateQuery notExists(Query subquery);

	default UpdateQuery whereOptionalEquals(Column column, SqlParameter<?> parameter) {
		return condition(OptionalConditions.optionalEquals(column, parameter));
	}

	default UpdateQuery whereOptionalEquals(TableDescriptor<?> descriptor, SqlParameter<?> parameter) {
		return whereOptionalEquals(descriptor.column(), parameter);
	}

	default UpdateQuery whereOptionalEquals(ColumnRef<?> descriptor, SqlParameter<?> parameter) {
		return whereOptionalEquals(descriptor.column(), parameter);
	}

	default UpdateQuery whereOptionalLike(Column column, SqlParameter<?> parameter) {
		return condition(OptionalConditions.optionalLike(column, parameter));
	}

	default UpdateQuery whereOptionalLike(TableDescriptor<?> descriptor, SqlParameter<?> parameter) {
		return whereOptionalLike(descriptor.column(), parameter);
	}

	default UpdateQuery whereOptionalLike(ColumnRef<? extends CharSequence> descriptor, SqlParameter<?> parameter) {
		return whereOptionalLike(descriptor.column(), parameter);
	}

	default UpdateQuery whereOptionalGreaterOrEqual(Column column, SqlParameter<?> parameter) {
		return condition(OptionalConditions.optionalGreaterOrEq(column, parameter));
	}

	default UpdateQuery whereOptionalGreaterOrEqual(TableDescriptor<?> descriptor, SqlParameter<?> parameter) {
		return whereOptionalGreaterOrEqual(descriptor.column(), parameter);
	}

	default UpdateQuery whereOptionalGreaterOrEqual(ColumnRef<? extends Number> descriptor,
			SqlParameter<?> parameter) {
		return whereOptionalGreaterOrEqual(descriptor.column(), parameter);
	}

	Table target();
}
