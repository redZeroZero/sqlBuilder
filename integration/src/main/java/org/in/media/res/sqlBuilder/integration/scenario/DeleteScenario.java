package org.in.media.res.sqlBuilder.integration.scenario;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.CompiledQuery;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.api.query.SqlParameters;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.integration.model.IntegrationSchema;
import org.in.media.res.sqlBuilder.integration.model.PaymentsTable;

public final class DeleteScenario implements IntegrationScenario {

	@Override
	public String title() {
		return "15. Deletes (DML)";
	}

	@Override
	public void run(Connection connection) throws SQLException {
		boolean autoCommit = connection.getAutoCommit();
		connection.setAutoCommit(false);
		try {
			executeDelete(connection);
			connection.rollback();
		} finally {
			connection.setAutoCommit(autoCommit);
		}
	}

	private void executeDelete(Connection connection) throws SQLException {
		Table payments = IntegrationSchema.payments();
		SqlParameter<String> method = SqlParameters.param("method");

		CompiledQuery compiled = SqlQuery.deleteFrom(payments)
				.whereOptionalEquals(PaymentsTable.C_METHOD, method)
				.compile();

		var params = Map.<String, Object>of(method.name(), "WIRE");
		ScenarioSupport.executeUpdate(connection, compiled.bind(params), title());
	}
}
