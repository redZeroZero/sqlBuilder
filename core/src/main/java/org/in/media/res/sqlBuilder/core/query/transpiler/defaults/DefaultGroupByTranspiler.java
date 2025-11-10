package org.in.media.res.sqlBuilder.core.query.transpiler.defaults;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.query.GroupBy;
import java.util.ArrayList;
import java.util.List;

import org.in.media.res.sqlBuilder.api.query.GroupByTranspiler;
import org.in.media.res.sqlBuilder.core.query.GroupByRawSupport;
import org.in.media.res.sqlBuilder.api.query.RawSqlFragment;

public class DefaultGroupByTranspiler implements GroupByTranspiler {

 private static final String GROUP_BY_ = " GROUP BY ";

 private static final String SEP_ = ", ";

 @Override
 public String transpile(GroupBy groupBy) {
  List<String> segments = new ArrayList<>();
  for (Column column : groupBy.groupByColumns()) {
   segments.add(column.transpile(false));
  }
	if (groupBy instanceof GroupByRawSupport support) {
	 for (RawSqlFragment fragment : support.groupByFragments()) {
	 segments.add(fragment.sql());
	 }
	}
  if (segments.isEmpty()) {
   return "";
  }
  StringBuilder sb = new StringBuilder(GROUP_BY_);
  sb.append(segments.getFirst());
  for (int i = 1; i < segments.size(); i++) {
   sb.append(SEP_).append(segments.get(i));
  }
  return sb.toString();
 }

}
