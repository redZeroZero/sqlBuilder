package org.in.media.res.sqlBuilder.implementation.transpilers.clauses;

import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.query.ISelect;
import org.in.media.res.sqlBuilder.interfaces.query.ISelectTranspiler;

public class OracleSelectTranspilerImpl implements ISelectTranspiler {

	private String SEP_ = ", ";

	private String EMPTY_STR = "";

	private String SELECT_ = "SELECT ";

	private String OP_PARENTHESIS = "(";

	private String CL_PARENTHESIS = ")";

	@Override
	public String transpile(ISelect c) {
		StringBuilder sb = new StringBuilder();
		sb.append(SELECT_);

		if (!c.aggColumns().isEmpty())
			transpileAggregates(c, sb);
		transpileColumns(c, sb);

		return sb.toString();
	}

	private void transpileAggregates(ISelect c, StringBuilder sb) {
		IColumn[] arr = c.aggColumns().keySet().toArray(new IColumn[0]);
		for (IColumn col : arr) {
			sb.append(c.aggColumns().get(col).value()).append(OP_PARENTHESIS).append(col.transpile(false))
					.append(CL_PARENTHESIS);
			if (col.equals(arr[arr.length - 1]) && c.columns().isEmpty())
				sb.append(EMPTY_STR);
			else
				sb.append(SEP_);
		}
	}

	private void transpileColumns(ISelect c, StringBuilder sb) {
		for (int i = 0; i < c.columns().size(); i++) {
			if ((c.columns().size() - 1 == i))
				sb.append(c.columns().get(i).transpile());
			else
				sb.append(c.columns().get(i).transpile()).append(SEP_);
		}
	}

}
