package org.in.media.res.sqlBuilder.example;

import org.in.media.res.sqlBuilder.interfaces.model.ITableDescriptor;

public enum Employee implements ITableDescriptor<Employee> {

	T_ALIAS("E"), 
	C_ID(null), 
	C_FIRST_NAME("firstName"), 
	C_LAST_NAME("lastName"), 
	C_MAIL("email"), 
	C_PASSWORD("passwd");

	private String alias;

	private Employee(String alias) {
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

}