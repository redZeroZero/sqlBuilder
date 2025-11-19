package org.in.media.res.sqlBuilder.integration.scenario;

import java.sql.Connection;
import java.sql.SQLException;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.integration.model.EmployeesTable;
import org.in.media.res.sqlBuilder.integration.model.IntegrationSchema;
import org.in.media.res.sqlBuilder.integration.model.JobsTable;

public final class JoinWithFiltersScenario implements IntegrationScenario {

	@Override
	public String title() {
		return "2. Joins with Filters";
	}

	@Override
	public void run(Connection connection) throws SQLException {
		Table employees = IntegrationSchema.employees();
		Table jobs = IntegrationSchema.jobs();

		Query query = SqlQuery.query();
		query.select(EmployeesTable.C_FIRST_NAME, JobsTable.C_TITLE)
				.from(employees)
				.leftJoin(jobs).on(EmployeesTable.C_ID, JobsTable.C_EMPLOYEE_ID)
				.where(JobsTable.C_SALARY).supOrEqTo(50_000);

		ScenarioSupport.executeQuery(connection, query.render(), title());
	}
}
