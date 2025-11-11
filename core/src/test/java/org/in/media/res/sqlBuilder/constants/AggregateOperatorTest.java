package org.in.media.res.sqlBuilder.constants;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AggregateOperatorTest {

    @Test
    void logicalNamesAreExposedInUppercase() {
        assertThat(AggregateOperator.SUM.logicalName()).isEqualTo("sum");
        assertThat(AggregateOperator.AVG.logicalName()).isEqualTo("avg");
        assertThat(AggregateOperator.COUNT.logicalName()).isEqualTo("count");
    }
}
