package org.in.media.res.sqlBuilder.interfaces.model;


public interface ITable {

	public String getName();

	public String getAlias();

	public boolean hasAlias();
	
	public IColumn[] getColumns();

	public IColumn get(String columnName);

	public IColumn get(ITableDescriptor<?> descriptor);
	
	public void includeSchema(String schema);

}
