package org.in.media.res.sqlBuilder.core.query;

import java.util.ArrayList;
import java.util.List;

import org.in.media.res.sqlBuilder.constants.SortDirection;
import org.in.media.res.sqlBuilder.core.query.factory.OrderByTranspilerFactory;
import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.query.OrderBy;
import org.in.media.res.sqlBuilder.api.query.OrderByTranspiler;

public class OrderByImpl implements OrderBy {

	private final List<Ordering> orderings = new ArrayList<>();

	private final OrderByTranspiler orderByTranspiler = OrderByTranspilerFactory.instanciateOrderByTranspiler();

	@Override
	public String transpile() {
		return orderings.isEmpty() ? "" : orderByTranspiler.transpile(this);
	}

	@Override
	public void reset() {
		orderings.clear();
	}

	@Override
	public OrderBy orderBy(Column column) {
		return orderBy(column, SortDirection.ASC);
	}

	@Override
	public OrderBy orderBy(Column column, SortDirection direction) {
		orderings.addLast(new Ordering(column, direction));
		return this;
	}

	@Override
	public OrderBy asc(Column column) {
		return orderBy(column, SortDirection.ASC);
	}

	@Override
	public OrderBy desc(Column column) {
		return orderBy(column, SortDirection.DESC);
	}

	@Override
	public List<Ordering> orderings() {
		return orderings;
	}

}
