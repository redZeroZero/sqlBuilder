package org.in.media.res.sqlBuilder.interfaces.query;

import org.in.media.res.sqlBuilder.implementation.Column;

public interface IColumnTranspiler {

	public String transpile(boolean useAlias, Column c);

}
