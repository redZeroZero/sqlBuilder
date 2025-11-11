package org.in.media.res.sqlBuilder.core.query;

import java.util.ArrayList;
import java.util.List;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.query.RawSql;
import org.in.media.res.sqlBuilder.api.query.RawSqlFragment;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.api.query.spi.OrderBy;
import org.in.media.res.sqlBuilder.api.query.spi.OrderByTranspiler;
import org.in.media.res.sqlBuilder.constants.SortDirection;
import org.in.media.res.sqlBuilder.core.query.transpiler.defaults.DefaultOrderByTranspiler;

final class OrderByImpl implements OrderBy, OrderByRawSupport {

	private final List<Ordering> orderings = new ArrayList<>();
	private final List<RawSqlFragment> rawFragments = new ArrayList<>();

	private final OrderByTranspiler orderByTranspiler = new DefaultOrderByTranspiler();

	@Override
	public String transpile() {
		return orderings.isEmpty() ? "" : orderByTranspiler.transpile(this);
	}

	@Override
	public void reset() {
		orderings.clear();
		rawFragments.clear();
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
	public OrderBy orderByRaw(RawSqlFragment fragment) {
		rawFragments.add(fragment);
		return this;
	}

	@Override
	public OrderBy orderByRaw(String sql, SqlParameter<?>... params) {
		return orderByRaw(RawSql.of(sql, params));
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

	@Override
	public List<RawSqlFragment> orderByFragments() {
		return List.copyOf(rawFragments);
	}

}
