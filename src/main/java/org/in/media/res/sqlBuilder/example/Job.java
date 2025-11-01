package org.in.media.res.sqlBuilder.example;

import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.model.ITableDescriptor;

public enum Job implements ITableDescriptor<Job> {
	T_ALIAS("J"), 
	C_ID(), C_SALARY("pay"), 
	C_DESCRIPTION("Intitule"), 
	C_EMPLOYEE_ID("employeeId");

	private String alias;
	private IColumn column;

	private Job() {
		this.alias = null;
	}

	private Job(String alias) {
		this.alias = alias;
	}

	public String value() {
		return this.name().substring(2);
	}

	public String alias() {
		return alias;
	}

	public String fieldName() {
		return this.name();
	}

	@Override
	public void bindColumn(IColumn column) {
		this.column = column;
	}

	@Override
	public IColumn column() {
		if (column == null) {
			throw new IllegalStateException("Column for descriptor " + name() + " is not bound to a table");
		}
		return column;
	}
}
