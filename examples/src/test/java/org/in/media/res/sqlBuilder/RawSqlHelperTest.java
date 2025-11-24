package org.in.media.res.sqlBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.List;

import org.in.media.res.sqlBuilder.api.query.SqlAndParams;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.junit.jupiter.api.Test;

class RawSqlHelperTest {

	@Test
	void rawHelperBuildsSqlAndParams() {
		SqlAndParams sap = SqlQuery.raw("SELECT * FROM employees WHERE status = ? AND salary > ?", "ACTIVE", 80_000);

		assertEquals("SELECT * FROM employees WHERE status = ? AND salary > ?", sap.sql());
		assertIterableEquals(List.of("ACTIVE", 80_000), sap.params());
	}

	@Test
	void rawHelperSupportsNoParams() {
		SqlAndParams sap = SqlQuery.raw("SELECT 1");

		assertEquals("SELECT 1", sap.sql());
		assertIterableEquals(List.of(), sap.params());
	}
}
