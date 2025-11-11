package org.in.media.res.sqlBuilder.example;

import org.in.media.res.sqlBuilder.api.model.annotation.SqlColumn;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlTable;
import org.in.media.res.sqlBuilder.api.model.ColumnRef;

@SqlTable(name = "Job", alias = "J")
public final class Job {

	@SqlColumn(name = "ID", javaType = Integer.class)
	public static ColumnRef<Integer> C_ID;

	@SqlColumn(name = "SALARY", alias = "pay", javaType = Integer.class)
	public static ColumnRef<Integer> C_SALARY;

	@SqlColumn(name = "DESCRIPTION", alias = "Intitule", javaType = String.class)
	public static ColumnRef<String> C_DESCRIPTION;

	@SqlColumn(name = "EMPLOYEE_ID", alias = "employeeId", javaType = Integer.class)
	public static ColumnRef<Integer> C_EMPLOYEE_ID;

	private Job() {
	}
}
