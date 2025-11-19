package org.in.media.res.sqlBuilder.integration.scenario;

import java.sql.Connection;
import java.sql.SQLException;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.constants.SortDirection;
import org.in.media.res.sqlBuilder.integration.model.IntegrationSchema;
import org.in.media.res.sqlBuilder.integration.model.JobsTable;

public final class PaginationScenario implements IntegrationScenario {

	@Override
	public String title() {
		return "4. Pagination";
	}

	@Override
	public void run(Connection connection) throws SQLException {
		Table jobs = IntegrationSchema.jobs();
		Query query = SqlQuery.query();
		query.select(JobsTable.C_TITLE)
				.from(jobs)
				.orderBy(JobsTable.C_SALARY, SortDirection.DESC)
				.limitAndOffset(10, 1);

		ScenarioSupport.executeQuery(connection, query.render(), title());
	}
}
