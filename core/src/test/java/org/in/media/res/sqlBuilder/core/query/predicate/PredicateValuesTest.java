package org.in.media.res.sqlBuilder.core.query.predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Date;

import org.in.media.res.sqlBuilder.api.query.ConditionValue;
import org.in.media.res.sqlBuilder.constants.Operator;
import org.junit.jupiter.api.Test;

class PredicateValuesTest {

	@Test
	void stringsNormalizeEqualsToInWhenMultipleValues() {
		PredicateValues.Result result = PredicateValues.strings(Operator.EQ, "A", "B");

		assertThat(result.operator()).isEqualTo(Operator.IN);
		assertThat(result.values())
				.extracting(ConditionValue::value)
				.containsExactly("A", "B");
	}

	@Test
	void numbersSupportMixedNumericTypes() {
		PredicateValues.Result result = PredicateValues.numbers(Operator.IN, 1, 2.5d, 3L);

		assertThat(result.operator()).isEqualTo(Operator.IN);
		assertThat(result.values())
				.extracting(ConditionValue::value)
				.containsExactly(1, 2.5d, 3);
	}

	@Test
	void emptyInputRaisesHelpfulError() {
		assertThatThrownBy(() -> PredicateValues.strings(Operator.EQ))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("requires at least one value");
	}

	@Test
	void nullValueRejectedWhenMapping() {
		assertThatThrownBy(() -> PredicateValues.dates(Operator.IN, new Date(), null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("value");
	}
}
