package org.in.media.res.sqlBuilder.interfaces.query;

import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.model.ITableDescriptor;

public interface IJoinable {

	IFrom on(IColumn c1, IColumn c2);

	default IFrom on(ITableDescriptor<?> left, ITableDescriptor<?> right) {
		return on(left.column(), right.column());
	}

}
