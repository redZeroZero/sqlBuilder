package org.in.media.res.sqlBuilder.implementation.transpilers.clauses;

import org.in.media.res.sqlBuilder.interfaces.query.ICondition;
import org.in.media.res.sqlBuilder.interfaces.query.IHaving;
import org.in.media.res.sqlBuilder.interfaces.query.IHavingTranspiler;

public class OracleHavingTranspilerImpl implements IHavingTranspiler {

 private static final String HAVING_ = " HAVING ";

 @Override
 public String transpile(IHaving having) {
  StringBuilder sb = new StringBuilder(HAVING_);
  for (ICondition condition : having.havingConditions()) {
   sb.append(condition.transpile());
  }
  return sb.toString();
 }

}
