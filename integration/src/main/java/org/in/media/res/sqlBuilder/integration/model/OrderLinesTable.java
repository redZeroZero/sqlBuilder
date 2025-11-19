package org.in.media.res.sqlBuilder.integration.model;

import java.math.BigDecimal;

import org.in.media.res.sqlBuilder.api.model.ColumnRef;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlColumn;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlTable;

@SqlTable(name = "order_lines", alias = "ol")
public final class OrderLinesTable {

	@SqlColumn(name = "id", javaType = Long.class)
	public static ColumnRef<Long> C_ID;

	@SqlColumn(name = "order_id", alias = "orderId", javaType = Long.class)
	public static ColumnRef<Long> C_ORDER_ID;

	@SqlColumn(name = "product_id", alias = "productId", javaType = Long.class)
	public static ColumnRef<Long> C_PRODUCT_ID;

	@SqlColumn(name = "quantity", javaType = Integer.class)
	public static ColumnRef<Integer> C_QUANTITY;

	@SqlColumn(name = "unit_price", alias = "unitPrice", javaType = BigDecimal.class)
	public static ColumnRef<BigDecimal> C_UNIT_PRICE;

	@SqlColumn(name = "discount", javaType = BigDecimal.class)
	public static ColumnRef<BigDecimal> C_DISCOUNT;

	@SqlColumn(name = "tax_amount", alias = "taxAmount", javaType = BigDecimal.class)
	public static ColumnRef<BigDecimal> C_TAX_AMOUNT;

	private OrderLinesTable() {
	}
}
