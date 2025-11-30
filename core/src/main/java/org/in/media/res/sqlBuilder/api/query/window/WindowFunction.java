package org.in.media.res.sqlBuilder.api.query.window;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.ColumnRef;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.SortDirection;

public final class WindowFunction {

	private final String logicalName;
	private final List<WindowExpression> arguments = new ArrayList<>();
	private final List<WindowExpression> partitions = new ArrayList<>();
	private final List<WindowOrdering> orderings = new ArrayList<>();
	private WindowFrame frame;
	private String alias;

	private WindowFunction(String logicalName) {
		this.logicalName = Objects.requireNonNull(logicalName, "logicalName");
	}

	private WindowFunction(String logicalName, List<WindowExpression> args) {
		this(logicalName);
		this.arguments.addAll(args);
	}

	public static WindowFunction of(String logicalName) {
		if (logicalName == null || logicalName.isBlank()) {
			throw new IllegalArgumentException("logicalName must not be blank");
		}
		return new WindowFunction(logicalName);
	}

	public static WindowFunction of(String logicalName, WindowExpression... args) {
		return of(logicalName, Arrays.asList(args));
	}

	public static WindowFunction of(String logicalName, List<WindowExpression> args) {
		if (logicalName == null || logicalName.isBlank()) {
			throw new IllegalArgumentException("logicalName must not be blank");
		}
		return new WindowFunction(logicalName, Objects.requireNonNull(args, "args"));
	}

	public static WindowFunction aggregate(AggregateOperator aggregate, Column column) {
		List<WindowExpression> args = new ArrayList<>();
		if (column != null) {
			args.add(WindowExpression.of(column));
		} else if (aggregate == AggregateOperator.COUNT) {
			args.add(WindowExpression.raw("*"));
		}
		return of(Objects.requireNonNull(aggregate, "aggregate").logicalName(), args);
	}

	public WindowFunction partitionBy(Column... columns) {
		for (Column column : columns) {
			this.partitions.add(WindowExpression.of(column));
		}
		return this;
	}

	public WindowFunction partitionBy(ColumnRef<?>... columns) {
		for (ColumnRef<?> column : columns) {
			this.partitions.add(WindowExpression.of(column.column()));
		}
		return this;
	}

	public WindowFunction partitionBy(WindowExpression... expressions) {
		this.partitions.addAll(Arrays.asList(expressions));
		return this;
	}

	public WindowFunction orderBy(Column column) {
		return orderBy(column, SortDirection.ASC);
	}

	public WindowFunction orderBy(ColumnRef<?> column) {
		return orderBy(column.column(), SortDirection.ASC);
	}

	public WindowFunction orderBy(Column column, SortDirection direction) {
		if (direction == SortDirection.DESC) {
			this.orderings.add(WindowOrdering.desc(WindowExpression.of(column)));
		} else {
			this.orderings.add(WindowOrdering.asc(WindowExpression.of(column)));
		}
		return this;
	}

	public WindowFunction orderBy(ColumnRef<?> column, SortDirection direction) {
		if (direction == SortDirection.DESC) {
			this.orderings.add(WindowOrdering.desc(WindowExpression.of(column.column())));
		} else {
			this.orderings.add(WindowOrdering.asc(WindowExpression.of(column.column())));
		}
		return this;
	}

	public WindowFunction orderBy(WindowOrdering ordering) {
		this.orderings.add(Objects.requireNonNull(ordering, "ordering"));
		return this;
	}

	public WindowFunction orderBy(WindowOrdering... ordering) {
		this.orderings.addAll(Arrays.asList(ordering));
		return this;
	}

	public WindowFunction frame(WindowFrame frame) {
		this.frame = frame;
		return this;
	}

	public WindowFunction rowsBetween(WindowFrame.Bound start, WindowFrame.Bound end) {
		return frame(WindowFrame.rowsBetween(start, end));
	}

	public WindowFunction rangeBetween(WindowFrame.Bound start, WindowFrame.Bound end) {
		return frame(WindowFrame.rangeBetween(start, end));
	}

	public WindowFunction as(String alias) {
		this.alias = alias;
		return this;
	}

	public String logicalName() {
		return logicalName;
	}

	public List<WindowExpression> arguments() {
		return Collections.unmodifiableList(arguments);
	}

	public List<WindowExpression> partitions() {
		return Collections.unmodifiableList(partitions);
	}

	public List<WindowOrdering> orderings() {
		return Collections.unmodifiableList(orderings);
	}

	public WindowFrame frame() {
		return frame;
	}

	public String alias() {
		return alias;
	}
}
