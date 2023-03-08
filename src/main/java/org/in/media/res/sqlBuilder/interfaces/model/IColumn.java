package org.in.media.res.sqlBuilder.interfaces.model;

import org.in.media.res.sqlBuilder.interfaces.query.ITranspilable;

public interface IColumn extends ITranspilable {

	public String transpile(boolean useAlias);
	
	public ITable table();
	
	

}
