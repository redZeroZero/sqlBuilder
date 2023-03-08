package org.in.media.res.sqlBuilder.interfaces.query;

public interface ITranspiler {

	public String transpile(IClause	clause);
	
	public String transpile(IQuery	clause);

}
