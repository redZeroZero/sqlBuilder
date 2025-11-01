package org.in.media.res.sqlBuilder.implementation;

import java.util.ArrayList;
import java.util.List;

import org.in.media.res.sqlBuilder.implementation.factories.GroupByTranspilerFactory;
import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.query.IGroupBy;
import org.in.media.res.sqlBuilder.interfaces.query.IGroupByTranspiler;

public class GroupBy implements IGroupBy {

	private final List<IColumn> columns = new ArrayList<>();

	private final IGroupByTranspiler groupByTranspiler = GroupByTranspilerFactory.instanciateGroupByTranspiler();

	@Override
	public String transpile() {
		return columns.isEmpty() ? "" : groupByTranspiler.transpile(this);
	}

	@Override
	public void reset() {
		columns.clear();
	}

	@Override
	public IGroupBy groupBy(IColumn column) {
		columns.addLast(column);
		return this;
	}

	@Override
	public IGroupBy groupBy(IColumn... columns) {
		for (IColumn column : columns) {
			groupBy(column);
		}
		return this;
	}

	@Override
	public List<IColumn> groupByColumns() {
		return columns;
	}

}
