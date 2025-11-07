package org.in.media.res.sqlBuilder.example;

import java.time.LocalDate;

import org.in.media.res.sqlBuilder.api.model.annotation.SqlColumn;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlTable;

@SqlTable(name = "Customer", alias = "C")
public final class Customer {

	@SqlColumn(name = "ID", alias = "id", javaType = Long.class)
	public static Long ID;

	@SqlColumn(name = "FIRST_NAME", alias = "firstName", javaType = String.class)
	public static String FIRST_NAME;

	@SqlColumn(name = "LAST_NAME", alias = "lastName", javaType = String.class)
	public static String LAST_NAME;

	@SqlColumn(name = "EMAIL", alias = "email", javaType = String.class)
	public static String EMAIL;

	@SqlColumn(name = "ACTIVE", alias = "active", javaType = Boolean.class)
	public static Boolean ACTIVE;

	@SqlColumn(name = "CREATED_AT", alias = "createdAt", javaType = LocalDate.class)
	public static LocalDate CREATED_AT;

	private Customer() {
	}
}
