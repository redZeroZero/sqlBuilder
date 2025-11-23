package org.in.media.res.sqlBuilder.integration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.in.media.res.sqlBuilder.integration.scenario.AggregationsScenario;
import org.in.media.res.sqlBuilder.integration.scenario.CountAndPrettyScenario;
import org.in.media.res.sqlBuilder.integration.scenario.CteScenario;
import org.in.media.res.sqlBuilder.integration.scenario.DeleteScenario;
import org.in.media.res.sqlBuilder.integration.scenario.DerivedTableScenario;
import org.in.media.res.sqlBuilder.integration.scenario.AboveDepartmentAverageScenario;
import org.in.media.res.sqlBuilder.integration.scenario.CustomerOrderSummaryScenario;
import org.in.media.res.sqlBuilder.integration.scenario.GroupedFiltersScenario;
import org.in.media.res.sqlBuilder.integration.scenario.InsertScenario;
import org.in.media.res.sqlBuilder.integration.scenario.IntegrationScenario;
import org.in.media.res.sqlBuilder.integration.scenario.JoinWithFiltersScenario;
import org.in.media.res.sqlBuilder.integration.scenario.OptimizerHintScenario;
import org.in.media.res.sqlBuilder.integration.scenario.OptionalFiltersScenario;
import org.in.media.res.sqlBuilder.integration.scenario.PaginationScenario;
import org.in.media.res.sqlBuilder.integration.scenario.RawSqlScenario;
import org.in.media.res.sqlBuilder.integration.scenario.SetOperationsScenario;
import org.in.media.res.sqlBuilder.integration.scenario.SimpleProjectionScenario;
import org.in.media.res.sqlBuilder.integration.scenario.SubqueryFilteringScenario;
import org.in.media.res.sqlBuilder.integration.scenario.UpdateScenario;
import org.in.media.res.sqlBuilder.integration.scenario.TopEarnersByDepartmentScenario;

public final class IntegrationApp {

	private static final List<IntegrationScenario> SCENARIOS = List.of(
			new SimpleProjectionScenario(),
			new JoinWithFiltersScenario(),
			new AggregationsScenario(),
			new PaginationScenario(),
			new CountAndPrettyScenario(),
			new OptimizerHintScenario(),
			new SetOperationsScenario(),
			new DerivedTableScenario(),
			new CteScenario(),
			new SubqueryFilteringScenario(),
			new OptionalFiltersScenario(),
			new GroupedFiltersScenario(),
			new RawSqlScenario(),
			new UpdateScenario(),
			new InsertScenario(),
			new DeleteScenario(),
			new TopEarnersByDepartmentScenario(),
			new CustomerOrderSummaryScenario(),
			new AboveDepartmentAverageScenario());

	public static void main(String[] args) throws Exception {
		IntegrationConfig config = IntegrationConfig.fromEnvironment();
		System.out.printf("Connecting to %s as %s%n", config.jdbcUrl(), config.user());
		try (Connection connection = config.connect()) {
			describeServer(connection);
			for (IntegrationScenario scenario : SCENARIOS) {
				scenario.run(connection);
			}
		}
	}

	private static void describeServer(Connection connection) throws SQLException {
		var meta = connection.getMetaData();
		System.out.printf("Database: %s %s%n", meta.getDatabaseProductName(), meta.getDatabaseProductVersion());
	}

	private IntegrationApp() {
	}
}
