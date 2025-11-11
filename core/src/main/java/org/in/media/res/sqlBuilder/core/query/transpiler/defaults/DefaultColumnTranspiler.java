package org.in.media.res.sqlBuilder.core.query.transpiler.defaults;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.query.spi.ColumnTranspiler;
import org.in.media.res.sqlBuilder.core.query.dialect.DialectContext;

public class DefaultColumnTranspiler implements ColumnTranspiler {

	private final String AS_ = " as ";

	private final String TABLE_SEP_ = ".";

	public String transpile(boolean useAlias, Column c) {
		StringBuilder sb = new StringBuilder();

		String tableName = c.table().hasTableName() ? DialectContext.current().quoteIdent(c.table().tableName()) : null;
		String columnName = DialectContext.current().quoteIdent(c.getName());
		if (tableName != null) {
			sb.append(tableName).append(TABLE_SEP_).append(columnName);
		} else {
			sb.append(columnName);
		}

		if (useAlias && c.hasColumnAlias())
			sb.append(AS_).append(DialectContext.current().quoteIdent(c.getAlias()));

		return sb.toString();
	}

}
