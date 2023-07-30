package org.in.media.res.sqlBuilder.implementation.transpilers.clauses;

import org.in.media.res.sqlBuilder.interfaces.query.ICondition;
import org.in.media.res.sqlBuilder.interfaces.query.IWhere;
import org.in.media.res.sqlBuilder.interfaces.query.IWhereTranspiler;

public class OracleWhereTranspilerImpl implements IWhereTranspiler {

	private String WHERE_ = " WHERE ";

	@Override
	public String transpile(IWhere w) {
		StringBuilder sb = new StringBuilder();
		sb.append(WHERE_);
		if (!w.conditions().isEmpty()) {
			for (ICondition c : w.conditions())
				sb.append(c.transpile());
		}
		return sb.toString();
	}

}
