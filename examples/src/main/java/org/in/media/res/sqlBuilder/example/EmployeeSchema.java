package org.in.media.res.sqlBuilder.example;

import org.in.media.res.sqlBuilder.api.model.ScannedSchema;
import org.in.media.res.sqlBuilder.api.query.Dialect;

public class EmployeeSchema extends ScannedSchema {

	public EmployeeSchema() {
		super(EmployeeSchema.class.getPackageName());
	}

	public EmployeeSchema(String basePackage) {
		super(basePackage);
	}

	public EmployeeSchema(Dialect dialect) {
		this();
		setDialect(dialect);
	}
}
