package org.in.media.res.sqlBuilder.api.query;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.ColumnRef;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;

/**
 * Fluent DSL for building {@code INSERT} statements.
 */
public interface InsertQuery {

	String transpile();

	SqlAndParams render();

	CompiledQuery compile();

	InsertQuery columns(Column... columns);

	InsertQuery columns(TableDescriptor<?>... descriptors);

	default InsertQuery columns(ColumnRef<?>... refs) {
		if (refs != null) {
			for (ColumnRef<?> ref : refs) {
				columns(ref.column());
			}
		}
		return this;
	}

	InsertQuery values(Object... values);

	InsertQuery valuesRaw(String sql);

	InsertQuery valuesRaw(String sql, SqlParameter<?>... params);

	InsertQuery valuesRaw(RawSqlFragment fragment);

	InsertQuery select(Query query);

	Table target();
}
