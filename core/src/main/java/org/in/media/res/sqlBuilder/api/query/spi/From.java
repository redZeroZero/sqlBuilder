package org.in.media.res.sqlBuilder.api.query.spi;

import java.util.Map;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.RawSqlFragment;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.constants.JoinOperator;

public interface From extends Joinable, Clause, Transpilable {

	From from(Table table);

	From from(Table... tables);

	From fromRaw(String sql);

	From fromRaw(String sql, SqlParameter<?>... params);

	From fromRaw(RawSqlFragment fragment);

	Joinable join(Table t);
	Joinable innerJoin(Table t);
	Joinable leftJoin(Table t);
	Joinable rightJoin(Table t);
	Joinable crossJoin(Table t);
	Joinable fullOuterJoin(Table t);

	From joinRaw(String sql);

	From joinRaw(String sql, SqlParameter<?>... params);

	From joinRaw(RawSqlFragment fragment);

	From leftJoinRaw(String sql);

	From leftJoinRaw(String sql, SqlParameter<?>... params);

	From leftJoinRaw(RawSqlFragment fragment);

	From rightJoinRaw(String sql);

	From rightJoinRaw(String sql, SqlParameter<?>... params);

	From rightJoinRaw(RawSqlFragment fragment);

	From fullOuterJoinRaw(String sql);

	From fullOuterJoinRaw(String sql, SqlParameter<?>... params);

	From fullOuterJoinRaw(RawSqlFragment fragment);

	From crossJoinRaw(String sql);

	From crossJoinRaw(String sql, SqlParameter<?>... params);

	From crossJoinRaw(RawSqlFragment fragment);

	public Map<Table, JoinSpec> joins();

	interface JoinSpec {
		JoinOperator getOp();
		Column getCol1();
		Column getCol2();
	}

}
