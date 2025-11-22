package org.in.media.res.sqlBuilder.integration.scenario;

import java.sql.Connection;
import java.sql.SQLException;

import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.constants.SortDirection;
import org.in.media.res.sqlBuilder.integration.model.EmployeesTable;
import org.in.media.res.sqlBuilder.integration.model.IntegrationSchema;

public final class AboveDepartmentAverageScenario implements IntegrationScenario {

	@Override
	public String title() {
		return "18. Salary above department average (correlated subquery)";
	}

	@Override
	public void run(Connection connection) throws SQLException {
		var employees = IntegrationSchema.employees();

		Query query = SqlQuery.query();
		query.select(EmployeesTable.C_FIRST_NAME)
				.select(EmployeesTable.C_LAST_NAME)
				.select(EmployeesTable.C_SALARY)
				.from(employees)
				.whereRaw("""
						e.salary > (
							SELECT AVG(j.salary)
							FROM jobs j
							JOIN employees e2 ON j.employee_id = e2.id
							WHERE e2.department_id = e.department_id
						)
						""")
				.orderBy(EmployeesTable.C_SALARY, SortDirection.DESC);

		ScenarioSupport.executeQuery(connection, query.render(), title());
	}
}
