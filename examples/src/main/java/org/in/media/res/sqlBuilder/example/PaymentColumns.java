package org.in.media.res.sqlBuilder.example;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.in.media.res.sqlBuilder.api.model.ColumnRef;
import org.in.media.res.sqlBuilder.api.model.TableFacets;

public interface PaymentColumns {

	ColumnRef<Long> ID();

	ColumnRef<Long> ORDER_ID();

	ColumnRef<BigDecimal> AMOUNT();

	ColumnRef<LocalDate> PAID_AT();

	ColumnRef<String> METHOD();

	static PaymentColumns of(TableFacets.Facet facet) {
		return new PaymentColumnsImpl(facet);
	}
}
