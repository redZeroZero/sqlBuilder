package org.in.media.res.sqlBuilder.core.query;

import static org.assertj.core.api.Assertions.assertThat;

import org.in.media.res.sqlBuilder.api.query.CompiledQuery;
import org.in.media.res.sqlBuilder.api.query.CteRef;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlAndParams;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.api.query.SqlParameters;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.api.query.WithBuilder;
import org.in.media.res.sqlBuilder.testutil.TestSchema;
import org.junit.jupiter.api.Test;

class QueryRenderingIntegrationTest {

	@Test
	void compileCollectsPlaceholdersAcrossClausesAndSetOperations() {
		SqlParameter<Integer> cteParam = SqlParameters.param("cteFilter", Integer.class);
		Query cteQuery = SqlQuery.newQuery().asQuery();
		cteQuery.select(TestSchema.EMP_ID.column())
				.from(TestSchema.EMPLOYEES)
				.where(TestSchema.EMP_ID.column()).eq(cteParam);

		WithBuilder with = SqlQuery.with();
		CteRef cte = with.cte("cte_emps", cteQuery, "EMP_ID");

		SqlParameter<Integer> selectParam = SqlParameters.param("selectMarker", Integer.class);
		SqlParameter<Integer> fromParam = SqlParameters.param("fromFilter", Integer.class);
		SqlParameter<Integer> whereParam = SqlParameters.param("whereFilter", Integer.class);
		SqlParameter<Integer> groupParam = SqlParameters.param("groupFilter", Integer.class);
		SqlParameter<Integer> havingParam = SqlParameters.param("havingFilter", Integer.class);
		SqlParameter<Integer> orderParam = SqlParameters.param("orderFilter", Integer.class);
		SqlParameter<Integer> unionParam = SqlParameters.param("unionFilter", Integer.class);

		Query main = with.main(SqlQuery.newQuery().asQuery());
		main.selectRaw("? AS tag", selectParam);
		main.select(cte.column("EMP_ID"));
		main.from(cte);
		main.joinRaw("JOIN cte_emps t2 ON t2.EMP_ID = \"cte_emps\".\"EMP_ID\" AND t2.EMP_ID > ?", fromParam);
		main.groupByRaw("ROLLUP(\"cte_emps\".\"EMP_ID\" + ?)", groupParam);
		main.havingRaw("COUNT(*) > ?", havingParam);
		main.orderByRaw("\"cte_emps\".\"EMP_ID\" + ?", orderParam);
		main.whereRaw("EXISTS (SELECT 1 FROM dual WHERE value = ?)", whereParam);
		main.limitAndOffset(3, 5);

		Query union = SqlQuery.newQuery().asQuery();
		union.select(TestSchema.EMP_ID.column())
				.from(TestSchema.EMPLOYEES)
				.where(TestSchema.EMP_ID.column()).eq(unionParam);
		main.union(union);

		CompiledQuery compiled = main.compile();
		SqlAndParams bound = compiled.bind(700, 99, 88, 77, 66, 55, 44, 33);

		assertThat(bound.params()).containsExactly(
				700, // CTE filter
				99,  // selectRaw
				88,  // fromRaw
				77,  // whereRaw
				66,  // groupByRaw
				55,  // havingRaw
				44,  // orderByRaw
				5L,  // offset (dialect emits Long)
				3L,  // limit (dialect emits Long)
				33   // union filter
		);
	}
}
