package org.in.media.res.sqlBuilder.interfaces.query;

import org.in.media.res.sqlBuilder.interfaces.model.IColumn;

public interface IConnector {

	public IComparator and(IColumn column);

	public IComparator or(IColumn column);

	public IAggregator and();

	public IAggregator or();
}
