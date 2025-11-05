package org.in.media.res.sqlBuilder.core.query.transpiler.oracle;

import static org.in.media.res.sqlBuilder.constants.Operator.EQ;
import static org.in.media.res.sqlBuilder.constants.Operator.IN;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.query.Condition;
import org.in.media.res.sqlBuilder.api.query.ConditionTranspiler;
import org.in.media.res.sqlBuilder.api.query.ConditionValue;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.Operator;
import org.in.media.res.sqlBuilder.core.query.util.SqlEscapers;

public class OracleConditionTranspilerImpl implements ConditionTranspiler {

	private final String OPENING_PARENTHESIS = "(";

	private final String CLOSING_PARENTHESIS = ")";

	private final String POUIC = "'";

	private final String SEP_ = ", ";

	public String transpile(Condition co) {
		if (isGroupedCondition(co)) {
			return renderGroupedCondition(co);
		}
		final StringBuilder sb = new StringBuilder();
		Operator resolvedOperator = resolveOperator(co);
		if (co.getStartOperator() != null)
			sb.append(co.getStartOperator().value());
		Column left = co.getLeft();
		String operatorSql = resolvedOperator.value();
		if (left != null) {
			transpileColumn(sb, left, co.getLeftAgg());
			sb.append(operatorSql);
			if (co.getRight() != null)
				transpileColumn(sb, co.getRight(), co.getRightAgg());
			else
				transpileValues(sb, co);
		} else {
			sb.append(operatorSql.stripLeading());
			transpileValues(sb, co);
		}
		return sb.toString();
	}

	private Operator resolveOperator(Condition co) {
		if (co.getOperator() == Operator.EXISTS || co.getOperator() == Operator.NOT_EXISTS) {
			return co.getOperator();
		}
		if (co.values().size() > 1) {
			if (EQ.equals(co.getOperator())) {
				return IN;
			}
			if (Operator.NOT_EQ.equals(co.getOperator())) {
				return Operator.NOT_IN;
			}
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
		if (co.values().isEmpty()) {
			return;
		}
		if (Operator.BETWEEN.equals(co.getOperator())) {
			if (co.values().size() != 2) {
				throw new IllegalStateException("BETWEEN operator requires exactly two values");
			}
			appendLiteral(sb, co.values().get(0));
			sb.append(" AND ");
			appendLiteral(sb, co.values().get(1));
			return;
		}
		if (co.values().size() == 1) {
			if (Operator.EXISTS.equals(co.getOperator()) || Operator.NOT_EXISTS.equals(co.getOperator())) {
				appendSubquery(sb, co.values().get(0));
				return;
			}
			appendLiteral(sb, co.values().get(0));
			if (isLikeOperator(co.getOperator())) {
				sb.append(" ESCAPE '").append(SqlEscapers.likeEscapeChar()).append("'");
			}
		} else {
			sb.append(OPENING_PARENTHESIS);
			for (int i = 0; i < co.values().size(); i++)
				switchOnOpType(sb, i, isLast(co, i), co);
			sb.append(CLOSING_PARENTHESIS);
		}
	}

	private void switchOnOpType(StringBuilder sb, int index, boolean isLast, Condition co) {
		appendLiteral(sb, co.values().get(index));
		if (!isLast) {
			sb.append(SEP_);
		}
	}

	private boolean isLast(Condition co, int i) {
		return co.values().size() - 1 == i;
	}

	private void appendLiteral(StringBuilder sb, ConditionValue value) {
		switch (value.type()) {
		case TY_DATE:
		case TY_STR:
			sb.append(POUIC).append(SqlEscapers.escapeStringLiteral(String.valueOf(value.value()))).append(POUIC);
			break;
		case TY_DBL:
		case TY_INT:
			sb.append(value.value());
			break;
		case TY_SUBQUERY:
			appendSubquery(sb, value);
			break;
		default:
			sb.append(POUIC).append(value.value()).append(POUIC);
			break;
		}
	}

	private void appendSubquery(StringBuilder sb, ConditionValue value) {
		Query query = (Query) value.value();
		sb.append(OPENING_PARENTHESIS).append(query.transpile()).append(CLOSING_PARENTHESIS);
	}

	private boolean isLikeOperator(Operator operator) {
		return Operator.LIKE.equals(operator) || Operator.NOT_LIKE.equals(operator);
	}

	private boolean isGroupedCondition(Condition condition) {
		return condition.getLeft() == null
				&& condition.getOperator() == null
				&& condition.values().isEmpty();
	}

	private String renderGroupedCondition(Condition condition) {
		StringBuilder sb = new StringBuilder();
		if (condition.getStartOperator() != null) {
			sb.append(condition.getStartOperator().value());
		}
		sb.append(condition.transpile());
		return sb.toString();
	}
}
