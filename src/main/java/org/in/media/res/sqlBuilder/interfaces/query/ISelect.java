package org.in.media.res.sqlBuilder.interfaces.query;

import java.util.List;
import java.util.Map;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.model.ITable;

public interface ISelect extends IClause, IResetable, ITranspilable {

	ISelect select(IColumn column);

	ISelect select(IColumn... columns);

	ISelect select(ITable table);

	ISelect select(AggregateOperator agg, IColumn column);

	List<IColumn> columns();

	Map<IColumn, AggregateOperator> aggColumns();
	
}
