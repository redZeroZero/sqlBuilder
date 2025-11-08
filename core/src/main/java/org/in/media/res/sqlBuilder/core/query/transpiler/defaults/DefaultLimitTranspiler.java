package org.in.media.res.sqlBuilder.core.query.transpiler.defaults;

import org.in.media.res.sqlBuilder.api.query.Limit;
import org.in.media.res.sqlBuilder.api.query.LimitTranspiler;

public class DefaultLimitTranspiler implements LimitTranspiler {

 private static final String OFFSET_ = " OFFSET ";
 private static final String ROWS_ = " ROWS";
 private static final String FETCH_NEXT_ = " FETCH NEXT ";
 private static final String ROWS_ONLY = " ROWS ONLY";

 @Override
 public String transpile(Limit limitClause) {
  StringBuilder sb = new StringBuilder();
  Integer offset = limitClause.offsetValue();
  Integer limit = limitClause.limitValue();

	if (offset != null) {
		sb.append(OFFSET_).append('?').append(ROWS_);
	}

	if (limit != null) {
		sb.append(FETCH_NEXT_).append('?').append(ROWS_ONLY);
	}

  return sb.toString();
 }

}
