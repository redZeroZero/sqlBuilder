package org.in.media.res.sqlBuilder.core.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.Tables;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.junit.jupiter.api.Test;

class SelectionAliasResolverTest {

	private final Table employee = Tables.builder("Employee", "E")
			.column("ID", "EMP_ID")
			.column("SALARY")
			.build();

	@Test
	void derivesAliasesFromAggregatesAndColumnAliases() {
		Query query = SqlQuery.newQuery().asQuery()
				.select(AggregateOperator.AVG, employee.get("SALARY"))
				.select(employee.get("SALARY"))
				.select(employee.get("ID"))
				.from(employee);

		List<String> aliases = SelectionAliasResolver.resolve(query);

		assertThat(aliases).containsExactly("AVG_SALARY", "SALARY", "EMP_ID");
	}

	@Test
	void rejectsMismatchedAliasCounts() {
		Query query = SqlQuery.newQuery().asQuery()
				.select(employee.get("ID"))
				.select(employee.get("SALARY"))
				.from(employee);

		assertThatThrownBy(() -> SelectionAliasResolver.resolve(query, "onlyOne"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Column alias count");
	}

	@Test
	void requiresAtLeastOneProjectedColumn() {
		Query empty = SqlQuery.newQuery().asQuery();

		assertThatThrownBy(() -> SelectionAliasResolver.resolve(empty))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("requires the subquery to select at least one column");
	}
}
