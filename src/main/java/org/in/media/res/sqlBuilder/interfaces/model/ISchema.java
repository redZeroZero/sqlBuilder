package org.in.media.res.sqlBuilder.interfaces.model;

import java.util.List;

public interface ISchema {

	public String getName();

	public void setName(String name);

	public List<ITable> getTables();

	public ITable getTableBy(Class<?> clazz);

	public ITable getTableBy(String name);
	
}
