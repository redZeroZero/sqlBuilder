package org.in.media.res.sqlBuilder.api.query;

import java.util.Objects;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.core.query.QueryImpl;
import org.in.media.res.sqlBuilder.core.query.cte.WithBuilderImpl;

/**
 * Public facade for fluent query creation. Delegates to {@link QueryImpl} while
 * keeping the implementation detail outside of user code.
 */
public final class SqlQuery {

	private SqlQuery() {
		// no instances
	}

	public static SelectStage newQuery() {
		return (SelectStage) QueryImpl.newQuery();
	}

	public static SelectStage newQuery(Dialect dialect) {
		return (SelectStage) QueryImpl.newQuery(dialect);
	}

	public static SelectStage newQuery(org.in.media.res.sqlBuilder.api.model.Schema schema) {
		return (SelectStage) QueryImpl.newQuery(schema.getDialect());
	}

	public static FromStage fromTable(Table table) {
		return (FromStage) QueryImpl.fromTable(Objects.requireNonNull(table, "table"));
	}

	public static SelectStage selecting(Column... columns) {
		return (SelectStage) QueryImpl.selecting(columns);
	}

	public static SelectStage selectingDescriptors(TableDescriptor<?>... descriptors) {
		return (SelectStage) QueryImpl.selectingDescriptors(descriptors);
	}

	public static SelectStage countAll() {
		return (SelectStage) QueryImpl.countAll();
	}

	public static SelectStage selecting(AggregateOperator agg, Column column) {
		return (SelectStage) QueryImpl.newQuery().select(agg, column);
	}

	public static WithBuilder with() {
		return new WithBuilderImpl();
	}

	public static Table toTable(Query query, String alias, String... columnAliases) {
		return QueryImpl.toTable(query, alias, columnAliases);
	}

	public static Table toTable(Query query) {
		return QueryImpl.toTable(query);
	}
}
