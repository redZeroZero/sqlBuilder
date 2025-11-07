package org.in.media.res.sqlBuilder.example;

import java.time.LocalDate;

import org.in.media.res.sqlBuilder.api.model.annotation.SqlColumn;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlTable;

@SqlTable(name = "Payment", alias = "PAY")
public final class Payment {

	@SqlColumn(name = "ID", alias = "id", javaType = Long.class)
	public static Long ID;

	@SqlColumn(name = "ORDER_ID", alias = "orderId", javaType = Long.class)
	public static Long ORDER_ID;

	@SqlColumn(name = "AMOUNT", alias = "amount", javaType = java.math.BigDecimal.class)
	public static java.math.BigDecimal AMOUNT;

	@SqlColumn(name = "PAID_AT", alias = "paidAt", javaType = LocalDate.class)
	public static LocalDate PAID_AT;

	@SqlColumn(name = "METHOD", alias = "method", javaType = String.class)
	public static String METHOD;

	private Payment() {
	}
}
