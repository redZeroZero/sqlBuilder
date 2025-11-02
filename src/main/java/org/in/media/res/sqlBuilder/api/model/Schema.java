package org.in.media.res.sqlBuilder.api.model;

import java.util.List;

public interface Schema {

	public String getName();

	public void setName(String name);

	public List<Table> getTables();

	public Table getTableBy(Class<?> clazz);

	public Table getTableBy(String name);
	
}
