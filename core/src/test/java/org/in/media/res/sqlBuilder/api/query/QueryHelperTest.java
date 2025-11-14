package org.in.media.res.sqlBuilder.api.query;

import static org.assertj.core.api.Assertions.assertThat;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.Tables;
import org.junit.jupiter.api.Test;

class QueryHelperTest {

    private final Table employee = Tables.builder("Employee", "E")
            .column("ID")
            .column("STATE")
            .build();

    @Test
    void groupBuilderProducesConditionGroup() {
        var group = QueryHelper.group(builder -> builder
                .where(employee.get("STATE")).eq("CA")
                .or(employee.get("STATE")).eq("OR"));

        assertThat(group.transpile()).isEqualTo("(\"E\".\"STATE\" = ? OR \"E\".\"STATE\" = ?)");
    }
}
