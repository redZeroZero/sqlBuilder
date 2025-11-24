package org.in.media.res.sqlBuilder.api.query;

import java.util.Objects;
import java.util.Arrays;
import java.util.List;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.core.query.DeleteQueryImpl;
import org.in.media.res.sqlBuilder.core.query.InsertQueryImpl;
import org.in.media.res.sqlBuilder.core.query.QueryImpl;
import org.in.media.res.sqlBuilder.core.query.UpdateQueryImpl;
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

	public static Query query() {
		return (Query) QueryImpl.newQuery();
	}

	public static Query query(Dialect dialect) {
		return (Query) QueryImpl.newQuery(dialect);
	}

	public static Query query(org.in.media.res.sqlBuilder.api.model.Schema schema) {
		return (Query) QueryImpl.newQuery(schema.getDialect());
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

	/**
	 * Convenience entry point to start a chained CTE declaration without calling {@link #with()} explicitly.
	 * Returns the staged declaration step, preserving the underlying builder so {@code ref()} and {@code and()}
	 * behave the same as when starting from {@code with()}.
	 */
	public static WithBuilder.CteDeclarationStep withCte(String name) {
		return new WithBuilderImpl().with(name);
	}

	/**
	 * Convenience entry point to start a chained CTE declaration (name + body) without calling {@link #with()} explicitly.
	 * Returns the CTE step so callers can grab the {@link org.in.media.res.sqlBuilder.api.query.CteRef} and continue with {@code and()}.
	 */
	public static WithBuilder.CteStep withCte(String name, Query query, String... columnAliases) {
		return new WithBuilderImpl().with(name, query, columnAliases);
	}

	/**
	 * Convenience wrapper that stores CTE refs by name and lets you attach the main query in one chain.
	 */
	public static WithChain withChain() {
		return new WithChain(new WithBuilderImpl());
	}

	/**
	 * Render a fully raw SQL string with positional parameters.
	 *
	 * @param sql SQL text containing {@code ?} placeholders
	 * @param params positional parameter values matching the placeholders
	 * @return immutable SQL + params pair
	 */
	public static SqlAndParams raw(String sql, Object... params) {
		List<Object> paramList = params == null ? List.of() : Arrays.asList(params);
		return new SqlAndParams(sql, paramList);
	}

	public static Table toTable(Query query, String alias, String... columnAliases) {
		return QueryImpl.toTable(query, alias, columnAliases);
	}

	public static Table toTable(Query query) {
		return QueryImpl.toTable(query);
	}

	/**
	 * Helper to widen a staged builder back to the full {@link Query} API. Use this when you hold a
	 * staged type (e.g., after grouping/having) and need ordering/limit methods.
	 *
	 * @throws IllegalArgumentException if the given stage does not implement {@link Query}.
	 */
	public static Query asQuery(Object stage) {
		if (stage instanceof Query q) {
			return q;
		}
		throw new IllegalArgumentException(
				"Stage does not implement Query; start with SqlQuery.query() or capture .asQuery() earlier");
	}

	/**
	 * Validate a query without executing it. Returns a report containing any errors/warnings
	 * (e.g., grouping mismatches, missing aliases for derived tables with raw projections, etc.).
	 */
	public static org.in.media.res.sqlBuilder.core.query.ValidationReport validate(Query query) {
		return org.in.media.res.sqlBuilder.core.query.QueryValidator.validate(query, null);
	}

	public static UpdateQuery update(Table table) {
		return UpdateQueryImpl.update(Objects.requireNonNull(table, "table"));
	}

	public static UpdateQuery update(Table table, Dialect dialect) {
		return UpdateQueryImpl.update(Objects.requireNonNull(table, "table"),
				Objects.requireNonNull(dialect, "dialect"));
	}

	public static UpdateQuery update(org.in.media.res.sqlBuilder.api.model.Schema schema, Table table) {
		Objects.requireNonNull(schema, "schema");
		return UpdateQueryImpl.update(Objects.requireNonNull(table, "table"), schema.getDialect());
	}

	public static InsertQuery insertInto(Table table) {
		return InsertQueryImpl.into(Objects.requireNonNull(table, "table"));
	}

	public static InsertQuery insertInto(Table table, Dialect dialect) {
		return InsertQueryImpl.into(Objects.requireNonNull(table, "table"), Objects.requireNonNull(dialect, "dialect"));
	}

	public static InsertQuery insertInto(org.in.media.res.sqlBuilder.api.model.Schema schema, Table table) {
		Objects.requireNonNull(schema, "schema");
		return InsertQueryImpl.into(Objects.requireNonNull(table, "table"), schema.getDialect());
	}

	public static DeleteQuery deleteFrom(Table table) {
		return DeleteQueryImpl.from(Objects.requireNonNull(table, "table"));
	}

	public static DeleteQuery deleteFrom(Table table, Dialect dialect) {
		return DeleteQueryImpl.from(Objects.requireNonNull(table, "table"), Objects.requireNonNull(dialect, "dialect"));
	}

	public static DeleteQuery deleteFrom(org.in.media.res.sqlBuilder.api.model.Schema schema, Table table) {
		Objects.requireNonNull(schema, "schema");
		return DeleteQueryImpl.from(Objects.requireNonNull(table, "table"), schema.getDialect());
	}
}
