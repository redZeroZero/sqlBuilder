package org.in.media.res.sqlBuilder.integration.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.in.media.res.sqlBuilder.api.model.ColumnRef;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlColumn;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlTable;

@SqlTable(name = "orders", alias = "o")
public final class OrdersTable {

	@SqlColumn(name = "id", javaType = Long.class)
	public static ColumnRef<Long> C_ID;

	@SqlColumn(name = "customer_id", alias = "customerId", javaType = Long.class)
	public static ColumnRef<Long> C_CUSTOMER_ID;

	@SqlColumn(name = "order_date", alias = "orderDate", javaType = LocalDateTime.class)
	public static ColumnRef<LocalDateTime> C_ORDER_DATE;

	@SqlColumn(name = "status", javaType = String.class)
	public static ColumnRef<String> C_STATUS;

	@SqlColumn(name = "shipping_method", alias = "shippingMethod", javaType = String.class)
	public static ColumnRef<String> C_SHIPPING_METHOD;

	@SqlColumn(name = "taxes", javaType = BigDecimal.class)
	public static ColumnRef<BigDecimal> C_TAXES;

	@SqlColumn(name = "total", javaType = BigDecimal.class)
	public static ColumnRef<BigDecimal> C_TOTAL;

	private OrdersTable() {
	}
}
