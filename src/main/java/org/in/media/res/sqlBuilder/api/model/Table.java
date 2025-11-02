package org.in.media.res.sqlBuilder.api.model;

public interface Table {

	public String getName();

	public String getAlias();

	public boolean hasAlias();

	public Column[] getColumns();

	public Column get(String columnName);

	public Column get(TableDescriptor<?> descriptor);

	public void includeSchema(String schema);

	public boolean hasTableName();

	public String tableName();

}
