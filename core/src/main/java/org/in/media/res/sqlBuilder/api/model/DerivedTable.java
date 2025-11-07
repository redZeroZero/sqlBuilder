package org.in.media.res.sqlBuilder.api.model;

import org.in.media.res.sqlBuilder.api.query.Query;

/**
 * Represents a derived table backed by a subquery.
 */
public interface DerivedTable extends Table {

	/**
	 * Returns the underlying query providing rows for this derived table.
	 */
	Query subquery();

}
