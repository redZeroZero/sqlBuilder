package org.in.media.res.sqlBuilder.interfaces.query;

import java.util.List;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.model.ITable;

public interface IQuery extends IResetable, ISelect, IFrom, IWhere {

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
}
