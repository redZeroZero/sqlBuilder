package org.in.media.res.sqlBuilder.constants;

public enum Operator {

	AND(" AND "),
	OR(" OR "),
	EQ(" = "),
	NOT_EQ(" <> "),
	MORE(" > "),
	LESS(" < "),
	MORE_OR_EQ(" >= "),
	LESS_OR_EQ(" <= "),
	IN(" IN "),
	NOT_IN(" NOT IN "),
	LIKE(" LIKE "),
	NOT_LIKE(" NOT LIKE "),
	BETWEEN(" BETWEEN "),
	IS_NULL(" IS NULL"),
	IS_NOT_NULL(" IS NOT NULL"),
	EXISTS(" EXISTS "),
	NOT_EXISTS(" NOT EXISTS ");
 
	private String value;
	
	private Operator(String operator) {
		this.value = operator;
	}

	public String value() {
		return value;
	}

}
