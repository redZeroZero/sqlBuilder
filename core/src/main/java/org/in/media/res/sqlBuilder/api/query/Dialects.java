package org.in.media.res.sqlBuilder.api.query;

/**
 * Public facade exposing the built-in dialect instances.
 */
public final class Dialects {

	private Dialects() {
	}

	public static Dialect defaultDialect() {
		return org.in.media.res.sqlBuilder.core.query.dialect.Dialects.defaultDialect();
	}

	public static Dialect oracle() {
		return org.in.media.res.sqlBuilder.core.query.dialect.Dialects.oracle();
	}

	public static Dialect postgres() {
		return org.in.media.res.sqlBuilder.core.query.dialect.Dialects.postgres();
	}
}
