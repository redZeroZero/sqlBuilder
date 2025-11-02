package org.in.media.res.sqlBuilder.api.query;

import java.util.List;

import org.in.media.res.sqlBuilder.constants.SortDirection;
import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;

public interface OrderBy extends Clause, Resetable, Transpilable {

	OrderBy orderBy(Column column);

	default OrderBy orderBy(TableDescriptor<?> descriptor) {
		return orderBy(descriptor.column());
	}

	OrderBy orderBy(Column column, SortDirection direction);

	default OrderBy orderBy(TableDescriptor<?> descriptor, SortDirection direction) {
		return orderBy(descriptor.column(), direction);
	}

	OrderBy asc(Column column);

	default OrderBy asc(TableDescriptor<?> descriptor) {
		return asc(descriptor.column());
	}

	OrderBy desc(Column column);

	default OrderBy desc(TableDescriptor<?> descriptor) {
		return desc(descriptor.column());
	}

	List<Ordering> orderings();

	record Ordering(Column column, SortDirection direction) {
	}
}
