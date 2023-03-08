package org.in.media.res.sqlBuilder.interfaces.query;

import org.in.media.res.sqlBuilder.interfaces.model.IColumn;

public interface IJoinable {

	IFrom on(IColumn c1, IColumn c2);

}
