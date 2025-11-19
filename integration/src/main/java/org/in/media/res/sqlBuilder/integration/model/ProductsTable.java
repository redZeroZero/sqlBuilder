package org.in.media.res.sqlBuilder.integration.model;

import java.math.BigDecimal;

import org.in.media.res.sqlBuilder.api.model.ColumnRef;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlColumn;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlTable;

@SqlTable(name = "products", alias = "p")
public final class ProductsTable {

	@SqlColumn(name = "id", javaType = Long.class)
	public static ColumnRef<Long> C_ID;

	@SqlColumn(name = "sku", javaType = String.class)
	public static ColumnRef<String> C_SKU;

	@SqlColumn(name = "name", javaType = String.class)
	public static ColumnRef<String> C_NAME;

	@SqlColumn(name = "category", javaType = String.class)
	public static ColumnRef<String> C_CATEGORY;

	@SqlColumn(name = "description", javaType = String.class)
	public static ColumnRef<String> C_DESCRIPTION;

	@SqlColumn(name = "inventory_count", alias = "inventoryCount", javaType = Integer.class)
	public static ColumnRef<Integer> C_INVENTORY_COUNT;

	@SqlColumn(name = "price", javaType = BigDecimal.class)
	public static ColumnRef<BigDecimal> C_PRICE;

	private ProductsTable() {
	}
}
