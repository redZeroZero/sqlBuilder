package org.in.media.res.sqlBuilder.core.model.samples;

import org.in.media.res.sqlBuilder.api.model.ColumnRef;

public interface AnnotatedCustomerColumns {
	ColumnRef<Long> ID();

	ColumnRef<String> FIRST_NAME();

	ColumnRef<String> LAST_NAME();
}
