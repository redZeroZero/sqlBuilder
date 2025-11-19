package org.in.media.res.sqlBuilder.integration.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.in.media.res.sqlBuilder.api.model.ColumnRef;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlColumn;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlTable;

@SqlTable(name = "jobs", alias = "j")
public final class JobsTable {

	@SqlColumn(name = "id", javaType = Long.class)
	public static ColumnRef<Long> C_ID;

	@SqlColumn(name = "title", javaType = String.class)
	public static ColumnRef<String> C_TITLE;

	@SqlColumn(name = "salary", javaType = BigDecimal.class)
	public static ColumnRef<BigDecimal> C_SALARY;

	@SqlColumn(name = "employee_id", alias = "employeeId", javaType = Long.class)
	public static ColumnRef<Long> C_EMPLOYEE_ID;

	@SqlColumn(name = "start_date", alias = "startDate", javaType = LocalDate.class)
	public static ColumnRef<LocalDate> C_START_DATE;

	@SqlColumn(name = "end_date", alias = "endDate", javaType = LocalDate.class)
	public static ColumnRef<LocalDate> C_END_DATE;

	@SqlColumn(name = "job_type", alias = "jobType", javaType = String.class)
	public static ColumnRef<String> C_JOB_TYPE;

	private JobsTable() {
	}
}
