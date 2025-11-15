package org.in.media.res.sqlBuilder.core.query;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.Tables;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.api.query.WithBuilder;

class QueryImplCteSupportTest {

    private final Table employee = Tables.builder("Employee", "E")
            .column("ID")
            .column("FIRST_NAME")
            .build();

    @Test
    void withClauseRendersBeforeMainQuery() {
        Query cteQuery = SqlQuery.query();
        cteQuery.select(employee.get("ID")).from(employee);

        WithBuilder with = SqlQuery.with();
        var cte = with.cte("ids", cteQuery, "ID");

        Query main = SqlQuery.query();
        main.select(cte.column("ID")).from(cte);

        String sql = with.main(main).asQuery().transpile();

        assertThat(sql).startsWith("WITH \"ids\"");
        assertThat(sql).contains("SELECT \"ids\".\"ID\"");
    }
}
