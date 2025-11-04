package org.in.media.res.sqlBuilder.api.query;

import java.util.Objects;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.core.query.QueryImpl;

/**
 * Public facade for fluent query creation. Delegates to {@link QueryImpl} while
 * keeping the implementation detail outside of user code.
 */
public final class SqlQuery {

	private SqlQuery() {
		// no instances
	}

	public static Query newQuery() {
		return QueryImpl.newQuery();
	}

	public static Query fromTable(Table table) {
		return QueryImpl.fromTable(Objects.requireNonNull(table, "table"));
	}

	public static Query selecting(Column... columns) {
		return QueryImpl.selecting(columns);
	}

	public static Query selectingDescriptors(TableDescriptor<?>... descriptors) {
		return QueryImpl.selectingDescriptors(descriptors);
	}

	public static Query countAll() {
		return QueryImpl.countAll();
	}

	public static Query selecting(AggregateOperator agg, Column column) {
		return QueryImpl.newQuery().select(agg, column);
	}

	public static Table toTable(Query query, String alias, String... columnAliases) {
		return QueryImpl.toTable(query, alias, columnAliases);
	}

	public static Table toTable(Query query) {
		return QueryImpl.toTable(query);
	}
}
