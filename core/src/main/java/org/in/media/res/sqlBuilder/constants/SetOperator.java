package org.in.media.res.sqlBuilder.constants;

public enum SetOperator {
	UNION("UNION"),
	UNION_ALL("UNION ALL"),
	INTERSECT("INTERSECT"),
	INTERSECT_ALL("INTERSECT ALL"),
	EXCEPT("EXCEPT"),
	EXCEPT_ALL("EXCEPT ALL");

	private final String sql;

	SetOperator(String sql) {
		this.sql = sql;
	}

	public String sql() {
		return sql;
	}
}
