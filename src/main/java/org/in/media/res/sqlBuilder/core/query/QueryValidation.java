package org.in.media.res.sqlBuilder.core.query;

import java.util.Objects;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;

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

}
