package org.in.media.res.sqlBuilder.example;

import org.in.media.res.sqlBuilder.api.model.annotation.SqlColumn;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlTable;

@SqlTable(name = "Product", alias = "P")
public final class Product {

	@SqlColumn(name = "ID", alias = "id", javaType = Long.class)
	public static Long ID;

	@SqlColumn(name = "SKU", alias = "sku", javaType = String.class)
	public static String SKU;

	@SqlColumn(name = "NAME", alias = "name", javaType = String.class)
	public static String NAME;

	@SqlColumn(name = "CATEGORY", alias = "category", javaType = String.class)
	public static String CATEGORY;

	@SqlColumn(name = "UNIT_PRICE", alias = "unitPrice", javaType = java.math.BigDecimal.class)
	public static java.math.BigDecimal UNIT_PRICE;

	private Product() {
	}
}
