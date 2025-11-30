package org.in.media.res.sqlBuilder.api.query.window;

import java.util.Objects;

import org.in.media.res.sqlBuilder.constants.SortDirection;

public final class WindowOrdering {

	private final WindowExpression expression;
	private final SortDirection direction;

	private WindowOrdering(WindowExpression expression, SortDirection direction) {
		this.expression = Objects.requireNonNull(expression, "expression");
		this.direction = Objects.requireNonNull(direction, "direction");
	}

	public static WindowOrdering asc(WindowExpression expression) {
		return new WindowOrdering(expression, SortDirection.ASC);
	}

	public static WindowOrdering desc(WindowExpression expression) {
		return new WindowOrdering(expression, SortDirection.DESC);
	}

	public WindowExpression expression() {
		return expression;
	}

	public SortDirection direction() {
		return direction;
	}
}
