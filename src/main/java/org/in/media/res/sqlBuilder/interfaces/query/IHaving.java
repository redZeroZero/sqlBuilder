package org.in.media.res.sqlBuilder.interfaces.query;

import java.util.List;

import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.model.ITableDescriptor;

public interface IHaving extends IClause, IResetable, ITranspilable {

	IHaving having(ICondition condition);

	IHavingBuilder having(IColumn column);

	default IHavingBuilder having(ITableDescriptor<?> descriptor) {
		return having(descriptor.column());
	}

	IHaving and(ICondition condition);

	IHaving or(ICondition condition);

	List<ICondition> havingConditions();
}
