package org.in.media.res.sqlBuilder.interfaces.query;

import org.in.media.res.sqlBuilder.interfaces.model.IColumn;

public interface IAggregator {

	public IComparator min(IColumn column);

	public IComparator max(IColumn column);

	public IComparator sum(IColumn column);

	public IComparator avg(IColumn column);

	public IComparator col(IColumn column);

}
