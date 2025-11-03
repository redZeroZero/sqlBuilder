package org.in.media.res.sqlBuilder.api.query;

import java.util.Map;

import org.in.media.res.sqlBuilder.core.query.FromImpl.Joiner;
import org.in.media.res.sqlBuilder.api.model.Table;

public interface From extends Joinable, Clause, Transpilable {

	From from(Table table);

	From from(Table... tables);

	Joinable join(Table t);
	Joinable innerJoin(Table t);
	Joinable leftJoin(Table t);
	Joinable rightJoin(Table t);
	Joinable crossJoin(Table t);
	Joinable fullOuterJoin(Table t);

	public Map<Table, Joiner> joins();

}
