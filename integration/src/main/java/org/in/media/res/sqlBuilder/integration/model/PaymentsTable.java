package org.in.media.res.sqlBuilder.integration.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.in.media.res.sqlBuilder.api.model.ColumnRef;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlColumn;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlTable;

@SqlTable(name = "payments", alias = "pay")
public final class PaymentsTable {

	@SqlColumn(name = "id", javaType = Long.class)
	public static ColumnRef<Long> C_ID;

	@SqlColumn(name = "order_id", alias = "orderId", javaType = Long.class)
	public static ColumnRef<Long> C_ORDER_ID;

	@SqlColumn(name = "amount", javaType = BigDecimal.class)
	public static ColumnRef<BigDecimal> C_AMOUNT;

	@SqlColumn(name = "paid_at", alias = "paidAt", javaType = LocalDateTime.class)
	public static ColumnRef<LocalDateTime> C_PAID_AT;

	@SqlColumn(name = "method", javaType = String.class)
	public static ColumnRef<String> C_METHOD;

	@SqlColumn(name = "reference", javaType = String.class)
	public static ColumnRef<String> C_REFERENCE;

	private PaymentsTable() {
	}
}
