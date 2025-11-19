package org.in.media.res.sqlBuilder.integration.scenario;

import java.sql.Connection;
import java.sql.SQLException;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.integration.model.CustomersTable;
import org.in.media.res.sqlBuilder.integration.model.EmployeesTable;
import org.in.media.res.sqlBuilder.integration.model.IntegrationSchema;

public final class SetOperationsScenario implements IntegrationScenario {

	@Override
	public String title() {
		return "6. Set Operations";
	}

	@Override
	public void run(Connection connection) throws SQLException {
		Table employees = IntegrationSchema.employees();
		Table customers = IntegrationSchema.customers();

		Query customerNames = SqlQuery.newQuery()
				.select(CustomersTable.C_FIRST_NAME)
				.select(CustomersTable.C_LAST_NAME)
				.from(customers)
				.asQuery();

		Query union = SqlQuery.newQuery()
				.select(EmployeesTable.C_FIRST_NAME)
				.select(EmployeesTable.C_LAST_NAME)
				.from(employees)
				.asQuery()
				.union(customerNames);

		ScenarioSupport.executeQuery(connection, union.render(), title());
	}
}
