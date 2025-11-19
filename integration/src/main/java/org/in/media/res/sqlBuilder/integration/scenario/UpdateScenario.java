package org.in.media.res.sqlBuilder.integration.scenario;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.CompiledQuery;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.api.query.SqlParameters;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.integration.model.EmployeesTable;
import org.in.media.res.sqlBuilder.integration.model.IntegrationSchema;

public final class UpdateScenario implements IntegrationScenario {

	@Override
	public String title() {
		return "13. Updates (DML)";
	}

	@Override
	public void run(Connection connection) throws SQLException {
		boolean autoCommit = connection.getAutoCommit();
		connection.setAutoCommit(false);
		try {
			executeUpdateScenario(connection);
			connection.rollback();
		} finally {
			connection.setAutoCommit(autoCommit);
		}
	}

	private void executeUpdateScenario(Connection connection) throws SQLException {
		Table employees = IntegrationSchema.employees();
		SqlParameter<String> status = SqlParameters.param("status");
		SqlParameter<Integer> bonus = SqlParameters.param("bonus");
		SqlParameter<Long> employeeId = SqlParameters.param("employeeId");

		CompiledQuery compiled = SqlQuery.update(employees)
				.set(EmployeesTable.C_STATUS, status)
				.set(EmployeesTable.C_BONUS, bonus)
				.where(EmployeesTable.C_ID).eq(employeeId)
				.compile();

		Map<String, Object> params = new HashMap<>();
		params.put(status.name(), "ACTIVE");
		params.put(bonus.name(), 20_000);
		params.put(employeeId.name(), 5L);

		ScenarioSupport.executeUpdate(connection, compiled.bind(params), title());
	}
}
