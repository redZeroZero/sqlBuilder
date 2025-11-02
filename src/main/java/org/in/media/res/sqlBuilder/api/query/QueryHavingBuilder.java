package org.in.media.res.sqlBuilder.api.query;

import java.util.Date;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;

public interface QueryHavingBuilder extends HavingBuilder {

	@Override
	Query eq(String value);

	@Override
	Query eq(Number value);

	@Override
	Query eq(Date value);

	@Override
	Query in(String... values);

	@Override
	Query in(Number... values);

	@Override
	Query supTo(Number value);

	@Override
	Query supOrEqTo(Number value);

	@Override
	Query infTo(Number value);

	@Override
	Query infOrEqTo(Number value);

	@Override
	Query supTo(Column column);

	@Override
	default Query supTo(TableDescriptor<?> descriptor) {
		return supTo(descriptor.column());
	}

	@Override
	Query supOrEqTo(Column column);

	@Override
	default Query supOrEqTo(TableDescriptor<?> descriptor) {
		return supOrEqTo(descriptor.column());
	}

	@Override
	Query infTo(Column column);

	@Override
	default Query infTo(TableDescriptor<?> descriptor) {
		return infTo(descriptor.column());
	}

	@Override
	Query infOrEqTo(Column column);

	@Override
	default Query infOrEqTo(TableDescriptor<?> descriptor) {
		return infOrEqTo(descriptor.column());
	}

	@Override
	QueryHavingBuilder and(Column column);

	@Override
	default QueryHavingBuilder and(TableDescriptor<?> descriptor) {
		return and(descriptor.column());
	}

	@Override
	QueryHavingBuilder or(Column column);

	@Override
	default QueryHavingBuilder or(TableDescriptor<?> descriptor) {
		return or(descriptor.column());
	}

	@Override
	QueryHavingBuilder min(Column column);

	@Override
	default QueryHavingBuilder min(TableDescriptor<?> descriptor) {
		return min(descriptor.column());
	}

	@Override
	QueryHavingBuilder max(Column column);

	@Override
	default QueryHavingBuilder max(TableDescriptor<?> descriptor) {
		return max(descriptor.column());
	}

	@Override
	QueryHavingBuilder sum(Column column);

	@Override
	default QueryHavingBuilder sum(TableDescriptor<?> descriptor) {
		return sum(descriptor.column());
	}

	@Override
	QueryHavingBuilder avg(Column column);

	@Override
	default QueryHavingBuilder avg(TableDescriptor<?> descriptor) {
		return avg(descriptor.column());
	}

	@Override
	QueryHavingBuilder col(Column column);

	@Override
	default QueryHavingBuilder col(TableDescriptor<?> descriptor) {
		return col(descriptor.column());
	}
}
