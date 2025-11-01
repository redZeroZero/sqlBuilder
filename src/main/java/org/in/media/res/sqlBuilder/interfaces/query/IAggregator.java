package org.in.media.res.sqlBuilder.interfaces.query;

import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.model.ITableDescriptor;

public interface IAggregator {

	public IComparator min(IColumn column);

	default IComparator min(ITableDescriptor<?> descriptor) {
		return min(descriptor.column());
	}

	public IComparator max(IColumn column);

	default IComparator max(ITableDescriptor<?> descriptor) {
		return max(descriptor.column());
	}

	public IComparator sum(IColumn column);

	default IComparator sum(ITableDescriptor<?> descriptor) {
		return sum(descriptor.column());
	}

	public IComparator avg(IColumn column);

	default IComparator avg(ITableDescriptor<?> descriptor) {
		return avg(descriptor.column());
	}

	public IComparator col(IColumn column);

	default IComparator col(ITableDescriptor<?> descriptor) {
		return col(descriptor.column());
	}

}
