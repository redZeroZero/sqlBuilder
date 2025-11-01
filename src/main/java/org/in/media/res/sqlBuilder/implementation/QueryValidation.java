package org.in.media.res.sqlBuilder.implementation;

import java.util.Objects;

import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.model.ITable;

final class QueryValidation {

    private QueryValidation() {
    }

	static ITable requireTable(IColumn column, String message) {
		ITable table = Objects.requireNonNull(column, message).table();
		if (table == null) {
			throw new IllegalStateException(message + " (no owning table)");
		}
		return table;
	}

}
