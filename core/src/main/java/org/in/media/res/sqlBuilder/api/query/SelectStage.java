package org.in.media.res.sqlBuilder.api.query;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.core.model.ColumnRef;

/**
 * Stage exposing projection-oriented operations before a {@code FROM} clause is
 * introduced.
 */
public interface SelectStage extends QueryStage, Select {

	@Override
	SelectStage select(Column column);

	@Override
	SelectStage select(TableDescriptor<?> descriptor);

	@Override
	SelectStage select(Column... columns);

	@Override
	SelectStage select(TableDescriptor<?>... descriptors);

	@Override
	SelectStage select(Table table);

	@Override
	SelectStage select(AggregateOperator agg, Column column);

	@Override
	SelectStage select(AggregateOperator agg, TableDescriptor<?> descriptor);

	@Override
	SelectStage distinct();

	SelectStage count();

	SelectStage count(Column column);

	SelectStage count(TableDescriptor<?> descriptor);

	@Override
	SelectStage hint(String hintSql);

	FromStage from(Table table);

	FromStage from(Table... tables);

	default SelectStage select(ColumnRef<?> descriptor) {
		return select(descriptor.column());
	}

	default SelectStage select(ColumnRef<?>... descriptors) {
		for (ColumnRef<?> descriptor : descriptors) {
			select(descriptor);
		}
		return this;
	}

	default SelectStage select(AggregateOperator agg, ColumnRef<?> descriptor) {
		return select(agg, descriptor.column());
	}
}
