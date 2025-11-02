package org.in.media.res.sqlBuilder.core.query.transpiler.oracle;

import static org.in.media.res.sqlBuilder.constants.Operator.EQ;
import static org.in.media.res.sqlBuilder.constants.Operator.IN;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.Operator;
import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.query.Condition;
import org.in.media.res.sqlBuilder.api.query.ConditionTranspiler;

public class OracleConditionTranspilerImpl implements ConditionTranspiler {

	private final String OPENING_PARENTHESIS = "(";

	private final String CLOSING_PARENTHESIS = ")";

	private final String POUIC = "'";

	private final String SEP_ = ", ";

	public String transpile(Condition co) {
		final StringBuilder sb = new StringBuilder();
		Operator resolvedOperator = resolveOperator(co);
		if (co.getStartOperator() != null)
			sb.append(co.getStartOperator().value());
		transpileColumn(sb, co.getLeft(), co.getLeftAgg());
		sb.append(resolvedOperator.value());
		if (co.getRight() != null)
			transpileColumn(sb, co.getRight(), co.getRightAgg());
		else
			transpileValues(sb, co);
		return sb.toString();
	}

	private Operator resolveOperator(Condition co) {
		if (co.values().size() > 1 && EQ.equals(co.getOperator())) {
			return IN;
		}
		return co.getOperator();
	}

	private void transpileColumn(StringBuilder sb, Column col, AggregateOperator agg) {
		if (agg != null)
			sb.append(agg.value()).append(OPENING_PARENTHESIS);
		sb.append(col.transpile(false));
		if (agg != null)
			sb.append(CLOSING_PARENTHESIS);
	}

	private void transpileValues(StringBuilder sb, Condition co) {
		if (co.values().size() == 1) {
			switchOnOpType(sb, 0, true, co);
		} else {
			sb.append(OPENING_PARENTHESIS);
			for (int i = 0; i < co.values().size(); i++)
				switchOnOpType(sb, i, isLast(co, i), co);
			sb.append(CLOSING_PARENTHESIS);
		}
	}

	private void switchOnOpType(StringBuilder sb, int index, boolean isLast, Condition co) {
		switch (co.values().get(index).type()) {
		case TY_DATE:
		case TY_STR:
			this.builStringItem(sb, index, isLast, co);
			break;
		case TY_DBL:
		case TY_INT:
			this.builIntItem(sb, index, isLast, co);
			break;
		default:
			this.builStringItem(sb, index, isLast, co);
			break;
		}
	}

	private boolean isLast(Condition co, int i) {
		return co.values().size() - 1 == i;
	}

	private void builStringItem(StringBuilder sb, int index, boolean last, Condition co) {
		sb.append(POUIC).append(co.values().get(index).value()).append(POUIC);
		if (!last)
			sb.append(SEP_);
	}

	private void builIntItem(StringBuilder sb, int index, boolean last, Condition co) {
		sb.append(co.values().get(index).value());
		if (!last)
			sb.append(SEP_);
	}
}
