package org.in.media.res.sqlBuilder.core.model;

import java.util.Objects;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;

public final class ColumnRef implements TableDescriptor<ColumnRef> {

	private final String name;
	private final String alias;
	private Column column;

	private ColumnRef(String name, String alias) {
		this.name = Objects.requireNonNull(name, "name");
		this.alias = alias;
	}

	public static ColumnRef of(String name) {
		return new ColumnRef(name, null);
	}

	public static ColumnRef of(String name, String alias) {
		return new ColumnRef(name, alias);
	}

	@Override
	public String value() {
		return name;
	}

	@Override
	public String alias() {
		return alias;
	}

	@Override
	public String fieldName() {
		return name;
	}

	@Override
	public void bindColumn(Column column) {
		this.column = column;
	}

	@Override
	public Column column() {
		if (column == null) {
			throw new IllegalStateException("Column not bound for descriptor '" + name + "'");
		}
		return column;
	}
}
