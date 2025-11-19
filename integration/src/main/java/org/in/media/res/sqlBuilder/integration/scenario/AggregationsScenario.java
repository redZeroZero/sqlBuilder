package org.in.media.res.sqlBuilder.integration.scenario;

import java.sql.Connection;
import java.sql.SQLException;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.integration.model.EmployeesTable;
import org.in.media.res.sqlBuilder.integration.model.IntegrationSchema;
import org.in.media.res.sqlBuilder.integration.model.JobsTable;

public final class AggregationsScenario implements IntegrationScenario {

	@Override
	public String title() {
		return "3. Aggregations with GROUP BY / HAVING";
	}

	@Override
	public void run(Connection connection) throws SQLException {
		Table employees = IntegrationSchema.employees();
		Table jobs = IntegrationSchema.jobs();

		Query query = SqlQuery.query();
		query.select(EmployeesTable.C_FIRST_NAME)
				.select(AggregateOperator.AVG, JobsTable.C_SALARY)
				.from(employees)
				.join(jobs).on(EmployeesTable.C_ID, JobsTable.C_EMPLOYEE_ID)
				.groupBy(EmployeesTable.C_FIRST_NAME)
				.having(JobsTable.C_SALARY).avg(JobsTable.C_SALARY).supTo(60_000)
				.orderBy(EmployeesTable.C_FIRST_NAME);

		ScenarioSupport.executeQuery(connection, query.render(), title());
	}
}
