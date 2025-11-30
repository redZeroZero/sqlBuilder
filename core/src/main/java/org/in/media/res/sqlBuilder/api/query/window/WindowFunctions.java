package org.in.media.res.sqlBuilder.api.query.window;

import java.util.List;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;

/**
 * Factory helpers for common window functions.
 */
public final class WindowFunctions {

	private WindowFunctions() {
	}

	public static WindowFunction rowNumber() {
		return WindowFunction.of("row_number");
	}

	public static WindowFunction rank() {
		return WindowFunction.of("rank");
	}

	public static WindowFunction denseRank() {
		return WindowFunction.of("dense_rank");
	}

	public static WindowFunction percentRank() {
		return WindowFunction.of("percent_rank");
	}

	public static WindowFunction cumeDist() {
		return WindowFunction.of("cume_dist");
	}

	public static WindowFunction ntile(int buckets) {
		if (buckets <= 0) {
			throw new IllegalArgumentException("buckets must be positive");
		}
		return WindowFunction.of("ntile", WindowExpression.raw(Integer.toString(buckets)));
	}

	public static WindowFunction sum(Column column) {
		return WindowFunction.aggregate(AggregateOperator.SUM, column);
	}

	public static WindowFunction avg(Column column) {
		return WindowFunction.aggregate(AggregateOperator.AVG, column);
	}

	public static WindowFunction count(Column column) {
		return WindowFunction.aggregate(AggregateOperator.COUNT, column);
	}

	public static WindowFunction countStar() {
		return WindowFunction.aggregate(AggregateOperator.COUNT, null);
	}

	public static WindowFunction min(Column column) {
		return WindowFunction.aggregate(AggregateOperator.MIN, column);
	}

	public static WindowFunction max(Column column) {
		return WindowFunction.aggregate(AggregateOperator.MAX, column);
	}

	public static WindowFunction lag(Column column) {
		return lag(column, 1);
	}

	public static WindowFunction lag(Column column, int offset) {
		requireOffset(offset);
		return WindowFunction.of("lag", List.of(WindowExpression.of(column), WindowExpression.raw(Integer.toString(offset))));
	}

	public static WindowFunction lead(Column column) {
		return lead(column, 1);
	}

	public static WindowFunction lead(Column column, int offset) {
		requireOffset(offset);
		return WindowFunction.of("lead", List.of(WindowExpression.of(column), WindowExpression.raw(Integer.toString(offset))));
	}

	private static void requireOffset(int offset) {
		if (offset <= 0) {
			throw new IllegalArgumentException("offset must be positive");
		}
	}
}
