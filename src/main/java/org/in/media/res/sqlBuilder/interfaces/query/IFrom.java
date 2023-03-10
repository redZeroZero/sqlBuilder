package org.in.media.res.sqlBuilder.interfaces.query;

import java.util.Map;

import org.in.media.res.sqlBuilder.implementation.From.Joiner;
import org.in.media.res.sqlBuilder.interfaces.model.ITable;

public interface IFrom extends IJoinable, IClause, ITranspilable {

	IFrom from(ITable table);

	IFrom from(ITable... tables);

	IJoinable join(ITable t);

	IJoinable innerJoin(ITable t);

	IJoinable leftJoin(ITable t);

	IJoinable rightJoin(ITable t);

	public Map<ITable, Joiner> joins();

}
