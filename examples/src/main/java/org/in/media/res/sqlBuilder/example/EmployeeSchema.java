package org.in.media.res.sqlBuilder.example;

import org.in.media.res.sqlBuilder.core.model.ScannedSchema;

public class EmployeeSchema extends ScannedSchema {

	public EmployeeSchema() {
		super(EmployeeSchema.class.getPackageName());
	}

	public EmployeeSchema(String basePackage) {
		super(basePackage);
	}
}
