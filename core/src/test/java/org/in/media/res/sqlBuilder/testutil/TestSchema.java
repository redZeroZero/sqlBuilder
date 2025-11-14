package org.in.media.res.sqlBuilder.testutil;

import java.util.Date;

import org.in.media.res.sqlBuilder.api.model.ColumnRef;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.Tables;

/**
 * Shared tables/columns used across query-related unit tests.
 */
public final class TestSchema {

	private TestSchema() {
	}

	public static final ColumnRef<Integer> EMP_ID = ColumnRef.of("EMP_ID", Integer.class);
	public static final ColumnRef<String> EMP_NAME = ColumnRef.of("EMP_NAME", String.class);
	public static final ColumnRef<Double> EMP_SALARY = ColumnRef.of("EMP_SALARY", Double.class);
	public static final ColumnRef<Integer> EMP_DEPT_ID = ColumnRef.of("EMP_DEPT_ID", Integer.class);
	public static final ColumnRef<Date> EMP_HIRED_AT = ColumnRef.of("EMP_HIRED_AT", Date.class);

	public static final Table EMPLOYEES = Tables.builder("EMPLOYEES", "E")
			.column(EMP_ID)
			.column(EMP_NAME)
			.column(EMP_SALARY)
			.column(EMP_DEPT_ID)
			.column(EMP_HIRED_AT)
			.build();

	public static final ColumnRef<Integer> DEPT_ID = ColumnRef.of("DEPT_ID", Integer.class);
	public static final ColumnRef<String> DEPT_NAME = ColumnRef.of("DEPT_NAME", String.class);

	public static final Table DEPARTMENTS = Tables.builder("DEPARTMENTS", "D")
			.column(DEPT_ID)
			.column(DEPT_NAME)
			.build();
}
