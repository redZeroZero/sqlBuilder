package org.in.media.res.sqlBuilder.interfaces.query;

import java.util.List;

import org.in.media.res.sqlBuilder.constants.SortDirection;
import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.model.ITableDescriptor;

public interface IOrderBy extends IClause, IResetable, ITranspilable {

	IOrderBy orderBy(IColumn column);

	default IOrderBy orderBy(ITableDescriptor<?> descriptor) {
		return orderBy(descriptor.column());
	}

	IOrderBy orderBy(IColumn column, SortDirection direction);

	default IOrderBy orderBy(ITableDescriptor<?> descriptor, SortDirection direction) {
		return orderBy(descriptor.column(), direction);
	}

	IOrderBy asc(IColumn column);

	default IOrderBy asc(ITableDescriptor<?> descriptor) {
		return asc(descriptor.column());
	}

	IOrderBy desc(IColumn column);

	default IOrderBy desc(ITableDescriptor<?> descriptor) {
		return desc(descriptor.column());
	}

	List<Ordering> orderings();

	record Ordering(IColumn column, SortDirection direction) {
	}
}
