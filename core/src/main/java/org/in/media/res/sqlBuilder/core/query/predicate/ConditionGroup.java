package org.in.media.res.sqlBuilder.core.query.predicate;

import java.util.List;
import java.util.Objects;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.query.Condition;
import org.in.media.res.sqlBuilder.api.query.ConditionValue;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.Operator;

/**
 * Composite condition representing a parenthesised group of child predicates.
 */
public final class ConditionGroup implements Condition {

	private final List<Condition> children;
	private final Operator startOperator;
	private final boolean forceParentheses;

	public ConditionGroup(List<Condition> children, Operator startOperator, boolean forceParentheses) {
		if (children == null || children.isEmpty()) {
			throw new IllegalArgumentException("Condition group must contain at least one predicate");
		}
		this.children = List.copyOf(children);
		this.startOperator = startOperator;
		this.forceParentheses = forceParentheses;
	}

	public List<Condition> children() {
		return children;
	}

	public Operator startOperator() {
		return startOperator;
	}

	public boolean forceParentheses() {
		return forceParentheses;
	}

	public ConditionGroup withStartOperator(Operator operator) {
		if (Objects.equals(operator, this.startOperator)) {
			return this;
		}
		return new ConditionGroup(children, operator, forceParentheses);
	}

	@Override
	public List<ConditionValue> values() {
		return List.of();
	}

	@Override
	public Column getLeft() {
		return null;
	}

	@Override
	public Column getRight() {
		return null;
	}

	@Override
	public Operator getStartOperator() {
		return startOperator;
	}

	@Override
	public Operator getOperator() {
		return null;
	}

	@Override
	public AggregateOperator getLeftAgg() {
		return null;
	}

	@Override
	public AggregateOperator getRightAgg() {
		return null;
	}

	@Override
	public String transpile() {
		StringBuilder builder = new StringBuilder();
		if (startOperator != null) {
			builder.append(startOperator.value());
		}
		boolean renderParentheses = forceParentheses || children.size() > 1;
		if (renderParentheses) {
			builder.append('(');
		}
		boolean first = true;
		for (Condition condition : children) {
			String rendered = condition.transpile();
			if (!first && rendered.length() > 0 && !Character.isWhitespace(rendered.charAt(0))) {
				builder.append(' ');
			}
			builder.append(rendered);
			first = false;
		}
		if (renderParentheses) {
			builder.append(')');
		}
		return builder.toString();
	}

	@Override
	public String toString() {
		return "ConditionGroup" + children;
	}
}
