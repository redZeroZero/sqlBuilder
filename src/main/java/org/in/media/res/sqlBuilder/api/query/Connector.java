package org.in.media.res.sqlBuilder.api.query;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;

public interface Connector {

	public Comparator and(Column column);

	default Comparator and(TableDescriptor<?> descriptor) {
		return and(descriptor.column());
	}

	public Comparator or(Column column);

	default Comparator or(TableDescriptor<?> descriptor) {
		return or(descriptor.column());
	}

	public Aggregator and();

	public Aggregator or();
}
