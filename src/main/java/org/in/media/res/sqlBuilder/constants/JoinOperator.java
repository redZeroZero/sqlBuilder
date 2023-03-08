package org.in.media.res.sqlBuilder.constants;

public enum JoinOperator {

	JOIN(" JOIN "), LEFT_JOIN(" LEFT JOIN "), INNER_JOIN(" INNER JOIN "), RIGHT_JOIN(" RIGHT JOIN "), ON(" ON ");

	private String value;

	JoinOperator(String operator) {
		this.value = operator;
	}

	public String value() {
		return value;
	}
}
