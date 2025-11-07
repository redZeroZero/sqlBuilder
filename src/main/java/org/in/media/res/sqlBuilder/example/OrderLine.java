package org.in.media.res.sqlBuilder.example;

import org.in.media.res.sqlBuilder.api.model.annotation.SqlColumn;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlTable;

@SqlTable(name = "OrderLine", alias = "OL")
public final class OrderLine {

	@SqlColumn(name = "ID", alias = "id", javaType = Long.class)
	public static Long ID;

	@SqlColumn(name = "ORDER_ID", alias = "orderId", javaType = Long.class)
	public static Long ORDER_ID;

	@SqlColumn(name = "PRODUCT_ID", alias = "productId", javaType = Long.class)
	public static Long PRODUCT_ID;

	@SqlColumn(name = "QUANTITY", alias = "qty", javaType = Integer.class)
	public static Integer QUANTITY;

	@SqlColumn(name = "LINE_TOTAL", alias = "lineTotal", javaType = java.math.BigDecimal.class)
	public static java.math.BigDecimal LINE_TOTAL;

	private OrderLine() {
	}
}
