package org.in.media.res.sqlBuilder.example;

import org.in.media.res.sqlBuilder.api.model.annotation.SqlColumn;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlTable;
import org.in.media.res.sqlBuilder.core.model.ColumnRef;

@SqlTable(name = "Employee", alias = "E")
public final class Employee {

	@SqlColumn(name = "ID")
	public static ColumnRef C_ID;

	@SqlColumn(name = "FIRST_NAME", alias = "firstName")
	public static ColumnRef C_FIRST_NAME;

	@SqlColumn(name = "LAST_NAME", alias = "lastName")
	public static ColumnRef C_LAST_NAME;

	@SqlColumn(name = "MAIL", alias = "email")
	public static ColumnRef C_MAIL;

	@SqlColumn(name = "PASSWORD", alias = "passwd")
	public static ColumnRef C_PASSWORD;

	private Employee() {
	}
}
