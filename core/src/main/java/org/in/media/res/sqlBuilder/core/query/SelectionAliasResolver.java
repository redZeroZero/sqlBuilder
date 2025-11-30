package org.in.media.res.sqlBuilder.core.query;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.query.RawSqlFragment;
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
		int projectedColumns = query.aggColumns().size() + query.columns().size() + extraProjectionCount(query);
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
		if (query instanceof QueryImpl impl) {
			for (SelectProjectionSupport.SelectProjection projection : impl.projections()) {
				if (projection.type() == SelectProjectionSupport.ProjectionType.RAW) {
					resolved.add(uniqueName(suggestRawAlias(projection, seen.size()), seen));
				} else if (projection.type() == SelectProjectionSupport.ProjectionType.WINDOW) {
					resolved.add(uniqueName(suggestWindowAlias(projection, seen.size()), seen));
				}
			}
		}
		return List.copyOf(resolved);
	}

	private static int extraProjectionCount(Query query) {
		if (query instanceof QueryImpl impl) {
			int count = 0;
			for (SelectProjectionSupport.SelectProjection projection : impl.projections()) {
				if (projection.type() == SelectProjectionSupport.ProjectionType.RAW
						|| projection.type() == SelectProjectionSupport.ProjectionType.WINDOW) {
					count++;
				}
			}
			return count;
		}
		return 0;
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

	private static String suggestRawAlias(SelectProjectionSupport.SelectProjection projection, int position) {
		RawSqlFragment fragment = projection.fragment();
		if (fragment != null) {
			String sql = fragment.sql();
			if (sql != null) {
				String trimmed = sql.trim();
				int asIndex = trimmed.toUpperCase().lastIndexOf(" AS ");
				if (asIndex > -1 && asIndex + 4 < trimmed.length()) {
					return trimmed.substring(asIndex + 4).replaceAll("[^A-Za-z0-9_]", "");
				}
			}
		}
		return "RAW_COL_" + position;
	}

	private static String suggestWindowAlias(SelectProjectionSupport.SelectProjection projection, int position) {
		if (projection.windowFunction() != null) {
			String alias = projection.windowFunction().alias();
			if (alias != null && !alias.isBlank()) {
				return alias;
			}
			String logical = projection.windowFunction().logicalName();
			if (logical != null && !logical.isBlank()) {
				return logical;
			}
		}
		return "WINDOW_COL_" + position;
	}
}
