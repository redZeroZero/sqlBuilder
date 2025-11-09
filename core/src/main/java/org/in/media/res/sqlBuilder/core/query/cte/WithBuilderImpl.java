package org.in.media.res.sqlBuilder.core.query.cte;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.in.media.res.sqlBuilder.api.query.CteRef;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.WithBuilder;
import org.in.media.res.sqlBuilder.core.query.QueryImpl;
import org.in.media.res.sqlBuilder.core.query.SelectionAliasResolver;

public final class WithBuilderImpl implements WithBuilder {

	private final Map<String, CteRefImpl> registry = new LinkedHashMap<>();
	private final List<QueryImpl.CteDeclaration> declarations = new ArrayList<>();

	@Override
	public CteRef cte(String name, Query query) {
		return cte(name, query, new String[0]);
	}

	@Override
	public CteRef cte(String name, Query query, String... columnAliases) {
		String normalizedName = validateName(name);
		if (registry.containsKey(normalizedName)) {
			throw new IllegalArgumentException("Duplicate CTE name '" + normalizedName + "'");
		}
		QueryImpl queryImpl = asQueryImpl(query);
		List<String> resolvedAliases = SelectionAliasResolver.resolve(queryImpl, columnAliases);
		CteRefImpl ref = new CteRefImpl(normalizedName, resolvedAliases);
		declarations.add(new QueryImpl.CteDeclaration(normalizedName, queryImpl, resolvedAliases));
		registry.put(normalizedName, ref);
		return ref;
	}

	@Override
	public Query main(Query query) {
		QueryImpl queryImpl = asQueryImpl(query);
		queryImpl.withClauses(declarations);
		return queryImpl;
	}

	private static QueryImpl asQueryImpl(Query query) {
		if (query instanceof QueryImpl impl) {
			return impl;
		}
		throw new IllegalArgumentException("Unsupported query implementation: " + query);
	}

	private static String validateName(String name) {
		String normalized = Objects.requireNonNull(name, "name").trim();
		if (normalized.isEmpty()) {
			throw new IllegalArgumentException("CTE name must not be blank");
		}
		return normalized;
	}
}
