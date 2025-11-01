package org.in.media.res.sqlBuilder.interfaces.query;

import java.util.List;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.SortDirection;
import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.model.ITable;
import org.in.media.res.sqlBuilder.interfaces.model.ITableDescriptor;

public interface IQuery extends IResetable, ISelect, IFrom, IWhere, IGroupBy, IOrderBy, IHaving, ILimit {

	public List<IClause> clauses();

	IQuery select(IColumn column);

	IQuery select(ITableDescriptor<?> descriptor);

	IQuery select(IColumn... columns);

	IQuery select(ITableDescriptor<?>... descriptors);

	IQuery select(ITable table);

	IQuery select(AggregateOperator agg, IColumn column);

	IQuery select(AggregateOperator agg, ITableDescriptor<?> descriptor);

	IQuery on(IColumn c1, IColumn c2);

	IQuery from(ITable table);

	IQuery from(ITable... tables);

	IQuery join(ITable t);

	IQuery innerJoin(ITable t);

	IQuery leftJoin(ITable t);

	IQuery rightJoin(ITable t);

	@Override
	IQuery groupBy(IColumn column);

	IQuery groupBy(ITableDescriptor<?> descriptor);

	@Override
	IQuery groupBy(IColumn... columns);

	IQuery groupBy(ITableDescriptor<?>... descriptors);

	@Override
	IQuery orderBy(IColumn column);

	IQuery orderBy(ITableDescriptor<?> descriptor);

	@Override
	IQuery orderBy(IColumn column, SortDirection direction);

	IQuery orderBy(ITableDescriptor<?> descriptor, SortDirection direction);

	@Override
	IQuery asc(IColumn column);

	IQuery asc(ITableDescriptor<?> descriptor);

	@Override
	IQuery desc(IColumn column);

	IQuery desc(ITableDescriptor<?> descriptor);

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
