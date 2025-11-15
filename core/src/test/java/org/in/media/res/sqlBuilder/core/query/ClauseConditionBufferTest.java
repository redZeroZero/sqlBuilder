package org.in.media.res.sqlBuilder.core.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.Tables;
import org.in.media.res.sqlBuilder.api.query.Condition;
import org.in.media.res.sqlBuilder.api.query.ConditionValue;
import org.in.media.res.sqlBuilder.api.query.RawSql;
import org.in.media.res.sqlBuilder.api.query.SqlParameters;
import org.in.media.res.sqlBuilder.constants.Operator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClauseConditionBufferTest {

	private final Table employee = Tables.builder("Employee", "E")
			.column("ID")
			.column("SALARY")
			.build();

	private final Column id = employee.get("ID");

	private List<Condition> conditions;
	private ClauseConditionBuffer buffer;

	@BeforeEach
	void setUp() {
		this.conditions = new ArrayList<>();
		this.buffer = new ClauseConditionBuffer(conditions, "Missing predicate");
	}

	@Test
	void applyValuesAssignsOperatorAndValues() {
		ConditionImpl base = ConditionImpl.builder().leftColumn(id).build();
		ConditionValue parameter = ConditionValue.of(SqlParameters.param("id", Long.class));

		ConditionImpl updated = ClauseConditionBuffer.applyValues(base, Operator.EQ, List.of(parameter));

		assertThat(updated.getOperator()).isEqualTo(Operator.EQ);
		assertThat(updated.values()).containsExactly(parameter);
	}

	@Test
	void applyValuesWithoutRequiredValuesThrows() {
		ConditionImpl base = ConditionImpl.builder().leftColumn(id).build();
		assertThatThrownBy(() -> ClauseConditionBuffer.applyValues(base, Operator.EQ, List.of()))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void appendStandaloneCreatesConditionWhenBufferEmpty() {
		buffer.appendStandalone(Operator.EQ, ConditionValue.of(SqlParameters.param("id", Long.class)));

		assertThat(conditions).hasSize(1);
		assertThat(conditions.getFirst()).isInstanceOf(ConditionImpl.class);
		assertThat(((ConditionImpl) conditions.getFirst()).getOperator()).isEqualTo(Operator.EQ);
	}

	@Test
	void addRawPreservesOperatorAndParameters() {
		var min = SqlParameters.param("minSalary", Integer.class);
		var max = SqlParameters.param("maxSalary", Integer.class);

		buffer.addRaw(RawSql.of("E.SALARY BETWEEN ? AND ?", min, max), Operator.AND);

		assertThat(conditions).hasSize(1);
		assertThat(conditions.getFirst()).isInstanceOf(RawCondition.class);
		RawCondition raw = (RawCondition) conditions.getFirst();
		assertThat(raw.getStartOperator()).isEqualTo(Operator.AND);
		assertThat(raw.values()).hasSize(2);
		assertThat(raw.transpile()).contains("BETWEEN ? AND ?");
	}

	@Test
	void appendStandaloneMergesWithPendingCondition() {
		buffer.add(ConditionImpl.builder().build(), null);

		var param = ConditionValue.of(SqlParameters.param("flag"));
		buffer.appendStandalone(Operator.IS_NULL, param);

		assertThat(buffer.snapshot())
				.hasSize(1)
				.first()
				.isInstanceOf(ConditionImpl.class)
				.satisfies(condition -> {
					ConditionImpl impl = (ConditionImpl) condition;
					assertThat(impl.getOperator()).isEqualTo(Operator.IS_NULL);
					assertThat(impl.values()).containsExactly(param);
				});
	}

	@Test
	void lastThrowsWhenNoConditionAvailable() {
		assertThatThrownBy(buffer::last)
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Missing predicate");
	}

	@Test
	void normalizeConvertsGroupBuilders() {
		ConditionGroupBuilder group = new ConditionGroupBuilder()
				.where(employee.get("ID")).eq(SqlParameters.param("id"))
				.andGroup(builder -> builder.where(employee.get("SALARY")).supOrEqTo(100));

		Condition normalized = buffer.normalize(group, Operator.OR);

		assertThat(normalized).isInstanceOf(org.in.media.res.sqlBuilder.core.query.predicate.ConditionGroup.class);
		assertThat(normalized.getStartOperator()).isEqualTo(Operator.OR);
	}
}
