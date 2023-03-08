package org.in.media.res.sqlBuilder.constants;

public enum Operator {

	AND(" AND "), OR(" OR "), EQ(" = "), MORE(" > "), LESS(" < "), MORE_OR_EQ(" >= "), LESS_OR_EQ(" <= "), IN(" IN ");
 
	private String value;
	
	private Operator(String operator) {
		this.value = operator;
	}

	public String value() {
		return value;
	}

}
