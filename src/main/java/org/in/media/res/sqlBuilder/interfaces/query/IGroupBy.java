package org.in.media.res.sqlBuilder.interfaces.query;

import java.util.List;

import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.model.ITableDescriptor;

public interface IGroupBy extends IClause, IResetable, ITranspilable {

	IGroupBy groupBy(IColumn column);

	default IGroupBy groupBy(ITableDescriptor<?> descriptor) {
		return groupBy(descriptor.column());
	}

	IGroupBy groupBy(IColumn... columns);

	default IGroupBy groupBy(ITableDescriptor<?>... descriptors) {
		for (ITableDescriptor<?> descriptor : descriptors) {
			groupBy(descriptor.column());
		}
		return this;
	}

	List<IColumn> groupByColumns();

}
