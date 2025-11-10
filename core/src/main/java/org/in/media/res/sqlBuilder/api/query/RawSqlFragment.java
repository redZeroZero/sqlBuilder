package org.in.media.res.sqlBuilder.api.query;

import java.util.List;

/**
 * Lightweight representation of a raw SQL snippet and its associated parameters.
 * The snippet is injected verbatim into the rendered statement.
 */
public interface RawSqlFragment {

	/**
	 * @return the raw SQL snippet. It is appended to the query without modification.
	 */
	String sql();

	/**
	 * @return parameters corresponding to {@code ?} placeholders inside {@link #sql()}.
	 *         They are merged into the compiled query placeholder list in order.
	 */
	List<SqlParameter<?>> parameters();
}
