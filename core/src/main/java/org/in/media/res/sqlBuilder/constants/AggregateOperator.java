package org.in.media.res.sqlBuilder.constants;

public enum AggregateOperator {
	
	COUNT("count"), MIN("min"), MAX("max"), SUM("sum"), AVG("avg"), STAR("*");

	private final String logicalName;

	private AggregateOperator(String logicalName) {
		this.logicalName = logicalName;
	}

	public String logicalName() {
		return logicalName;
	}
}
