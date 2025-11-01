package org.in.media.res.sqlBuilder.interfaces.query;

import java.util.List;

import org.in.media.res.sqlBuilder.interfaces.model.IColumn;

public interface IGroupBy extends IClause, IResetable, ITranspilable {

	IGroupBy groupBy(IColumn column);

	IGroupBy groupBy(IColumn... columns);

	List<IColumn> groupByColumns();

}
