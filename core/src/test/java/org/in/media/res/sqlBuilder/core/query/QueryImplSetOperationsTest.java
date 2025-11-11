package org.in.media.res.sqlBuilder.core.query;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.Tables;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.api.query.Query;

class QueryImplSetOperationsTest {

    private final Table employee = Tables.builder("Employee", "E")
            .column("ID")
            .column("FIRST_NAME")
            .build();

    private final Table manager = Tables.builder("Manager", "M")
            .column("ID")
            .column("FIRST_NAME")
            .build();

    @Test
    void unionRendersJoinOfTwoQueries() {
        Query left = SqlQuery.newQuery().asQuery();
        left.select(employee.get("ID")).from(employee);

		Query right = SqlQuery.newQuery().asQuery();
		right.select(manager.get("ID")).from(manager);

        String sql = left.union(right).transpile();

        assertThat(sql).contains("UNION");
        assertThat(sql).contains("SELECT \"E\".\"ID\"");
        assertThat(sql).contains("SELECT \"M\".\"ID\"");
    }

    @Test
    void unionAllChainsMultipleQueries() {
        Query base = SqlQuery.newQuery().asQuery();
        base.select(employee.get("ID")).from(employee);

		Query q2 = SqlQuery.newQuery().asQuery();
		q2.select(manager.get("ID")).from(manager);
		Query q3 = SqlQuery.newQuery().asQuery();
		q3.select(manager.get("ID")).from(manager);

		String sql = base.unionAll(q2).unionAll(q3).transpile();

        assertThat(sql).contains("UNION ALL");
        assertThat(sql.split("UNION ALL").length).isGreaterThan(1);
    }

    @Test
    void intersectAndExceptAreSupported() {
        Query q = SqlQuery.newQuery().asQuery();
        q.select(employee.get("ID")).from(employee);

		Query intersect = SqlQuery.newQuery().asQuery();
		intersect.select(manager.get("ID")).from(manager);
		Query except = SqlQuery.newQuery().asQuery();
		except.select(manager.get("ID")).from(manager);

		String sql = q.intersect(intersect).except(except).transpile();

        assertThat(sql).contains("INTERSECT");
        assertThat(sql).contains("MINUS");
    }
}
