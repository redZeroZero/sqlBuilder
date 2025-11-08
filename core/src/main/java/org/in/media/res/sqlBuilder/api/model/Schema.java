package org.in.media.res.sqlBuilder.api.model;

import java.util.List;

import org.in.media.res.sqlBuilder.api.query.Dialect;

public interface Schema {

	String getName();

	void setName(String name);

	List<Table> getTables();

	Table getTableBy(Class<?> clazz);

	Table getTableBy(String name);

	Dialect getDialect();

	void setDialect(Dialect dialect);
}
