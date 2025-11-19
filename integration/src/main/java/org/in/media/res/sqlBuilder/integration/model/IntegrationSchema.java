package org.in.media.res.sqlBuilder.integration.model;

import org.in.media.res.sqlBuilder.api.model.ScannedSchema;
import org.in.media.res.sqlBuilder.api.model.Table;

public final class IntegrationSchema {

	private static final String BASE_PACKAGE = "org.in.media.res.sqlBuilder.integration.model";
	private static final ScannedSchema SCHEMA = new ScannedSchema(BASE_PACKAGE);

	private IntegrationSchema() {
	}

	public static ScannedSchema schema() {
		return SCHEMA;
	}

	public static Table employees() {
		return table(EmployeesTable.class);
	}

	public static Table departments() {
		return table(DepartmentsTable.class);
	}

	public static Table jobs() {
		return table(JobsTable.class);
	}

	public static Table customers() {
		return table(CustomersTable.class);
	}

	public static Table orders() {
		return table(OrdersTable.class);
	}

	public static Table orderLines() {
		return table(OrderLinesTable.class);
	}

	public static Table products() {
		return table(ProductsTable.class);
	}

	public static Table payments() {
		return table(PaymentsTable.class);
	}

	private static Table table(Class<?> descriptorClass) {
		return SCHEMA.getTableBy(descriptorClass);
	}
}
