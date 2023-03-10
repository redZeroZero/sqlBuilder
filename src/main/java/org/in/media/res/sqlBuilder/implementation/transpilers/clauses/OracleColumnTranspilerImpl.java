package org.in.media.res.sqlBuilder.implementation.transpilers.clauses;

import org.in.media.res.sqlBuilder.implementation.Column;
import org.in.media.res.sqlBuilder.interfaces.query.IColumnTranspiler;

public class OracleColumnTranspilerImpl implements IColumnTranspiler {

	private final String AS_ = " as ";

	private final String TABLE_SEP_ = ".";

	public String transpile(boolean useAlias, Column c) {
		StringBuilder sb = new StringBuilder();

		if (c.table().hasTableName())
			sb.append(c.table().tableName()).append(TABLE_SEP_).append(c.getName());
		else
			sb.append(c.getName());

		if (useAlias && c.hasColumnAlias())
			sb.append(AS_).append(c.getAlias());

		return sb.toString();
	}

}
