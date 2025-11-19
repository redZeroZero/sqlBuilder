package org.in.media.res.sqlBuilder.integration.scenario;

import java.sql.Connection;
import java.sql.SQLException;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.integration.model.EmployeesTable;
import org.in.media.res.sqlBuilder.integration.model.IntegrationSchema;
import org.in.media.res.sqlBuilder.integration.model.JobsTable;

public final class SubqueryFilteringScenario implements IntegrationScenario {

	@Override
	public String title() {
		return "9. Filtering with Subqueries";
	}

	@Override
	public void run(Connection connection) throws SQLException {
		Table jobs = IntegrationSchema.jobs();
		Table employees = IntegrationSchema.employees();

		Query highSalaryIds = SqlQuery.newQuery()
				.select(JobsTable.C_EMPLOYEE_ID)
				.from(jobs)
				.where(JobsTable.C_SALARY).supOrEqTo(80_000)
				.asQuery();

		Query jobExists = SqlQuery.newQuery()
				.select(JobsTable.C_ID)
				.from(jobs)
				.where(JobsTable.C_JOB_TYPE).eq("FULL_TIME")
				.asQuery();

		Query query = SqlQuery.query();
		query.select(EmployeesTable.C_FIRST_NAME)
				.select(EmployeesTable.C_LAST_NAME)
				.from(employees)
				.where(EmployeesTable.C_ID).in(highSalaryIds)
				.exists(jobExists);

		ScenarioSupport.executeQuery(connection, query.render(), title());
	}
}
