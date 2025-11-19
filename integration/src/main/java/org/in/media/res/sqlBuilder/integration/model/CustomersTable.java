package org.in.media.res.sqlBuilder.integration.model;

import java.time.LocalDateTime;

import org.in.media.res.sqlBuilder.api.model.ColumnRef;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlColumn;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlTable;

@SqlTable(name = "customers", alias = "c")
public final class CustomersTable {

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

	@SqlColumn(name = "loyalty_level", alias = "loyaltyLevel", javaType = String.class)
	public static ColumnRef<String> C_LOYALTY_LEVEL;

	@SqlColumn(name = "city", javaType = String.class)
	public static ColumnRef<String> C_CITY;

	@SqlColumn(name = "country", javaType = String.class)
	public static ColumnRef<String> C_COUNTRY;

	@SqlColumn(name = "created_at", alias = "createdAt", javaType = LocalDateTime.class)
	public static ColumnRef<LocalDateTime> C_CREATED_AT;

	private CustomersTable() {
	}
}
