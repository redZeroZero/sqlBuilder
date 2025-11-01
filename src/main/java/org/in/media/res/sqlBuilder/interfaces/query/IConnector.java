package org.in.media.res.sqlBuilder.interfaces.query;

import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.model.ITableDescriptor;

public interface IConnector {

	public IComparator and(IColumn column);

	default IComparator and(ITableDescriptor<?> descriptor) {
		return and(descriptor.column());
	}

	public IComparator or(IColumn column);

	default IComparator or(ITableDescriptor<?> descriptor) {
		return or(descriptor.column());
	}

	public IAggregator and();

	public IAggregator or();
}
