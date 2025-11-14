package org.in.media.res.sqlBuilder.api.query;

import static org.assertj.core.api.Assertions.assertThat;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.Tables;
import org.junit.jupiter.api.Test;

class SqlQueryFacadeTest {

    private final Table employee = Tables.builder("Employee", "E")
            .column("ID")
            .column("NAME")
            .build();

    @Test
    void selectingFacadeBootstrapsQuery() {
        Query query = SqlQuery.selecting(employee.get("ID")).from(employee).asQuery();
        assertThat(query.transpile()).contains("SELECT \"E\".\"ID\"");
    }

    @Test
    void countAllFacadeBuildsCountProjection() {
        Query query = SqlQuery.countAll().from(employee).asQuery();
        assertThat(query.transpile()).startsWith("SELECT COUNT(*)");
    }
}
