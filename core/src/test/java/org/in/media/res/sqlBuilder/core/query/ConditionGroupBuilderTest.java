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
}
