package org.in.media.res.sqlBuilder.core.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.Tables;
import org.in.media.res.sqlBuilder.api.query.Condition;
import org.in.media.res.sqlBuilder.api.query.ConditionValue;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlParameters;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.Operator;
import org.in.media.res.sqlBuilder.core.query.dialect.DialectContext;
import org.in.media.res.sqlBuilder.core.query.dialect.Dialects;
import org.in.media.res.sqlBuilder.core.query.transpiler.defaults.DefaultConditionTranspiler;
import org.junit.jupiter.api.Test;

class DefaultConditionTranspilerTest {

	private final Table employee = Tables.builder("Employee", "E")
			.column("SALARY")
			.column("DEPT_ID")
			.column("ID")
			.build();

	private final Column salary = employee.get("SALARY");
	private final Column deptId = employee.get("DEPT_ID");

	@Test
	void eqWithMultipleValuesTurnsIntoInClause() {
		ConditionImpl condition = ConditionImpl.builder()
				.leftColumn(salary)
				.eq()
				.values("10", "20")
				.build();

		try (DialectContext.Scope ignored = DialectContext.scope(Dialects.oracle())) {
			assertThat(condition.transpile()).isEqualTo("\"E\".\"SALARY\" IN (?, ?)");
		}
	}

	@Test
	void notEqWithMultipleValuesTurnsIntoNotIn() {
		ConditionImpl condition = ConditionImpl.builder()
				.leftColumn(deptId)
				.comparisonOp(Operator.NOT_EQ)
				.values("10", "20", "30")
				.build();

		try (DialectContext.Scope ignored = DialectContext.scope(Dialects.oracle())) {
			assertThat(condition.transpile()).isEqualTo("\"E\".\"DEPT_ID\" NOT IN (?, ?, ?)");
		}
	}

	@Test
	void betweenRequiresExactlyTwoValues() {
		ConditionImpl invalid = ConditionImpl.builder()
				.leftColumn(salary)
				.comparisonOp(Operator.BETWEEN)
				.value(ConditionValue.of(SqlParameters.param("minSalary", Integer.class)))
				.build();

		try (DialectContext.Scope ignored = DialectContext.scope(Dialects.oracle())) {
			assertThatThrownBy(invalid::transpile)
					.isInstanceOf(IllegalStateException.class)
					.hasMessageContaining("BETWEEN");
		}
	}

	@Test
	void betweenRendersRangeWithPlaceholders() {
		ConditionImpl between = ConditionImpl.builder()
				.leftColumn(salary)
				.comparisonOp(Operator.BETWEEN)
				.value(ConditionValue.of(SqlParameters.param("minSalary", Integer.class)))
				.value(ConditionValue.of(SqlParameters.param("maxSalary", Integer.class)))
				.build();

		try (DialectContext.Scope ignored = DialectContext.scope(Dialects.oracle())) {
			assertThat(between.transpile()).isEqualTo("\"E\".\"SALARY\" BETWEEN ? AND ?");
		}
	}

	@Test
	void likeAppendsEscapeClause() {
		ConditionImpl like = ConditionImpl.builder()
				.leftColumn(deptId)
				.comparisonOp(Operator.LIKE)
				.value("FIN%")
				.build();

		try (DialectContext.Scope ignored = DialectContext.scope(Dialects.oracle())) {
			assertThat(like.transpile()).contains(" ESCAPE '\\'");
		}
	}

	@Test
	void existsEmbedsSubquery() {
		Query deptQuery = (Query) SqlQuery.newQuery()
				.select(deptId)
				.from(employee)
				.groupBy(deptId);

		ConditionImpl exists = ConditionImpl.builder()
				.comparisonOp(Operator.EXISTS)
				.value(deptQuery)
				.build();

		String sql;
		try (DialectContext.Scope ignored = DialectContext.scope(Dialects.oracle())) {
			sql = exists.transpile();
		}
		assertThat(sql).startsWith("EXISTS (SELECT");
		assertThat(sql).contains("GROUP BY \"E\".\"DEPT_ID\"");
	}

	@Test
	void groupedConditionsAreWrappedOnce() {
		Condition grouped = new Condition() {
			@Override
			public List<ConditionValue> values() {
				return List.of();
			}

			@Override
			public Column getLeft() {
				return null;
			}

			@Override
			public Column getRight() {
				return null;
			}

			@Override
			public Operator getStartOperator() {
				return Operator.AND;
			}

			@Override
			public Operator getOperator() {
				return null;
			}

			@Override
			public AggregateOperator getLeftAgg() {
				return null;
			}

			@Override
			public AggregateOperator getRightAgg() {
				return null;
			}

			@Override
			public String transpile() {
				return "(custom)";
			}
		};

		DefaultConditionTranspiler transpiler = new DefaultConditionTranspiler();
		String sql;
		try (DialectContext.Scope ignored = DialectContext.scope(Dialects.oracle())) {
			sql = transpiler.transpile(grouped);
		}
		assertThat(sql).isEqualTo(" AND (custom)");
	}
}
