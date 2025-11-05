package org.in.media.res.sqlBuilder.api.query;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;

/**
 * Stage exposing {@code FROM} and {@code JOIN} operations. Extends
 * {@link PredicateStage} so downstream predicate methods remain available after
 * tables are introduced.
 */
public interface FromStage extends PredicateStage, From {

	@Override
	FromStage from(Table table);

	@Override
	FromStage from(Table... tables);

	@Override
	FromStage join(Table table);

	@Override
	FromStage innerJoin(Table table);

	@Override
	FromStage leftJoin(Table table);

	@Override
	FromStage rightJoin(Table table);

	@Override
	FromStage crossJoin(Table table);

	@Override
	FromStage fullOuterJoin(Table table);

	FromStage on(Column left, Column right);

	FromStage on(TableDescriptor<?> left, TableDescriptor<?> right);

	default FromStage from(Query subquery, String alias, String... columnAliases) {
		return from(subquery.as(alias, columnAliases));
	}

	default FromStage join(Query subquery, String alias, String... columnAliases) {
		return join(subquery.as(alias, columnAliases));
	}

	default FromStage innerJoin(Query subquery, String alias, String... columnAliases) {
		return innerJoin(subquery.as(alias, columnAliases));
	}

	default FromStage leftJoin(Query subquery, String alias, String... columnAliases) {
		return leftJoin(subquery.as(alias, columnAliases));
	}

	default FromStage rightJoin(Query subquery, String alias, String... columnAliases) {
		return rightJoin(subquery.as(alias, columnAliases));
	}

	default FromStage crossJoin(Query subquery, String alias, String... columnAliases) {
		return crossJoin(subquery.as(alias, columnAliases));
	}

	default FromStage fullOuterJoin(Query subquery, String alias, String... columnAliases) {
		return fullOuterJoin(subquery.as(alias, columnAliases));
	}
}
