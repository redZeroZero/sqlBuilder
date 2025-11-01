package org.in.media.res.sqlBuilder.interfaces.query;

public interface ILimit extends IClause, IResetable, ITranspilable {

	ILimit limit(int limit);

	ILimit offset(int offset);

	ILimit limitAndOffset(int limit, int offset);

	Integer limitValue();

	Integer offsetValue();
}
