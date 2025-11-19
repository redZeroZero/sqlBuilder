package org.in.media.res.sqlBuilder.integration.scenario;

import java.sql.Connection;
import java.sql.SQLException;

public interface IntegrationScenario {

	String title();

	void run(Connection connection) throws SQLException;
}
