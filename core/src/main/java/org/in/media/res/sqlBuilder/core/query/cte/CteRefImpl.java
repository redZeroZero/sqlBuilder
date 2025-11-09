package org.in.media.res.sqlBuilder.core.query.cte;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.api.query.CteRef;
import org.in.media.res.sqlBuilder.core.model.ColumnImpl;

final class CteRefImpl implements CteRef {

	private final String name;
	private final Map<String, Column> columns = new LinkedHashMap<>();

	CteRefImpl(String name, List<String> columnAliases) {
		this.name = Objects.requireNonNull(name, "name");
		if (columnAliases == null || columnAliases.isEmpty()) {
			throw new IllegalArgumentException("CTE '" + name + "' must define at least one column");
		}
		for (String alias : columnAliases) {
			Column column = ColumnImpl.builder().name(alias).table(this).build();
			if (columns.put(alias, column) != null) {
				throw new IllegalArgumentException("Duplicate column alias '" + alias + "' for CTE '" + name + "'");
			}
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getAlias() {
		return null;
	}

	@Override
	public boolean hasAlias() {
		return false;
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
		// CTEs are local to the query; schema decoration is not supported.
	}

	@Override
	public boolean hasTableName() {
		return true;
	}

	@Override
	public String tableName() {
		return name;
	}
}
