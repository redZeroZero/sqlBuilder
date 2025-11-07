package org.in.media.res.sqlBuilder.api.query;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;

public interface Joinable {

	From on(Column c1, Column c2);

	default From on(TableDescriptor<?> left, TableDescriptor<?> right) {
		return on(left.column(), right.column());
	}

	default From on(TableDescriptor<?> left, Column right) {
		return on(left.column(), right);
	}

	default From on(Column left, TableDescriptor<?> right) {
		return on(left, right.column());
	}

}
