package org.in.media.res.sqlBuilder.example;

import java.math.BigDecimal;
import java.util.Objects;

import org.in.media.res.sqlBuilder.api.model.ColumnRef;
import org.in.media.res.sqlBuilder.api.model.TableFacets;

final class ProductColumnsImpl implements ProductColumns {

	private final TableFacets.Facet facet;

	private ColumnRef<Long> cached_ID;
	private ColumnRef<String> cached_SKU;
	private ColumnRef<String> cached_NAME;
	private ColumnRef<String> cached_CATEGORY;
	private ColumnRef<BigDecimal> cached_UNIT_PRICE;

	ProductColumnsImpl(TableFacets.Facet facet) {
		this.facet = Objects.requireNonNull(facet, "facet");
	}

	@Override
	public ColumnRef<Long> ID() {
		if (cached_ID == null) {
			cached_ID = cast(facet.column("ID"));
		}
		return cached_ID;
	}

	@Override
	public ColumnRef<String> SKU() {
		if (cached_SKU == null) {
			cached_SKU = cast(facet.column("SKU"));
		}
		return cached_SKU;
	}

	@Override
	public ColumnRef<String> NAME() {
		if (cached_NAME == null) {
			cached_NAME = cast(facet.column("NAME"));
		}
		return cached_NAME;
	}

	@Override
	public ColumnRef<String> CATEGORY() {
		if (cached_CATEGORY == null) {
			cached_CATEGORY = cast(facet.column("CATEGORY"));
		}
		return cached_CATEGORY;
	}

	@Override
	public ColumnRef<BigDecimal> UNIT_PRICE() {
		if (cached_UNIT_PRICE == null) {
			cached_UNIT_PRICE = cast(facet.column("UNIT_PRICE"));
		}
		return cached_UNIT_PRICE;
	}

	@SuppressWarnings("unchecked")
	private static <T> ColumnRef<T> cast(ColumnRef<?> ref) {
		return (ColumnRef<T>) ref;
	}
}
