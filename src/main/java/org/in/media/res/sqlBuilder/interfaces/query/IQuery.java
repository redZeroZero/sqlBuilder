package org.in.media.res.sqlBuilder.interfaces.query;

import java.util.List;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.SortDirection;
import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.model.ITable;

public interface IQuery extends IResetable, ISelect, IFrom, IWhere, IGroupBy, IOrderBy, IHaving, ILimit {

	public List<IClause> clauses();

	IQuery select(IColumn column);

	IQuery select(IColumn... columns);

	IQuery select(ITable table);

	IQuery select(AggregateOperator agg, IColumn column);

	IQuery on(IColumn c1, IColumn c2);

	IQuery from(ITable table);

	IQuery from(ITable... tables);

	IQuery join(ITable t);

	IQuery innerJoin(ITable t);

	IQuery leftJoin(ITable t);

	IQuery rightJoin(ITable t);

	@Override
	IQuery groupBy(IColumn column);

	@Override
	IQuery groupBy(IColumn... columns);

	@Override
	IQuery orderBy(IColumn column);

	@Override
	IQuery orderBy(IColumn column, SortDirection direction);

	@Override
	IQuery asc(IColumn column);

	@Override
	IQuery desc(IColumn column);

	@Override
	IQuery having(ICondition condition);

	@Override
	IQuery and(ICondition condition);

	@Override
	IQuery or(ICondition condition);

	@Override
	IHavingBuilder having(IColumn column);

	@Override
	IQuery limit(int limit);

	@Override
	IQuery offset(int offset);

	@Override
	IQuery limitAndOffset(int limit, int offset);
}
