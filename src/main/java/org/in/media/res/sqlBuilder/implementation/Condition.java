package org.in.media.res.sqlBuilder.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.Operator;
import org.in.media.res.sqlBuilder.constants.ValueType;
import org.in.media.res.sqlBuilder.implementation.factories.TranspilerFactory;
import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.query.ICondition;
import org.in.media.res.sqlBuilder.interfaces.query.IConditionTranspiler;

/**
 * Immutable representation of a boolean condition. Use {@link Builder} to
 * create instances and the {@code with*} helpers to clone with tweaks.
 */
public final class Condition implements ICondition {

	private static final IConditionTranspiler TRANSPILER = TranspilerFactory.instanciateConditionTranspiler();

	private final Operator startOperator;
	private final ConditionSide left;
	private final ConditionSide right;
	private final Operator operator;
	private final List<ConditionValue> values;

	private Condition(Operator startOperator, ConditionSide left, ConditionSide right, Operator operator,
			List<ConditionValue> values) {
		this.startOperator = startOperator;
		this.left = left;
		this.right = right;
		this.operator = operator;
		this.values = Collections.unmodifiableList(values);
	}

	public static Builder builder() {
		return new Builder();
	}

	public Condition withStartOperator(Operator startOperator) {
		return new Condition(startOperator, left, right, operator, values);
	}

	public Condition withOperator(Operator operator) {
		return new Condition(startOperator, left, right, operator, values);
	}

	public Condition withLeftColumn(IColumn column) {
		return new Condition(startOperator, left.withColumn(column), right, operator, values);
	}

	public Condition withRightColumn(IColumn column) {
		return new Condition(startOperator, left, right.withColumn(column), operator, values);
	}

	public Condition withLeftAggregate(AggregateOperator aggregate) {
		return new Condition(startOperator, left.withAggregate(aggregate), right, operator, values);
	}

	public Condition withRightAggregate(AggregateOperator aggregate) {
		return new Condition(startOperator, left, right.withAggregate(aggregate), operator, values);
	}

	public Condition withNextAggregate(AggregateOperator aggregate) {
		if (!left.hasAggregate()) {
			return withLeftAggregate(aggregate);
		}
		if (!right.hasAggregate()) {
			return withRightAggregate(aggregate);
		}
		throw new IllegalStateException("Both sides already have an aggregate assigned");
	}

	public Condition withNextColumn(IColumn column) {
		if (!left.hasColumn()) {
			return withLeftColumn(column);
		}
		if (!right.hasColumn()) {
			return withRightColumn(column);
		}
		throw new IllegalStateException("Both sides already have a column assigned");
	}

	public Condition appendValue(ConditionValue value) {
		List<ConditionValue> updated = new ArrayList<>(values.size() + 1);
		updated.addAll(values);
		updated.add(value);
		return new Condition(startOperator, left, right, operator, updated);
	}

	public Condition appendValues(List<ConditionValue> additionalValues) {
		if (additionalValues.isEmpty())
			return this;
		List<ConditionValue> updated = new ArrayList<>(values.size() + additionalValues.size());
		updated.addAll(values);
		updated.addAll(additionalValues);
		return new Condition(startOperator, left, right, operator, updated);
	}

	@Override
	public Operator getStartOperator() {
		return startOperator;
	}

	@Override
	public IColumn getLeft() {
		return left.column();
	}

	@Override
	public IColumn getRight() {
		return right.column();
	}

	@Override
	public Operator getOperator() {
		return operator;
	}

	@Override
	public AggregateOperator getLeftAgg() {
		return left.aggregate();
	}

	@Override
	public AggregateOperator getRightAgg() {
		return right.aggregate();
	}

	@Override
	public List<ConditionValue> values() {
		return values;
	}

	@Override
	public String transpile() {
		return TRANSPILER.transpile(this);
	}

	public static final class Builder {

		private Operator startOperator;
		private ConditionSide left = ConditionSide.empty();
		private ConditionSide right = ConditionSide.empty();
		private Operator operator;
		private final List<ConditionValue> values = new ArrayList<>();

		public Builder startOp(Operator startOperator) {
			this.startOperator = startOperator;
			return this;
		}

		public Builder and() {
			return startOp(Operator.AND);
		}

		public Builder or() {
			return startOp(Operator.OR);
		}

		public Builder leftColumn(IColumn column) {
			this.left = left.withColumn(column);
			return this;
		}

		public Builder leftColumn(AggregateOperator aggregate, IColumn column) {
			this.left = left.withAggregate(aggregate).withColumn(column);
			return this;
		}

		public Builder rightColumn(IColumn column) {
			this.right = right.withColumn(column);
			return this;
		}

		public Builder rightColumn(AggregateOperator aggregate, IColumn column) {
			this.right = right.withAggregate(aggregate).withColumn(column);
			return this;
		}

		public Builder comparisonOp(Operator operator) {
			this.operator = operator;
			return this;
		}

		public Builder eq() {
			return comparisonOp(Operator.EQ);
		}

		public Builder in() {
			return comparisonOp(Operator.IN);
		}

		public Builder less() {
			return comparisonOp(Operator.LESS);
		}

		public Builder lessOrEq() {
			return comparisonOp(Operator.LESS_OR_EQ);
		}

		public Builder more() {
			return comparisonOp(Operator.MORE);
		}

		public Builder moreOrEq() {
			return comparisonOp(Operator.MORE_OR_EQ);
		}

		public Builder value(String value) {
			this.values.add(ConditionValue.of(value));
			return this;
		}

		public Builder value(Integer value) {
			this.values.add(ConditionValue.of(value));
			return this;
		}

		public Builder value(Double value) {
			this.values.add(ConditionValue.of(value));
			return this;
		}

		public Builder value(Date value) {
			this.values.add(ConditionValue.of(value));
			return this;
		}

		public Builder values(String... values) {
			for (String value : values) {
				this.values.add(ConditionValue.of(value));
			}
			return this;
		}

		public Builder values(Integer... values) {
			for (Integer value : values) {
				this.values.add(ConditionValue.of(value));
			}
			return this;
		}

		public Builder values(Double... values) {
			for (Double value : values) {
				this.values.add(ConditionValue.of(value));
			}
			return this;
		}

		public Builder values(Date... values) {
			for (Date value : values) {
				this.values.add(ConditionValue.of(value));
			}
			return this;
		}

		public Condition build() {
			return new Condition(startOperator, left, right, operator, List.copyOf(values));
		}
	}

	private static final class ConditionSide {
		private static final ConditionSide EMPTY = new ConditionSide(null, null);

		private final IColumn column;
		private final AggregateOperator aggregate;

		private ConditionSide(IColumn column, AggregateOperator aggregate) {
			this.column = column;
			this.aggregate = aggregate;
		}

		static ConditionSide empty() {
			return EMPTY;
		}

		ConditionSide withColumn(IColumn column) {
			return new ConditionSide(column, aggregate);
		}

		ConditionSide withAggregate(AggregateOperator aggregate) {
			return new ConditionSide(column, aggregate);
		}

		boolean hasColumn() {
			return column != null;
		}

		boolean hasAggregate() {
			return aggregate != null;
		}

		IColumn column() {
			return column;
		}

		AggregateOperator aggregate() {
			return aggregate;
		}
	}

	public static final class ConditionValue {
		private final Object value;
		private final ValueType type;

		private ConditionValue(Object value, ValueType type) {
			this.value = value;
			this.type = type;
		}

		public static ConditionValue of(String value) {
			return new ConditionValue(Objects.requireNonNull(value, "value"), ValueType.TY_STR);
		}

		public static ConditionValue of(Integer value) {
			return new ConditionValue(Objects.requireNonNull(value, "value"), ValueType.TY_INT);
		}

		public static ConditionValue of(Double value) {
			return new ConditionValue(Objects.requireNonNull(value, "value"), ValueType.TY_DBL);
		}

		public static ConditionValue of(Date value) {
			return new ConditionValue(Objects.requireNonNull(value, "value"), ValueType.TY_DATE);
		}

		public Object value() {
			return value;
		}

		public ValueType type() {
			return type;
		}
	}
}
