package org.in.media.res.sqlBuilder.implementation;

import java.util.HashMap;
import java.util.Map;

import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.model.ITable;
import org.in.media.res.sqlBuilder.interfaces.model.ITableDescriptor;

public class Table<T extends ITableDescriptor<T>> implements ITable {

	private Map<String, IColumn> cols = new HashMap<>();

	private String DESC_RES_T_ALIAS = "T_ALIAS";

	private String name;

	private String alias;

	private String schema;

	public Table(T[] descriptor) {
		this(descriptor, null);
	}

	public Table(T[] descriptor, String schema) {
		this.name = descriptor[0].getClass().getSimpleName();
		for (T v : descriptor) {
			fillValues(v);
		}
	}

	private void fillValues(T v) {
		if (DESC_RES_T_ALIAS.equals(v.fieldName()))
			this.alias = v.alias();
		else
			cols.put(v.value(), Column.builder().name(v.value()).alias(v.alias()).table(this).build());
	}

	public IColumn[] getColumns() {
		return cols.values().toArray(new Column[cols.values().size()]);
	}

	public IColumn get(ITableDescriptor<?> descriptor) {
		return this.get(descriptor.value());
	}

	public IColumn get(String name) {
		return cols.get(name);
	}

	public String getName() {
		return name;
	}

	public void includeSchema(String schema) {
		this.schema = schema;
	}

	public String getSchema() {
		return schema;
	}

	public String getAlias() {
		return alias;
	}

	public boolean hasAlias() {
		return getAlias() != null && !getAlias().isEmpty() && !getAlias().isBlank();
	}

}
