package org.in.media.res.sqlBuilder.example;

import java.math.BigDecimal;
import java.util.Objects;

import org.in.media.res.sqlBuilder.core.model.ColumnRef;
import org.in.media.res.sqlBuilder.core.model.TableFacets;

final class OrderLineColumnsImpl implements OrderLineColumns {

	private final TableFacets.Facet facet;

	private ColumnRef<Long> cached_ID;
	private ColumnRef<Long> cached_ORDER_ID;
	private ColumnRef<Long> cached_PRODUCT_ID;
	private ColumnRef<Integer> cached_QUANTITY;
	private ColumnRef<BigDecimal> cached_LINE_TOTAL;

	OrderLineColumnsImpl(TableFacets.Facet facet) {
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
	public ColumnRef<Long> ORDER_ID() {
		if (cached_ORDER_ID == null) {
			cached_ORDER_ID = cast(facet.column("ORDER_ID"));
		}
		return cached_ORDER_ID;
	}

	@Override
	public ColumnRef<Long> PRODUCT_ID() {
		if (cached_PRODUCT_ID == null) {
			cached_PRODUCT_ID = cast(facet.column("PRODUCT_ID"));
		}
		return cached_PRODUCT_ID;
	}

	@Override
	public ColumnRef<Integer> QUANTITY() {
		if (cached_QUANTITY == null) {
			cached_QUANTITY = cast(facet.column("QUANTITY"));
		}
		return cached_QUANTITY;
	}

	@Override
	public ColumnRef<BigDecimal> LINE_TOTAL() {
		if (cached_LINE_TOTAL == null) {
			cached_LINE_TOTAL = cast(facet.column("LINE_TOTAL"));
		}
		return cached_LINE_TOTAL;
	}

	@SuppressWarnings("unchecked")
	private static <T> ColumnRef<T> cast(ColumnRef<?> ref) {
		return (ColumnRef<T>) ref;
	}
}
