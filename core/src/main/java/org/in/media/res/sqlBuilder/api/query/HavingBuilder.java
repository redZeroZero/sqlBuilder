package org.in.media.res.sqlBuilder.api.query;

import java.util.Date;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;

public interface HavingBuilder {

	Having eq(String value);

	Having eq(Number value);

	Having eq(Date value);

	Having eq(SqlParameter<?> parameter);

	Having notEq(String value);

	Having notEq(Number value);

	Having notEq(Date value);

	Having notEq(SqlParameter<?> parameter);

	Having in(String... values);

	Having in(Number... values);

	Having in(Date... values);

	Having notIn(String... values);

	Having notIn(Number... values);

	Having notIn(Date... values);

	Having like(String value);

	Having notLike(String value);

	Having between(Number lower, Number upper);

	Having between(Date lower, Date upper);

	Having between(SqlParameter<?> lower, SqlParameter<?> upper);

	Having isNull();

	Having isNotNull();

	Having eq(Query subquery);

	Having notEq(Query subquery);

	Having in(Query subquery);

	Having notIn(Query subquery);

	Having supTo(Query subquery);

	Having supOrEqTo(Query subquery);

	Having infTo(Query subquery);

	Having infOrEqTo(Query subquery);

	Having supTo(SqlParameter<?> parameter);

	Having supOrEqTo(SqlParameter<?> parameter);

	Having infTo(SqlParameter<?> parameter);

	Having infOrEqTo(SqlParameter<?> parameter);

	Having exists(Query subquery);

	Having notExists(Query subquery);

	Having supTo(Number value);

	Having supOrEqTo(Number value);

	Having infTo(Number value);

	Having infOrEqTo(Number value);

	Having supTo(Column column);

	default Having supTo(TableDescriptor<?> descriptor) {
		return supTo(descriptor.column());
	}

	Having supOrEqTo(Column column);

	default Having supOrEqTo(TableDescriptor<?> descriptor) {
		return supOrEqTo(descriptor.column());
	}

	Having infTo(Column column);

	default Having infTo(TableDescriptor<?> descriptor) {
		return infTo(descriptor.column());
	}

	Having infOrEqTo(Column column);

	default Having infOrEqTo(TableDescriptor<?> descriptor) {
		return infOrEqTo(descriptor.column());
	}

	Having notEq(Column column);

	default Having notEq(TableDescriptor<?> descriptor) {
		return notEq(descriptor.column());
	}

	HavingBuilder and(Column column);

	default HavingBuilder and(TableDescriptor<?> descriptor) {
		return and(descriptor.column());
	}

	HavingBuilder or(Column column);

	default HavingBuilder or(TableDescriptor<?> descriptor) {
		return or(descriptor.column());
	}

	HavingBuilder min(Column column);

	default HavingBuilder min(TableDescriptor<?> descriptor) {
		return min(descriptor.column());
	}

	HavingBuilder max(Column column);

	default HavingBuilder max(TableDescriptor<?> descriptor) {
		return max(descriptor.column());
	}

	HavingBuilder sum(Column column);

	default HavingBuilder sum(TableDescriptor<?> descriptor) {
		return sum(descriptor.column());
	}

	HavingBuilder avg(Column column);

	default HavingBuilder avg(TableDescriptor<?> descriptor) {
		return avg(descriptor.column());
	}

	HavingBuilder col(Column column);

	default HavingBuilder col(TableDescriptor<?> descriptor) {
		return col(descriptor.column());
	}
}
