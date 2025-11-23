package org.in.media.res.sqlBuilder.integration.boot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.in.media.res.sqlBuilder.integration.boot.query.QueryExecution;
import org.in.media.res.sqlBuilder.integration.boot.query.QueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@EnabledIfEnvironmentVariable(named = "SQLBUILDER_IT_ORACLE", matches = "true")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("oracle")
class QueryServiceOracleIntegrationTest {

	@Autowired
	private QueryService queryService;

	@Test
	void executesSimpleProjectionAgainstOracle() {
		QueryExecution execution = queryService.run("simple-projection");

		assertEquals("simple-projection", execution.id());
		assertFalse(execution.rows().isEmpty());
	}
}
