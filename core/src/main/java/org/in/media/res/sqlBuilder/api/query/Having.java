package org.in.media.res.sqlBuilder.api.query;

import java.util.List;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;

public interface Having extends Clause, Resetable, Transpilable {

	Having having(Condition condition);

	HavingBuilder having(Column column);

	default HavingBuilder having(TableDescriptor<?> descriptor) {
		return having(descriptor.column());
	}

	default Having havingRaw(String sql) {
		return havingRaw(sql, new SqlParameter<?>[0]);
	}

	default Having havingRaw(String sql, SqlParameter<?>... params) {
		return havingRaw(RawSql.of(sql, params));
	}

	Having havingRaw(RawSqlFragment fragment);

	Having and(Condition condition);

	default Having andRaw(String sql) {
		return andRaw(sql, new SqlParameter<?>[0]);
	}

	default Having andRaw(String sql, SqlParameter<?>... params) {
		return andRaw(RawSql.of(sql, params));
	}

	Having andRaw(RawSqlFragment fragment);

	Having or(Condition condition);

	default Having orRaw(String sql) {
		return orRaw(sql, new SqlParameter<?>[0]);
	}

	default Having orRaw(String sql, SqlParameter<?>... params) {
		return orRaw(RawSql.of(sql, params));
	}

	Having orRaw(RawSqlFragment fragment);

	List<Condition> havingConditions();
}
