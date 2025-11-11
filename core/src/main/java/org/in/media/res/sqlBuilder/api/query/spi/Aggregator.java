package org.in.media.res.sqlBuilder.api.query.spi;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.api.model.ColumnRef;

public interface Aggregator {

	public Comparator min(Column column);

	default Comparator min(TableDescriptor<?> descriptor) {
		return min(descriptor.column());
	}

	default <T extends Comparable<? super T>> Comparator min(ColumnRef<T> descriptor) {
		return min(descriptor.column());
	}

	public Comparator max(Column column);

	default Comparator max(TableDescriptor<?> descriptor) {
		return max(descriptor.column());
	}

	default <T extends Comparable<? super T>> Comparator max(ColumnRef<T> descriptor) {
		return max(descriptor.column());
	}

	public Comparator sum(Column column);

	default Comparator sum(TableDescriptor<?> descriptor) {
		return sum(descriptor.column());
	}

	default <N extends Number> Comparator sum(ColumnRef<N> descriptor) {
		return sum(descriptor.column());
	}

	public Comparator avg(Column column);

	default Comparator avg(TableDescriptor<?> descriptor) {
		return avg(descriptor.column());
	}

	default <N extends Number> Comparator avg(ColumnRef<N> descriptor) {
		return avg(descriptor.column());
	}

	public Comparator col(Column column);

	default Comparator col(TableDescriptor<?> descriptor) {
		return col(descriptor.column());
	}

	default <T> Comparator col(ColumnRef<T> descriptor) {
		return col(descriptor.column());
	}

}
