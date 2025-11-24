package org.in.media.res.sqlBuilder.api.query;

/**
 * Fluent entry point for composing queries that start with a WITH / CTE block.
 */
public interface WithBuilder {

	/**
	 * Begin declaring a CTE by name, returning a step that accepts the body and aliases.
	 *
	 * @param name the CTE identifier (must be unique within the builder)
	 * @return a declaration step that can register the CTE body and aliases
	 */
	CteDeclarationStep with(String name);

	/**
	 * Declare a Common Table Expression (CTE) with the given name and subquery, returning
	 * a chaining step for SQL-like CTE composition.
	 *
	 * @param name the CTE identifier (must be unique within the builder)
	 * @param query the query that defines the CTE body
	 * @param columnAliases ordered aliases matching the subquery projection
	 * @return a {@link CteStep} exposing the registered {@link CteRef} and the builder to continue chaining
	 */
	CteStep with(String name, Query query, String... columnAliases);

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

	/**
	 * Step returned when declaring a CTE via {@link #with(String, Query, String...)}.
	 * Exposes the registered {@link CteRef} and the builder to continue chaining declarations.
	 */
	interface CteStep {

		/**
	 * @return the {@link CteRef} descriptor for the registered CTE
	 */
	CteRef ref();

	/**
	 * @return the same {@link WithBuilder} to continue declaring CTEs or attach a main query
	 */
	WithBuilder and();
	}

	/**
	 * Step returned when starting a CTE declaration via {@link #with(String)}. Accepts
	 * the CTE body and aliases, returning a {@link CteStep} to continue chaining or access the {@link CteRef}.
	 */
	interface CteDeclarationStep {

		/**
		 * @param query the query that defines the CTE body
		 * @param columnAliases ordered aliases matching the subquery projection
		 * @return a {@link CteStep} exposing the registered CTE and builder for further chaining
		 */
		CteStep as(Query query, String... columnAliases);
	}
}
