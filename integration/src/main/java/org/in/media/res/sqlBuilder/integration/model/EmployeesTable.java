package org.in.media.res.sqlBuilder.integration.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.in.media.res.sqlBuilder.api.model.ColumnRef;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlColumn;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlTable;

@SqlTable(name = "employees", alias = "e")
public final class EmployeesTable {

	@SqlColumn(name = "id", javaType = Long.class)
	public static ColumnRef<Long> C_ID;

	@SqlColumn(name = "first_name", alias = "firstName", javaType = String.class)
	public static ColumnRef<String> C_FIRST_NAME;

	@SqlColumn(name = "last_name", alias = "lastName", javaType = String.class)
	public static ColumnRef<String> C_LAST_NAME;

	@SqlColumn(name = "email", javaType = String.class)
	public static ColumnRef<String> C_EMAIL;

	@SqlColumn(name = "phone", javaType = String.class)
	public static ColumnRef<String> C_PHONE;

	@SqlColumn(name = "status", javaType = String.class)
	public static ColumnRef<String> C_STATUS;

	@SqlColumn(name = "department_id", alias = "departmentId", javaType = Long.class)
	public static ColumnRef<Long> C_DEPARTMENT_ID;

	@SqlColumn(name = "manager_id", alias = "managerId", javaType = Long.class)
	public static ColumnRef<Long> C_MANAGER_ID;

	@SqlColumn(name = "hire_date", alias = "hireDate", javaType = LocalDate.class)
	public static ColumnRef<LocalDate> C_HIRE_DATE;

	@SqlColumn(name = "salary", javaType = BigDecimal.class)
	public static ColumnRef<BigDecimal> C_SALARY;

	@SqlColumn(name = "bonus", javaType = BigDecimal.class)
	public static ColumnRef<BigDecimal> C_BONUS;

	private EmployeesTable() {
	}
}
