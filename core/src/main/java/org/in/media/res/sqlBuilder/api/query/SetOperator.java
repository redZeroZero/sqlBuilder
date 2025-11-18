package org.in.media.res.sqlBuilder.api.query;

/**
 * Supported SQL set operators (UNION / INTERSECT / EXCEPT) along with
 * their standard SQL keywords. Dialects can remap specific entries to
 * dialect-specific tokens (e.g. Oracle uses MINUS for EXCEPT).
 */
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
