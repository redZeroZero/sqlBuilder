package org.in.media.res.sqlBuilder.core.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Map;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.Tables;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.QueryHelper;
import org.in.media.res.sqlBuilder.api.query.SqlAndParams;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.api.query.SqlParameters;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.junit.jupiter.api.Test;

class OptionalAndGroupTest {

    private final Table employee = Tables.builder("Employee", "E")
            .column("ID")
            .column("FIRST_NAME")
            .column("LAST_NAME")
            .build();

    @Test
    void groupedHelpersWrapConditionsWithParentheses() {
        var nameGroup = QueryHelper.group()
                .where(employee.get("FIRST_NAME")).eq("Alice")
                .orGroup()
                    .where(employee.get("LAST_NAME")).eq("Smith")
                .endGroup();

        var idGroup = QueryHelper.group()
                .where(employee.get("ID")).supOrEqTo(1)
                .or(employee.get("ID")).eq(2);

        Query query = SqlQuery.newQuery().asQuery()
                .select(employee)
                .from(employee)
                .where(nameGroup)
                .and(idGroup);

        String sql = query.transpile();

        String firstName = employee.get("FIRST_NAME").transpile(false);
        String lastName = employee.get("LAST_NAME").transpile(false);
        String id = employee.get("ID").transpile(false);

        assertThat(sql).contains("(" + firstName + " = ? OR (" + lastName + " = ?))");
        assertThat(sql).contains("AND (" + id + " >= ? OR " + id + " = ?)");
    }

    @Test
    void optionalEqualsBindsParameterTwice() {
        SqlParameter<String> nameParam = SqlParameters.param("name");

        Query query = SqlQuery.newQuery().asQuery();
        query.select(employee.get("ID"))
                .from(employee)
                .whereOptionalEquals(employee.get("FIRST_NAME"), nameParam);

        SqlAndParams enabled = query.compile().bind(Map.of("name", "Alice"));
        assertThat(enabled.sql()).contains("IS NULL OR");
        assertThat(enabled.params()).containsExactly("Alice", "Alice");

        SqlAndParams disabled = query.compile().bind(Collections.singletonMap("name", null));
        assertThat(disabled.params()).containsExactly(null, null);
    }

    @Test
    void existsAppendsSubqueryPredicate() {
        Query sub = SqlQuery.newQuery().asQuery();
        sub.select(employee.get("ID")).from(employee).where(employee.get("ID")).eq(1);

        Query main = SqlQuery.newQuery().asQuery();
        main.select(employee.get("FIRST_NAME"))
                .from(employee)
                .exists(sub);

        String sql = main.transpile();

        assertThat(sql).contains("EXISTS (SELECT");
        assertThat(sql).contains(employee.get("ID").transpile(false));
    }
}
