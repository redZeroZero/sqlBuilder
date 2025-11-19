package org.in.media.res.sqlBuilder.integration.scenario;

import java.sql.Connection;
import java.sql.SQLException;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.integration.model.EmployeesTable;
import org.in.media.res.sqlBuilder.integration.model.IntegrationSchema;

public final class OptimizerHintScenario implements IntegrationScenario {

	@Override
	public String title() {
		return "Optimizer Hints";
	}

	@Override
	public void run(Connection connection) throws SQLException {
		Table employees = IntegrationSchema.employees();
		Query query = SqlQuery.newQuery()
				.hint("/*+ INDEX(e employees_department_id_idx) */")
				.select(EmployeesTable.C_FIRST_NAME)
				.select(EmployeesTable.C_LAST_NAME)
				.from(employees)
				.asQuery();
		query.where(EmployeesTable.C_STATUS).eq("ACTIVE")
				.orderBy(EmployeesTable.C_LAST_NAME);

		ScenarioSupport.executeQuery(connection, query.render(), title());
	}
}
