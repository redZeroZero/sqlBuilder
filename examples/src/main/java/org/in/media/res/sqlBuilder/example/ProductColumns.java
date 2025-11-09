package org.in.media.res.sqlBuilder.example;

import java.math.BigDecimal;

import org.in.media.res.sqlBuilder.core.model.ColumnRef;
import org.in.media.res.sqlBuilder.core.model.TableFacets;

public interface ProductColumns {

	ColumnRef<Long> ID();

	ColumnRef<String> SKU();

	ColumnRef<String> NAME();

	ColumnRef<String> CATEGORY();

	ColumnRef<BigDecimal> UNIT_PRICE();

	static ProductColumns of(TableFacets.Facet facet) {
		return new ProductColumnsImpl(facet);
	}
}
