package org.in.media.res.sqlBuilder.core.query;

import static org.assertj.core.api.Assertions.assertThat;

import org.in.media.res.sqlBuilder.api.query.SqlAndParams;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.testutil.TestSchema;
import org.junit.jupiter.api.Test;

/**
 * Ensures parameters from derived-table subqueries are carried into the outer query.
 */
class DerivedTablePlaceholderTest {

	@Test
	void derivedTableParamsPropagate() {
		var inner = SqlQuery.newQuery()
				.select(TestSchema.EMP_ID)
				.from(TestSchema.EMPLOYEES)
				.where(TestSchema.EMP_NAME).eq("Alice")
				.asQuery();

		var derived = SqlQuery.toTable(inner, "active_emp", "id");

		SqlAndParams outer = SqlQuery.query()
				.select(derived.get("id"))
				.from(derived)
				.render();

		assertThat(outer.params()).containsExactly("Alice");
	}
}
