package org.in.media.res.sqlBuilder.core.query;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;

/**
 * Utility that derives column aliases for queries based on their projection.
 */
public final class SelectionAliasResolver {

	private SelectionAliasResolver() {
	}

	public static List<String> resolve(Query query, String... providedAliases) {
		Objects.requireNonNull(query, "query");
		int projectedColumns = query.aggColumns().size() + query.columns().size();
		if (projectedColumns == 0) {
			throw new IllegalArgumentException("CTE/derived table requires the subquery to select at least one column");
		}
		if (providedAliases != null && providedAliases.length > 0) {
			if (providedAliases.length != projectedColumns) {
				throw new IllegalArgumentException("Column alias count (" + providedAliases.length
						+ ") does not match subquery selection (" + projectedColumns + ")");
			}
			List<String> normalized = new ArrayList<>(providedAliases.length);
			for (String alias : providedAliases) {
				normalized.add(validateAlias(alias));
			}
			return List.copyOf(normalized);
		}
		Map<String, Integer> seen = new LinkedHashMap<>();
		List<String> resolved = new ArrayList<>(projectedColumns);
		query.aggColumns().forEach((column, agg) -> resolved.add(uniqueName(suggestAlias(column, agg), seen)));
		query.columns().forEach(column -> resolved.add(uniqueName(suggestAlias(column, null), seen)));
		return List.copyOf(resolved);
	}

	private static String validateAlias(String alias) {
		if (alias == null || alias.isBlank()) {
			throw new IllegalArgumentException("Column alias must not be blank");
		}
		return alias;
	}

	private static String suggestAlias(Column column, AggregateOperator aggregate) {
		String base = column.hasColumnAlias() ? column.getAlias() : column.getName();
		if (base == null || base.isBlank()) {
			base = column.transpile(false).replace('.', '_');
		}
		if (aggregate != null) {
			String prefix = aggregate.name();
			if (!base.toUpperCase().startsWith(prefix)) {
				base = prefix + "_" + base;
			}
		}
		return base;
	}

	private static String uniqueName(String candidate, Map<String, Integer> seen) {
		String normalized = candidate;
		int counter = seen.getOrDefault(candidate, 0);
		if (counter > 0) {
			normalized = candidate + "_" + counter;
		}
		seen.put(candidate, counter + 1);
		return normalized;
	}
}
