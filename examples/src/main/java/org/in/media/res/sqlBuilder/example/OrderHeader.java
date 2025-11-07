package org.in.media.res.sqlBuilder.example;

import java.time.LocalDate;

import org.in.media.res.sqlBuilder.api.model.annotation.SqlColumn;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlTable;

@SqlTable(name = "Orders", alias = "O")
public final class OrderHeader {

	@SqlColumn(name = "ID", alias = "id", javaType = Long.class)
	public static Long ID;

	@SqlColumn(name = "CUSTOMER_ID", alias = "customerId", javaType = Long.class)
	public static Long CUSTOMER_ID;

	@SqlColumn(name = "ORDER_DATE", alias = "orderDate", javaType = LocalDate.class)
	public static LocalDate ORDER_DATE;

	@SqlColumn(name = "TOTAL", alias = "total", javaType = java.math.BigDecimal.class)
	public static java.math.BigDecimal TOTAL;

	@SqlColumn(name = "STATUS", alias = "status", javaType = String.class)
	public static String STATUS;

	private OrderHeader() {
	}
}
