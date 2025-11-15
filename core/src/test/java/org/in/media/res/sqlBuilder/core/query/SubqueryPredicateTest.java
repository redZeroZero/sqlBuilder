package org.in.media.res.sqlBuilder.core.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.Tables;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.junit.jupiter.api.Test;

class SubqueryPredicateTest {

    private final Table employee = Tables.builder("Employee", "E")
            .column("ID")
            .column("SALARY")
            .build();

    @Test
    void inSubqueryRendersScalarComparison() {
        Query sub = SqlQuery.query();
        sub.select(employee.get("ID")).from(employee).where(employee.get("SALARY")).supOrEqTo(100);

        Query query = SqlQuery.query();
        query.select(employee.get("ID")).from(employee).where(employee.get("ID")).in(sub);

        String sql = query.transpile();
        assertThat(sql).contains("IN (SELECT");
        assertThat(sql).contains("\"E\".\"ID\"");
    }

    @Test
    void greaterThanSubqueryRequiresScalarProjection() {
        Query sub = SqlQuery.query();
        sub.select(employee.get("SALARY")).from(employee);

        Query query = SqlQuery.query();
        query.select(employee.get("ID")).from(employee).where(employee.get("SALARY")).supTo(sub);

        String sql = query.transpile();
        assertThat(sql).contains("> (SELECT");
    }

    @Test
    void invalidSubqueryThrows() {
        Query invalid = SqlQuery.query();
        invalid.select(employee.get("ID"))
                .select(employee.get("SALARY"))
                .from(employee);

        assertThrows(IllegalArgumentException.class, () -> {
            Query query = SqlQuery.query();
            query.select(employee.get("ID")).from(employee).where(employee.get("ID")).in(invalid);
        });
}
}
