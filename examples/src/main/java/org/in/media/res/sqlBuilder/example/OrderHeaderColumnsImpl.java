package org.in.media.res.sqlBuilder.example;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import org.in.media.res.sqlBuilder.api.model.ColumnRef;
import org.in.media.res.sqlBuilder.api.model.TableFacets;

final class OrderHeaderColumnsImpl implements OrderHeaderColumns {

	private final TableFacets.Facet facet;

	private ColumnRef<Long> cached_ID;
	private ColumnRef<Long> cached_CUSTOMER_ID;
	private ColumnRef<LocalDate> cached_ORDER_DATE;
	private ColumnRef<BigDecimal> cached_TOTAL;
	private ColumnRef<String> cached_STATUS;

	OrderHeaderColumnsImpl(TableFacets.Facet facet) {
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
	public ColumnRef<Long> CUSTOMER_ID() {
		if (cached_CUSTOMER_ID == null) {
			cached_CUSTOMER_ID = cast(facet.column("CUSTOMER_ID"));
		}
		return cached_CUSTOMER_ID;
	}

	@Override
	public ColumnRef<LocalDate> ORDER_DATE() {
		if (cached_ORDER_DATE == null) {
			cached_ORDER_DATE = cast(facet.column("ORDER_DATE"));
		}
		return cached_ORDER_DATE;
	}

	@Override
	public ColumnRef<BigDecimal> TOTAL() {
		if (cached_TOTAL == null) {
			cached_TOTAL = cast(facet.column("TOTAL"));
		}
		return cached_TOTAL;
	}

	@Override
	public ColumnRef<String> STATUS() {
		if (cached_STATUS == null) {
			cached_STATUS = cast(facet.column("STATUS"));
		}
		return cached_STATUS;
	}

	@SuppressWarnings("unchecked")
	private static <T> ColumnRef<T> cast(ColumnRef<?> ref) {
		return (ColumnRef<T>) ref;
	}
}
