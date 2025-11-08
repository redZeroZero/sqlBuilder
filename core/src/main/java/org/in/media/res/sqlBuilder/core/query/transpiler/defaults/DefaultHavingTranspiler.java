package org.in.media.res.sqlBuilder.core.query.transpiler.defaults;

import org.in.media.res.sqlBuilder.api.query.Condition;
import org.in.media.res.sqlBuilder.api.query.Having;
import org.in.media.res.sqlBuilder.api.query.HavingTranspiler;

public class DefaultHavingTranspiler implements HavingTranspiler {

 private static final String HAVING_ = " HAVING ";

 @Override
 public String transpile(Having having) {
  StringBuilder sb = new StringBuilder(HAVING_);
  for (Condition condition : having.havingConditions()) {
   sb.append(condition.transpile());
  }
  return sb.toString();
 }

}
