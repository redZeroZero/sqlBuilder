package org.in.media.res.sqlBuilder.core.query;

import java.util.ArrayList;
import java.util.List;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.query.RawSql;
import org.in.media.res.sqlBuilder.api.query.RawSqlFragment;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.api.query.spi.GroupBy;
import org.in.media.res.sqlBuilder.api.query.spi.GroupByTranspiler;
import org.in.media.res.sqlBuilder.core.query.transpiler.defaults.DefaultGroupByTranspiler;

final class GroupByImpl implements GroupBy, GroupByRawSupport {

	private final List<Column> columns = new ArrayList<>();
	private final List<RawSqlFragment> rawFragments = new ArrayList<>();

	private final GroupByTranspiler groupByTranspiler = new DefaultGroupByTranspiler();

	@Override
	public String transpile() {
		if (columns.isEmpty() && rawFragments.isEmpty()) {
			return "";
		}
		return groupByTranspiler.transpile(this);
	}

	@Override
	public void reset() {
		columns.clear();
		rawFragments.clear();
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
	public GroupBy groupByRaw(RawSqlFragment fragment) {
		rawFragments.add(fragment);
		return this;
	}

	@Override
	public GroupBy groupByRaw(String sql, SqlParameter<?>... params) {
		return groupByRaw(RawSql.of(sql, params));
	}

	@Override
	public List<Column> groupByColumns() {
		return columns;
	}

	@Override
	public List<RawSqlFragment> groupByFragments() {
		return List.copyOf(rawFragments);
	}

}
