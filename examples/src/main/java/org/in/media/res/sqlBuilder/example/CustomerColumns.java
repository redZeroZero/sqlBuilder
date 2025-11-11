package org.in.media.res.sqlBuilder.example;

import java.time.LocalDate;

import org.in.media.res.sqlBuilder.api.model.ColumnRef;
import org.in.media.res.sqlBuilder.api.model.TableFacets;

/**
 * Generated customer column view (checked in so tests can reference typed
 * accessors without requiring annotation processing at build time).
 */
public interface CustomerColumns {

	ColumnRef<Long> ID();

	ColumnRef<String> FIRST_NAME();

	ColumnRef<String> LAST_NAME();

	ColumnRef<String> EMAIL();

	ColumnRef<Boolean> ACTIVE();

	ColumnRef<LocalDate> CREATED_AT();

	static CustomerColumns of(TableFacets.Facet facet) {
		return new CustomerColumnsImpl(facet);
	}
}
