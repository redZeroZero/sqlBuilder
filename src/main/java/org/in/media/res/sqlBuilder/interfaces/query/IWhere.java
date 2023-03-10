package org.in.media.res.sqlBuilder.interfaces.query;

import java.util.List;

public interface IWhere extends IComparator, IConnector, IAggregator, IClause, ITranspilable {

	List<ICondition> conditions();

}
