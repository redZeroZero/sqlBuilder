package org.in.media.res.sqlBuilder.api.query;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;

public interface Aggregator {

	public Comparator min(Column column);

	default Comparator min(TableDescriptor<?> descriptor) {
		return min(descriptor.column());
	}

	public Comparator max(Column column);

	default Comparator max(TableDescriptor<?> descriptor) {
		return max(descriptor.column());
	}

	public Comparator sum(Column column);

	default Comparator sum(TableDescriptor<?> descriptor) {
		return sum(descriptor.column());
	}

	public Comparator avg(Column column);

	default Comparator avg(TableDescriptor<?> descriptor) {
		return avg(descriptor.column());
	}

	public Comparator col(Column column);

	default Comparator col(TableDescriptor<?> descriptor) {
		return col(descriptor.column());
	}

}
