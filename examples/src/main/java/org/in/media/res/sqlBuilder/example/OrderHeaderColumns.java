package org.in.media.res.sqlBuilder.example;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.in.media.res.sqlBuilder.api.model.ColumnRef;
import org.in.media.res.sqlBuilder.api.model.TableFacets;

public interface OrderHeaderColumns {

	ColumnRef<Long> ID();

	ColumnRef<Long> CUSTOMER_ID();

	ColumnRef<LocalDate> ORDER_DATE();

	ColumnRef<BigDecimal> TOTAL();

	ColumnRef<String> STATUS();

	static OrderHeaderColumns of(TableFacets.Facet facet) {
		return new OrderHeaderColumnsImpl(facet);
	}
}
