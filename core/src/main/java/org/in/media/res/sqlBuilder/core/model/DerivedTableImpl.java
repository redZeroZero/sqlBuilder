package org.in.media.res.sqlBuilder.core.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.DerivedTable;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.core.query.SelectionAliasResolver;

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
		List<String> resolvedAliases = SelectionAliasResolver.resolve(subquery, columnAliases);
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
