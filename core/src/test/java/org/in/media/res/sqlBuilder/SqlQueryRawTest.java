package org.in.media.res.sqlBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.List;

import org.in.media.res.sqlBuilder.api.query.SqlAndParams;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.junit.jupiter.api.Test;

class SqlQueryRawTest {

	@Test
	void rawBuildsSqlAndParams() {
		SqlAndParams sap = SqlQuery.raw("SELECT * FROM foo WHERE a = ? AND b = ?", 1, "bar");

		assertEquals("SELECT * FROM foo WHERE a = ? AND b = ?", sap.sql());
		assertIterableEquals(List.of(1, "bar"), sap.params());
	}

	@Test
	void rawHandlesNullParamsArray() {
		SqlAndParams sap = SqlQuery.raw("SELECT 1");

		assertEquals("SELECT 1", sap.sql());
		assertIterableEquals(List.of(), sap.params());
	}
}
