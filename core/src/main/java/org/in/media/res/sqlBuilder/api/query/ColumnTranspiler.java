package org.in.media.res.sqlBuilder.api.query;

import org.in.media.res.sqlBuilder.api.model.Column;

public interface ColumnTranspiler {

	public String transpile(boolean useAlias, Column c);

}
