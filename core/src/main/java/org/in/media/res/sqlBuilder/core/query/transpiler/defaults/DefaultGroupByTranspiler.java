package org.in.media.res.sqlBuilder.core.query.transpiler.defaults;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.query.GroupBy;
import org.in.media.res.sqlBuilder.api.query.GroupByTranspiler;

public class DefaultGroupByTranspiler implements GroupByTranspiler {

 private static final String GROUP_BY_ = " GROUP BY ";

 private static final String SEP_ = ", ";

 @Override
 public String transpile(GroupBy groupBy) {
  StringBuilder sb = new StringBuilder(GROUP_BY_);
  var columns = groupBy.groupByColumns();
  sb.append(columns.getFirst().transpile(false));
  for (Column column : columns.subList(1, columns.size())) {
   sb.append(SEP_).append(column.transpile(false));
  }
  return sb.toString();
 }

}
