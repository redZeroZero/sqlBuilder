package org.in.media.res.sqlBuilder.example;

import org.in.media.res.sqlBuilder.api.model.annotation.SqlColumn;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlTable;
import org.in.media.res.sqlBuilder.core.model.ColumnRef;

@SqlTable(name = "Job", alias = "J")
public final class Job {

	@SqlColumn(name = "ID")
	public static ColumnRef C_ID;

	@SqlColumn(name = "SALARY", alias = "pay")
	public static ColumnRef C_SALARY;

	@SqlColumn(name = "DESCRIPTION", alias = "Intitule")
	public static ColumnRef C_DESCRIPTION;

	@SqlColumn(name = "EMPLOYEE_ID", alias = "employeeId")
	public static ColumnRef C_EMPLOYEE_ID;

	private Job() {
	}
}
