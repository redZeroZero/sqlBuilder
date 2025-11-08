package org.in.media.res.sqlBuilder.core.query.util;

/**
 * Common SQL escaping helpers. Keeps literal / LIKE escaping consistent across
 * clauses and transpilers.
 */
public final class SqlEscapers {

	private SqlEscapers() {
	}

	public static String escapeStringLiteral(String value) {
		if (value == null) {
			return null;
		}
		return value.replace("'", "''");
	}

	public static String escapeLikePattern(String value) {
		return escapeLikePattern(value, '\\');
	}

	public static String escapeLikePattern(String value, char escapeChar) {
		if (value == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder(value.length());
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c == escapeChar || c == '%' || c == '_') {
				sb.append(escapeChar);
			}
			sb.append(c);
		}
		return sb.toString();
	}
}
