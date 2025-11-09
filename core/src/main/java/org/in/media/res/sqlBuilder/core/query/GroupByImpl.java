package org.in.media.res.sqlBuilder.core.query;

import java.util.ArrayList;
import java.util.List;

import org.in.media.res.sqlBuilder.core.query.factory.TranspilerFactory;
import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.query.GroupBy;
import org.in.media.res.sqlBuilder.api.query.GroupByTranspiler;

final class GroupByImpl implements GroupBy {

	private final List<Column> columns = new ArrayList<>();

	private final GroupByTranspiler groupByTranspiler = TranspilerFactory.instanciateGroupByTranspiler();

	@Override
	public String transpile() {
		return columns.isEmpty() ? "" : groupByTranspiler.transpile(this);
	}

	@Override
	public void reset() {
		columns.clear();
	}

	@Override
	public GroupBy groupBy(Column column) {
		columns.addLast(column);
		return this;
	}

	@Override
	public GroupBy groupBy(Column... columns) {
		for (Column column : columns) {
			groupBy(column);
		}
		return this;
	}

	@Override
	public List<Column> groupByColumns() {
		return columns;
	}

}
