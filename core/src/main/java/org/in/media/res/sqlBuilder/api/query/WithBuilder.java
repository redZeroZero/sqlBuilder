package org.in.media.res.sqlBuilder.api.query;

/**
 * Fluent entry point for composing queries that start with a WITH / CTE block.
 */
public interface WithBuilder {

	/**
	 * Declare a Common Table Expression (CTE) with the given name and subquery.
	 *
	 * @param name the CTE identifier (must be unique within the builder)
	 * @param query the query that defines the CTE body
	 * @return a {@link CteRef} descriptor that can be used as a table in subsequent queries
	 */
	CteRef cte(String name, Query query);

	/**
	 * Declare a CTE with explicit column aliases.
	 *
	 * @param name the CTE identifier (must be unique within the builder)
	 * @param query the query that defines the CTE body
	 * @param columnAliases ordered aliases matching the subquery projection
	 * @return a {@link CteRef} descriptor that can be used as a table in subsequent queries
	 */
	CteRef cte(String name, Query query, String... columnAliases);

	/**
	 * Attach the previously declared CTEs to the supplied query, producing a {@link Query}
	 * that renders the WITH block followed by the query body.
	 *
	 * @param query the main query to execute after the WITH block
	 * @return the same query instance augmented with the configured CTEs
	 */
	Query main(Query query);
}
