package org.in.media.res.sqlBuilder.api.query;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Convenience wrapper to declare multiple CTEs and attach the main query in a single fluent chain.
 * Stores declared {@link CteRef}s by name so downstream code can resolve them without keeping locals.
 */
public final class WithChain {

	private final WithBuilder delegate;
	private final Map<String, CteRef> refs = new LinkedHashMap<>();

	WithChain(WithBuilder delegate) {
		this.delegate = Objects.requireNonNull(delegate, "delegate");
	}

	/**
	 * Register a CTE and keep its {@link CteRef} under the given name.
	 *
	 * @param name the CTE identifier (must be unique within the builder)
	 * @param query the query that defines the CTE body
	 * @param columnAliases ordered aliases matching the subquery projection
	 * @return this chain for further CTE declarations
	 */
	public WithChain cte(String name, Query query, String... columnAliases) {
		CteRef ref = delegate.cte(name, query, columnAliases);
		refs.put(name, ref);
		return this;
	}

	/**
	 * Resolve a {@link CteRef} by its registered name.
	 *
	 * @param name the CTE identifier
	 * @return the previously registered {@link CteRef}
	 * @throws IllegalArgumentException if no CTE with the given name was registered
	 */
	public CteRef ref(String name) {
		CteRef ref = refs.get(name);
		if (ref == null) {
			throw new IllegalArgumentException("Unknown CTE '" + name + "'");
		}
		return ref;
	}

	/**
	 * Attach all registered CTEs to the provided main query.
	 *
	 * @param query the main query to execute after the WITH block
	 * @return the same query instance augmented with the configured CTEs
	 */
	public Query main(Query query) {
		return delegate.main(query);
	}

	/**
	 * Build the main query using the chain (to resolve {@link CteRef}s by name) and attach all registered CTEs.
	 *
	 * @param builder a function that receives this chain and returns the main query
	 * @return the main query with attached CTEs
	 */
	public Query main(Function<WithChain, Query> builder) {
		return main(builder.apply(this));
	}

	/**
	 * Attach all registered CTEs to the provided main query and expose render/compile helpers.
	 *
	 * @param query the main query to execute after the WITH block
	 * @return a result wrapper with convenience methods
	 */
	public WithChainResult attach(Query query) {
		return new WithChainResult(main(query));
	}

	/**
	 * Attach all registered CTEs to the main query produced by the given builder, exposing render/compile helpers.
	 * See README “CTEs” section for example usage chaining multiple CTEs and attaching in one expression.
	 *
	 * @param builder a function that receives this chain and returns the main query
	 * @return a result wrapper with convenience methods
	 */
	public WithChainResult attach(Function<WithChain, Query> builder) {
		return attach(builder.apply(this));
	}

	/**
	 * Result wrapper that exposes render/compile on a query with attached CTEs.
	 */
	public static final class WithChainResult {

		private final Query query;

		private WithChainResult(Query query) {
			this.query = Objects.requireNonNull(query, "query");
		}

		public SqlAndParams render() {
			return query.render();
		}

		public CompiledQuery compile() {
			return query.compile();
		}

		public Query query() {
			return query;
		}
	}
}
