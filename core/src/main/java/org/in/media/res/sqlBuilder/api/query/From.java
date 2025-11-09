package org.in.media.res.sqlBuilder.api.query;

import java.util.Map;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.constants.JoinOperator;

public interface From extends Joinable, Clause, Transpilable {

	From from(Table table);

	From from(Table... tables);

	Joinable join(Table t);
	Joinable innerJoin(Table t);
	Joinable leftJoin(Table t);
	Joinable rightJoin(Table t);
	Joinable crossJoin(Table t);
	Joinable fullOuterJoin(Table t);

	public Map<Table, JoinSpec> joins();

	interface JoinSpec {
		JoinOperator getOp();
		Column getCol1();
		Column getCol2();
	}

}
