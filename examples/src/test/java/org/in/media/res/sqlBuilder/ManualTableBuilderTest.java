package org.in.media.res.sqlBuilder;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.Tables;
import org.in.media.res.sqlBuilder.api.query.SqlAndParams;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.api.model.ColumnRef;
import org.junit.jupiter.api.Test;

class ManualTableBuilderTest {

	@Test
	void manualTablesEnableTypedColumnsWithoutScanning() {
		ColumnRef<Integer> id = ColumnRef.of("ID", Integer.class);
		ColumnRef<String> firstName = ColumnRef.of("FIRST_NAME", String.class);

		Table employees = Tables.builder("Employee", "E")
				.column(id)
				.column(firstName)
				.column("LAST_NAME")
				.build();

		SqlAndParams sp = SqlQuery.query()
				.select(firstName)
				.from(employees)
				.where(id).eq(42)
				.render();

		assertTrue(sp.sql().contains("\"Employee\""));
		assertTrue(sp.sql().contains("\"E\".\"FIRST_NAME\""));
	}
}
