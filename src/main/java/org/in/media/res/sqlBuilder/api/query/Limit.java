package org.in.media.res.sqlBuilder.api.query;

public interface Limit extends Clause, Resetable, Transpilable {

	Limit limit(int limit);

	Limit offset(int offset);

	Limit limitAndOffset(int limit, int offset);

	Integer limitValue();

	Integer offsetValue();
}
