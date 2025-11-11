package org.in.media.res.sqlBuilder.example;

import org.in.media.res.sqlBuilder.api.model.annotation.SqlColumn;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlTable;
import org.in.media.res.sqlBuilder.api.model.ColumnRef;

@SqlTable(name = "Employee", alias = "E")
public final class Employee {

	@SqlColumn(name = "ID", javaType = Integer.class)
	public static ColumnRef<Integer> C_ID;

	@SqlColumn(name = "FIRST_NAME", alias = "firstName", javaType = String.class)
	public static ColumnRef<String> C_FIRST_NAME;

	@SqlColumn(name = "LAST_NAME", alias = "lastName", javaType = String.class)
	public static ColumnRef<String> C_LAST_NAME;

	@SqlColumn(name = "MAIL", alias = "email", javaType = String.class)
	public static ColumnRef<String> C_MAIL;

	@SqlColumn(name = "PASSWORD", alias = "passwd", javaType = String.class)
	public static ColumnRef<String> C_PASSWORD;

	private Employee() {
	}
}
