package org.in.media.res.sqlBuilder.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.Tables;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.junit.jupiter.api.Test;

class QueryValidationTest {

    private final Table employee = Tables.builder("Employee", "E")
            .column("ID")
            .column("FIRST_NAME")
            .build();

    @Test
    void requireTableRejectsColumnsWithoutOwner() {
        var column = org.in.media.res.sqlBuilder.core.model.ColumnImpl.builder()
                .name("COL")
                .table(null)
                .build();

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> QueryValidation.requireTable(column, "Requires table"));
        assertEquals("Requires table (no owning table)", ex.getMessage());
    }

    @Test
    void requireScalarSubqueryAllowsSingleProjection() {
        Query sub = SqlQuery.query();
        sub.select(employee.get("ID")).from(employee);
        QueryValidation.requireScalarSubquery(sub, "scalar");
    }

    @Test
    void requireScalarSubqueryThrowsWhenMultipleColumns() {
        Query sub = SqlQuery.query();
        sub.select(employee.get("ID"))
                .select(employee.get("FIRST_NAME"))
                .from(employee);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> QueryValidation.requireScalarSubquery(sub, "multi"));
        assertEquals("multi (expected 1 column, got 2)", ex.getMessage());
    }

    @Test
    void requireAnyProjectionThrowsWhenNoColumns() {
        Query sub = SqlQuery.query();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> QueryValidation.requireAnyProjection(sub, "any"));
        assertEquals("any (subquery selects no columns)", ex.getMessage());
    }
}
