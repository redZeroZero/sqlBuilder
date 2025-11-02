package org.in.media.res.sqlBuilder.api.query;

import java.util.List;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;

public interface GroupBy extends Clause, Resetable, Transpilable {

	GroupBy groupBy(Column column);

	default GroupBy groupBy(TableDescriptor<?> descriptor) {
		return groupBy(descriptor.column());
	}

	GroupBy groupBy(Column... columns);

	default GroupBy groupBy(TableDescriptor<?>... descriptors) {
		for (TableDescriptor<?> descriptor : descriptors) {
			groupBy(descriptor.column());
		}
		return this;
	}

	List<Column> groupByColumns();

}
