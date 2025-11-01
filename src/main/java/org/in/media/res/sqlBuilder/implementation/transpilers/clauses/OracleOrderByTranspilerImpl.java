package org.in.media.res.sqlBuilder.implementation.transpilers.clauses;

import org.in.media.res.sqlBuilder.interfaces.query.IOrderBy;
import org.in.media.res.sqlBuilder.interfaces.query.IOrderBy.Ordering;
import org.in.media.res.sqlBuilder.interfaces.query.IOrderByTranspiler;

public class OracleOrderByTranspilerImpl implements IOrderByTranspiler {

 private static final String ORDER_BY_ = " ORDER BY ";

 private static final String SEP_ = ", ";

 @Override
 public String transpile(IOrderBy orderBy) {
  StringBuilder sb = new StringBuilder(ORDER_BY_);
  var iterator = orderBy.orderings().iterator();
  while (iterator.hasNext()) {
   Ordering ordering = iterator.next();
   sb.append(ordering.column().transpile(false)).append(' ').append(ordering.direction().value());
   if (iterator.hasNext()) {
    sb.append(SEP_);
   }
  }
  return sb.toString();
 }

}
