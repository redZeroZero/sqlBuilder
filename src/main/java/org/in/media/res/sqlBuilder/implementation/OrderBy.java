package org.in.media.res.sqlBuilder.implementation;

import java.util.ArrayList;
import java.util.List;

import org.in.media.res.sqlBuilder.constants.SortDirection;
import org.in.media.res.sqlBuilder.implementation.factories.OrderByTranspilerFactory;
import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.query.IOrderBy;
import org.in.media.res.sqlBuilder.interfaces.query.IOrderByTranspiler;

public class OrderBy implements IOrderBy {

	private final List<Ordering> orderings = new ArrayList<>();

	private final IOrderByTranspiler orderByTranspiler = OrderByTranspilerFactory.instanciateOrderByTranspiler();

	@Override
	public String transpile() {
		return orderings.isEmpty() ? "" : orderByTranspiler.transpile(this);
	}

	@Override
	public void reset() {
		orderings.clear();
	}

	@Override
	public IOrderBy orderBy(IColumn column) {
		return orderBy(column, SortDirection.ASC);
	}

	@Override
	public IOrderBy orderBy(IColumn column, SortDirection direction) {
		orderings.addLast(new Ordering(column, direction));
		return this;
	}

	@Override
	public IOrderBy asc(IColumn column) {
		return orderBy(column, SortDirection.ASC);
	}

	@Override
	public IOrderBy desc(IColumn column) {
		return orderBy(column, SortDirection.DESC);
	}

	@Override
	public List<Ordering> orderings() {
		return orderings;
	}

}
