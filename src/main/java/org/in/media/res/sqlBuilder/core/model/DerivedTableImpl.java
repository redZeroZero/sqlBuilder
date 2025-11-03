package org.in.media.res.sqlBuilder.core.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.DerivedTable;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;

/**
 * Simple {@link DerivedTable} implementation backed by a subquery and explicit column aliases.
 */
public final class DerivedTableImpl implements DerivedTable {

	private static final AtomicInteger ALIAS_SEQUENCE = new AtomicInteger();

	private final Query subquery;
	private final String alias;
	private final Map<String, Column> columns = new LinkedHashMap<>();

	public DerivedTableImpl(Query subquery, String alias, String... columnAliases) {
		this.subquery = Objects.requireNonNull(subquery, "subquery");
		this.alias = resolveAlias(alias);
		String[] resolvedAliases = resolveColumnAliases(subquery, columnAliases);
		for (String columnAlias : resolvedAliases) {
			String normalized = validateColumnAlias(columnAlias);
			Column column = ColumnImpl.builder().name(normalized).table(this).build();
			if (this.columns.put(normalized, column) != null) {
				throw new IllegalArgumentException("Duplicate column alias '" + normalized + "' for derived table");
			}
		}
	}

	private static String resolveAlias(String alias) {
		if (alias == null || alias.isBlank()) {
			return "SQ" + ALIAS_SEQUENCE.incrementAndGet();
		}
		return alias;
	}

	private static String validateColumnAlias(String columnAlias) {
		if (columnAlias == null || columnAlias.isBlank()) {
			throw new IllegalArgumentException("Derived table column alias must not be blank");
		}
		return columnAlias;
	}

	private static String[] resolveColumnAliases(Query query, String... provided) {
		int expected = query.aggColumns().size() + query.columns().size();
		if (expected == 0) {
			throw new IllegalArgumentException("Derived table requires the subquery to select at least one column");
		}
		if (provided != null && provided.length > 0) {
			if (provided.length != expected) {
				throw new IllegalArgumentException(
						"Derived table column alias count (" + provided.length + ") does not match subquery selection (" + expected + ")");
			}
			return provided;
		}
		Map<String, Integer> seen = new LinkedHashMap<>();
		ArrayList<String> aliases = new ArrayList<>(expected);
		query.aggColumns().forEach((column, agg) -> aliases.add(uniqueName(suggestAlias(column, agg), seen)));
		query.columns().forEach(column -> aliases.add(uniqueName(suggestAlias(column, null), seen)));
		return aliases.toArray(String[]::new);
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
		int counter = seen.getOrDefault(normalized, 0);
		if (counter > 0) {
			normalized = normalized + "_" + counter;
		}
		seen.put(candidate, counter + 1);
		return normalized;
	}

	@Override
	public Query subquery() {
		return subquery;
	}

	@Override
	public String getName() {
		return "(" + subquery.transpile() + ")";
	}

	@Override
	public String getAlias() {
		return alias;
	}

	@Override
	public boolean hasAlias() {
		return true;
	}

	@Override
	public Column[] getColumns() {
		return columns.values().toArray(new Column[0]);
	}

	@Override
	public Column get(String columnName) {
		return columns.get(columnName);
	}

	@Override
	public Column get(TableDescriptor<?> descriptor) {
		if (descriptor == null) {
			return null;
		}
		return columns.get(descriptor.value());
	}

	@Override
	public void includeSchema(String schema) {
		// Derived tables do not support schema decoration.
	}

	@Override
	public boolean hasTableName() {
		return true;
	}

	@Override
	public String tableName() {
		return alias;
	}

}
