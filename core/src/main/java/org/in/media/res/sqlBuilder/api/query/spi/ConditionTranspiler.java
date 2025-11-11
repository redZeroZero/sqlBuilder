package org.in.media.res.sqlBuilder.api.query.spi;

import org.in.media.res.sqlBuilder.api.query.Condition;

public interface ConditionTranspiler {

	public String transpile(Condition co);
	
}
