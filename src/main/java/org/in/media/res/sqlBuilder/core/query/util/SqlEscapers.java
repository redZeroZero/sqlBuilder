package org.in.media.res.sqlBuilder.core.query.util;

/**
 * Common SQL escaping helpers. Keeps literal / LIKE escaping consistent across
 * clauses and transpilers.
 */
public final class SqlEscapers {

	private static final char ESCAPE_CHAR = '\\';

	private SqlEscapers() {
	}

	public static String escapeStringLiteral(String value) {
		if (value == null) {
			return null;
		}
		return value.replace("'", "''");
	}

	public static String escapeLikePattern(String value) {
		if (value == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder(value.length());
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c == ESCAPE_CHAR || c == '%' || c == '_') {
				sb.append(ESCAPE_CHAR);
			}
			sb.append(c);
		}
		return sb.toString();
	}

	public static char likeEscapeChar() {
		return ESCAPE_CHAR;
	}
}
