package org.in.media.res.sqlBuilder.constants;

public enum AggregateOperator {
	
	COUNT("COUNT"), MIN("MIN"), MAX("MAX"), SUM("SUM"), AVG("AVG"), STAR("*");

	private String value;

	private AggregateOperator(String actualJoin) {
		this.value = actualJoin;
	}

	public String value() {
		return value;
	}
}
