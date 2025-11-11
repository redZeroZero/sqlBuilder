package org.in.media.res.sqlBuilder.api.query.spi;

import java.util.List;

import org.in.media.res.sqlBuilder.api.query.Condition;
import org.in.media.res.sqlBuilder.api.query.RawSql;
import org.in.media.res.sqlBuilder.api.query.RawSqlFragment;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;

public interface Where extends Comparator, Connector, Aggregator, Clause, Transpilable {

	List<Condition> conditions();

	default Where whereRaw(String sql) {
		return whereRaw(sql, new SqlParameter<?>[0]);
	}

	default Where whereRaw(String sql, SqlParameter<?>... params) {
		return whereRaw(RawSql.of(sql, params));
	}

	Where whereRaw(RawSqlFragment fragment);

	default Where andRaw(String sql) {
		return andRaw(sql, new SqlParameter<?>[0]);
	}

	default Where andRaw(String sql, SqlParameter<?>... params) {
		return andRaw(RawSql.of(sql, params));
	}

	Where andRaw(RawSqlFragment fragment);

	default Where orRaw(String sql) {
		return orRaw(sql, new SqlParameter<?>[0]);
	}

	default Where orRaw(String sql, SqlParameter<?>... params) {
		return orRaw(RawSql.of(sql, params));
	}

	Where orRaw(RawSqlFragment fragment);
}
