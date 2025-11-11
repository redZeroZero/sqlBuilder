package org.in.media.res.sqlBuilder.api.query.spi;

import java.util.List;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.api.query.RawSql;
import org.in.media.res.sqlBuilder.api.query.RawSqlFragment;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;

public interface GroupBy extends Clause, Resetable, Transpilable {

	GroupBy groupBy(Column column);

	default GroupBy groupBy(TableDescriptor<?> descriptor) {
		return groupBy(descriptor.column());
	}

	GroupBy groupBy(Column... columns);

	default GroupBy groupBy(TableDescriptor<?>... descriptors) {
		for (TableDescriptor<?> descriptor : descriptors) {
			groupBy(descriptor.column());
		}
		return this;
	}

	default GroupBy groupByRaw(String sql) {
		return groupByRaw(sql, new SqlParameter<?>[0]);
	}

	default GroupBy groupByRaw(String sql, SqlParameter<?>... params) {
		return groupByRaw(RawSql.of(sql, params));
	}

	GroupBy groupByRaw(RawSqlFragment fragment);

	List<Column> groupByColumns();

}
