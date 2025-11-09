package org.in.media.res.sqlBuilder.example;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import org.in.media.res.sqlBuilder.core.model.ColumnRef;
import org.in.media.res.sqlBuilder.core.model.TableFacets;

final class PaymentColumnsImpl implements PaymentColumns {

	private final TableFacets.Facet facet;

	private ColumnRef<Long> cached_ID;
	private ColumnRef<Long> cached_ORDER_ID;
	private ColumnRef<BigDecimal> cached_AMOUNT;
	private ColumnRef<LocalDate> cached_PAID_AT;
	private ColumnRef<String> cached_METHOD;

	PaymentColumnsImpl(TableFacets.Facet facet) {
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
	public ColumnRef<BigDecimal> AMOUNT() {
		if (cached_AMOUNT == null) {
			cached_AMOUNT = cast(facet.column("AMOUNT"));
		}
		return cached_AMOUNT;
	}

	@Override
	public ColumnRef<LocalDate> PAID_AT() {
		if (cached_PAID_AT == null) {
			cached_PAID_AT = cast(facet.column("PAID_AT"));
		}
		return cached_PAID_AT;
	}

	@Override
	public ColumnRef<String> METHOD() {
		if (cached_METHOD == null) {
			cached_METHOD = cast(facet.column("METHOD"));
		}
		return cached_METHOD;
	}

	@SuppressWarnings("unchecked")
	private static <T> ColumnRef<T> cast(ColumnRef<?> ref) {
		return (ColumnRef<T>) ref;
	}
}
