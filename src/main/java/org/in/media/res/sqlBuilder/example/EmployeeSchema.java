package org.in.media.res.sqlBuilder.example;

import java.util.List;

import org.in.media.res.sqlBuilder.implementation.Table;
import org.in.media.res.sqlBuilder.interfaces.model.ISchema;
import org.in.media.res.sqlBuilder.interfaces.model.ITable;

public class EmployeeSchema implements ISchema {

	private List<ITable> tables = List.of(new Table<Employee>(Employee.values()), new Table<Job>(Job.values()));

	private String schema;

	public String getName() {
		return schema;
	}

	public List<ITable> getTables() {
		return tables;
	}

	public ITable getTableBy(Class<?> clazz) {
		String tableName = clazz.getSimpleName();
		for (ITable t : tables) {
			if (tableName.equals(t.getName()))
				return t;
		}
		return null;
	}

	public ITable getTableBy(String name) {
		for (ITable t : tables) {
			if (name.equals(t.getName()))
				return t;
		}
		return null;
	}

	public void setName(String name) {
		this.schema = name;
		for (ITable t : tables) {
			t.includeSchema(name);
		}
	}

}
