package org.in.media.res.sqlBuilder.core.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.Tables;
import org.in.media.res.sqlBuilder.api.query.QueryHelper;
import org.junit.jupiter.api.Test;

class ConditionGroupBuilderTest {

    private final Table employee = Tables.builder("Employee", "E")
            .column("FIRST_NAME")
            .column("LAST_NAME")
            .build();

	@Test
	void nestedGroupsProduceParentheses() {
		var group = QueryHelper.group()
                .where(employee.get("FIRST_NAME")).eq("Alice")
                .orGroup()
                    .where(employee.get("LAST_NAME")).eq("Smith")
                .endGroup()
                .build();

		assertThat(group.transpile()).isEqualTo(
				"(\"E\".\"FIRST_NAME\" = ? OR (\"E\".\"LAST_NAME\" = ?))");
	}

	@Test
	void emptyGroupNotAllowed() {
		assertThrows(IllegalStateException.class, () -> QueryHelper.group().build());
	}

	@Test
	void aggregatesAndMultipleValueTypesAreRendered() {
		var subquery = org.in.media.res.sqlBuilder.api.query.SqlQuery.query();
		subquery.select(employee.get("FIRST_NAME")).from(employee);

		var group = QueryHelper.group()
				.where(employee.get("FIRST_NAME")).like("A%")
				.and(employee.get("FIRST_NAME")).notLike("B%")
				.and(employee.get("LAST_NAME")).in("Smith", "Doe")
				.and(employee.get("LAST_NAME")).notIn("Brown")
				.and(employee.get("FIRST_NAME")).min(employee.get("FIRST_NAME")).supOrEqTo(1)
				.and(employee.get("LAST_NAME")).max(employee.get("LAST_NAME")).infOrEqTo(999)
				.or(employee.get("FIRST_NAME")).avg(employee.get("FIRST_NAME")).between(1, 5)
				.or(employee.get("FIRST_NAME")).sum(employee.get("FIRST_NAME")).notEq(99)
				.and(employee.get("FIRST_NAME")).col(employee.get("LAST_NAME")).eq("expected")
				.and(employee.get("FIRST_NAME")).eq(subquery)
				.build();

		assertThat(group.transpile())
				.contains("MIN(\"E\".\"FIRST_NAME\") >= ?")
				.contains("MAX(\"E\".\"LAST_NAME\") <= ?")
				.contains("AVG(\"E\".\"FIRST_NAME\") BETWEEN ? AND ?")
				.contains("SUM(\"E\".\"FIRST_NAME\") <> ?")
				.contains("\"E\".\"LAST_NAME\" = ?");
	}

	@Test
	void groupConsumersAutomaticallyCloseNestedScopes() {
		var group = QueryHelper.group()
				.where(employee.get("FIRST_NAME")).eq("Alice")
				.andGroup(nested -> nested.where(employee.get("LAST_NAME")).eq("Smith"))
				.orGroup(nested -> nested.where(employee.get("LAST_NAME")).eq("Jones"))
				.build();

		assertThat(group.transpile()).contains("AND (\"E\".\"LAST_NAME\" = ?)");
		assertThat(group.transpile()).contains("OR (\"E\".\"LAST_NAME\" = ?)");
	}

	@Test
	void rawFragmentsIncludeParameters() {
		var group = QueryHelper.group()
				.whereRaw("EXISTS (SELECT 1 FROM dual)")
				.andRaw(" salary > ? ",
						org.in.media.res.sqlBuilder.api.query.SqlParameters.param("min", Integer.class))
				.build();

		assertThat(group.transpile()).contains("EXISTS");
		assertThat(group.transpile()).contains("salary > ?");
	}

	@Test
	void endGroupOnRootThrows() {
		ConditionGroupBuilder builder = QueryHelper.group()
				.where(employee.get("FIRST_NAME")).eq("Alice");

		assertThrows(IllegalStateException.class, builder::endGroup);
	}
}
