package org.in.media.res.sqlBuilder.interfaces.query;

import org.in.media.res.sqlBuilder.interfaces.model.ITable;

public interface IFrom extends IJoinable, IClause, IResetable, ITranspilable {

	IFrom from(ITable table);

	IFrom from(ITable... tables);

	IJoinable join(ITable t);

	IJoinable innerJoin(ITable t);

	IJoinable leftJoin(ITable t);

	IJoinable rightJoin(ITable t);

}
