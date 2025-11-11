package org.in.media.res.sqlBuilder.api.query.spi;

import org.in.media.res.sqlBuilder.api.query.Query;

public interface QueryTranspiler {
	
	public String transpile(Query	clause);
	
}
