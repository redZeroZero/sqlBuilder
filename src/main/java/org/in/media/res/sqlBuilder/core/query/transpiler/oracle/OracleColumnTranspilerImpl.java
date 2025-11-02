package org.in.media.res.sqlBuilder.core.query.transpiler.oracle;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.query.ColumnTranspiler;

public class OracleColumnTranspilerImpl implements ColumnTranspiler {

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
