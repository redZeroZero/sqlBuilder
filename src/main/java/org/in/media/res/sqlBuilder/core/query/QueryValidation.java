package org.in.media.res.sqlBuilder.core.query;

import java.util.Objects;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.Query;

final class QueryValidation {

	private QueryValidation() {
	}

	static Table requireTable(Column column, String message) {
		Table table = Objects.requireNonNull(column, message).table();
		if (table == null) {
			throw new IllegalStateException(message + " (no owning table)");
		}
		return table;
	}

	static void requireScalarSubquery(Query query, String message) {
		int projection = projectionSize(query);
		if (projection == 0) {
			throw new IllegalArgumentException(message + " (subquery selects no columns)");
		}
		if (projection != 1) {
			throw new IllegalArgumentException(message + " (expected 1 column, got " + projection + ")");
		}
	}

	static void requireAnyProjection(Query query, String message) {
		if (projectionSize(query) == 0) {
			throw new IllegalArgumentException(message + " (subquery selects no columns)");
		}
	}

	private static int projectionSize(Query query) {
		return query.columns().size() + query.aggColumns().size();
	}

}
