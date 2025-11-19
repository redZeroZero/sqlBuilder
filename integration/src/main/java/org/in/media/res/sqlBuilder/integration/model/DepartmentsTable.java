package org.in.media.res.sqlBuilder.integration.model;

import java.math.BigDecimal;

import org.in.media.res.sqlBuilder.api.model.ColumnRef;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlColumn;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlTable;

@SqlTable(name = "departments", alias = "d")
public final class DepartmentsTable {

	@SqlColumn(name = "id", javaType = Long.class)
	public static ColumnRef<Long> C_ID;

	@SqlColumn(name = "name", javaType = String.class)
	public static ColumnRef<String> C_NAME;

	@SqlColumn(name = "location", javaType = String.class)
	public static ColumnRef<String> C_LOCATION;

	@SqlColumn(name = "budget", javaType = BigDecimal.class)
	public static ColumnRef<BigDecimal> C_BUDGET;

	private DepartmentsTable() {
	}
}
