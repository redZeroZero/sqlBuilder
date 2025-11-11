package org.in.media.res.sqlBuilder.example;

import java.math.BigDecimal;

import org.in.media.res.sqlBuilder.api.model.ColumnRef;
import org.in.media.res.sqlBuilder.api.model.TableFacets;

public interface OrderLineColumns {

	ColumnRef<Long> ID();

	ColumnRef<Long> ORDER_ID();

	ColumnRef<Long> PRODUCT_ID();

	ColumnRef<Integer> QUANTITY();

	ColumnRef<BigDecimal> LINE_TOTAL();

	static OrderLineColumns of(TableFacets.Facet facet) {
		return new OrderLineColumnsImpl(facet);
	}
}
