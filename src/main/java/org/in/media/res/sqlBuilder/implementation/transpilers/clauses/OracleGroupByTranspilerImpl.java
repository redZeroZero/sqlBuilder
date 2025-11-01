package org.in.media.res.sqlBuilder.implementation.transpilers.clauses;

import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.query.IGroupBy;
import org.in.media.res.sqlBuilder.interfaces.query.IGroupByTranspiler;

public class OracleGroupByTranspilerImpl implements IGroupByTranspiler {

 private static final String GROUP_BY_ = " GROUP BY ";

 private static final String SEP_ = ", ";

 @Override
 public String transpile(IGroupBy groupBy) {
  StringBuilder sb = new StringBuilder(GROUP_BY_);
  var columns = groupBy.groupByColumns();
  sb.append(columns.getFirst().transpile(false));
  for (IColumn column : columns.subList(1, columns.size())) {
   sb.append(SEP_).append(column.transpile(false));
  }
  return sb.toString();
 }

}
