package org.in.media.res.sqlBuilder.core.query;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.Tables;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;

class RawFragmentAndHintTest {

    private final Table employee = Tables.builder("Employee", "E")
            .column("ID")
            .column("FIRST_NAME")
            .build();

    @Test
    void rawWhereFragmentsAreInlined() {
        String sql = SqlQuery.newQuery()
                .select(employee.get("ID"))
                .from(employee)
                .whereRaw("E.FIRST_NAME = 'Bob'")
                .transpile();

        assertThat(sql).contains("FIRST_NAME = 'Bob'");
    }

    @Test
    void hintsRenderAfterSelectKeyword() {
        String sql = SqlQuery.newQuery()
                .hint("/*+ INDEX(E EMP_IDX) */")
                .select(employee.get("ID"))
                .from(employee)
                .transpile();

        assertThat(sql).startsWith("SELECT /*+ INDEX(E EMP_IDX) */");
    }
}
