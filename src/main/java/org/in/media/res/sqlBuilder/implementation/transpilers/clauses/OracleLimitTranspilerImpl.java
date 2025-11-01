package org.in.media.res.sqlBuilder.implementation.transpilers.clauses;

import org.in.media.res.sqlBuilder.interfaces.query.ILimit;
import org.in.media.res.sqlBuilder.interfaces.query.ILimitTranspiler;

public class OracleLimitTranspilerImpl implements ILimitTranspiler {

 private static final String OFFSET_ = " OFFSET ";
 private static final String ROWS_ = " ROWS";
 private static final String FETCH_NEXT_ = " FETCH NEXT ";
 private static final String ROWS_ONLY = " ROWS ONLY";

 @Override
 public String transpile(ILimit limitClause) {
  StringBuilder sb = new StringBuilder();
  Integer offset = limitClause.offsetValue();
  Integer limit = limitClause.limitValue();

  if (offset != null) {
   sb.append(OFFSET_).append(offset).append(ROWS_);
  }

  if (limit != null) {
   sb.append(FETCH_NEXT_).append(limit).append(ROWS_ONLY);
  }

  return sb.toString();
 }

}
