package org.in.media.res.sqlBuilder.interfaces.query;

import java.util.List;

import org.in.media.res.sqlBuilder.constants.SortDirection;
import org.in.media.res.sqlBuilder.interfaces.model.IColumn;

public interface IOrderBy extends IClause, IResetable, ITranspilable {

	IOrderBy orderBy(IColumn column);

	IOrderBy orderBy(IColumn column, SortDirection direction);

	IOrderBy asc(IColumn column);

	IOrderBy desc(IColumn column);

	List<Ordering> orderings();

	record Ordering(IColumn column, SortDirection direction) {
	}
}
