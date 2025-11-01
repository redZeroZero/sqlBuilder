package org.in.media.res.sqlBuilder.interfaces.query;

import java.util.List;

import org.in.media.res.sqlBuilder.interfaces.model.IColumn;

public interface IHaving extends IClause, IResetable, ITranspilable {

	IHaving having(ICondition condition);

	IHavingBuilder having(IColumn column);

	IHaving and(ICondition condition);

	IHaving or(ICondition condition);

	List<ICondition> havingConditions();
}
