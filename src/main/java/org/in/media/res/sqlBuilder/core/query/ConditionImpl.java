package org.in.media.res.sqlBuilder.core.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.Operator;
import org.in.media.res.sqlBuilder.core.query.factory.TranspilerFactory;
import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.query.Condition;
import org.in.media.res.sqlBuilder.api.query.ConditionTranspiler;
import org.in.media.res.sqlBuilder.api.query.ConditionValue;

/**
 * Immutable representation of a boolean condition. Use {@link Builder} to
 * create instances and the {@code with*} helpers to clone with tweaks.
 */
public final class ConditionImpl implements Condition {

	private static final ConditionTranspiler TRANSPILER = TranspilerFactory.instanciateConditionTranspiler();

	private final Operator startOperator;
	private final ConditionSide left;
	private final ConditionSide right;
	private final Operator operator;
	private final List<ConditionValue> values;

    private ConditionImpl(Operator startOperator, ConditionSide left, ConditionSide right, Operator operator,
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

    public ConditionImpl withStartOperator(Operator startOperator) {
        return new ConditionImpl(startOperator, left, right, operator, values);
    }

    public ConditionImpl withOperator(Operator operator) {
        return new ConditionImpl(startOperator, left, right, operator, values);
    }

    public ConditionImpl withLeftColumn(Column column) {
        return new ConditionImpl(startOperator, left.withColumn(column), right, operator, values);
    }

	public ConditionImpl withRightColumn(Column column) {
		return new ConditionImpl(startOperator, left, right.withColumn(column), operator, values);
	}

	public ConditionImpl withLeftAggregate(AggregateOperator aggregate) {
		return new ConditionImpl(startOperator, left.withAggregate(aggregate), right, operator, values);
	}

	public ConditionImpl withRightAggregate(AggregateOperator aggregate) {
		return new ConditionImpl(startOperator, left, right.withAggregate(aggregate), operator, values);
	}

	public ConditionImpl withNextAggregate(AggregateOperator aggregate) {
		if (!left.hasAggregate()) {
			return withLeftAggregate(aggregate);
		}
		if (!right.hasAggregate()) {
			return withRightAggregate(aggregate);
		}
		throw new IllegalStateException("Both sides already have an aggregate assigned");
	}

	public ConditionImpl withNextColumn(Column column) {
		if (!left.hasColumn()) {
			return withLeftColumn(column);
		}
		if (!right.hasColumn()) {
			return withRightColumn(column);
		}
		throw new IllegalStateException("Both sides already have a column assigned");
	}

	public ConditionImpl appendValue(ConditionValue value) {
		List<ConditionValue> updated = new ArrayList<>(values.size() + 1);
		updated.addAll(values);
		updated.add(value);
		return new ConditionImpl(startOperator, left, right, operator, updated);
	}

	public ConditionImpl appendValues(List<ConditionValue> additionalValues) {
		if (additionalValues.isEmpty())
			return this;
		List<ConditionValue> updated = new ArrayList<>(values.size() + additionalValues.size());
		updated.addAll(values);
		updated.addAll(additionalValues);
		return new ConditionImpl(startOperator, left, right, operator, updated);
	}

	@Override
	public Operator getStartOperator() {
		return startOperator;
	}

	@Override
	public Column getLeft() {
		return left.column();
	}

	@Override
	public Column getRight() {
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

		public Builder leftColumn(Column column) {
			this.left = left.withColumn(column);
			return this;
		}

		public Builder leftColumn(AggregateOperator aggregate, Column column) {
			this.left = left.withAggregate(aggregate).withColumn(column);
			return this;
		}

		public Builder rightColumn(Column column) {
			this.right = right.withColumn(column);
			return this;
		}

		public Builder rightColumn(AggregateOperator aggregate, Column column) {
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

		public ConditionImpl build() {
			return new ConditionImpl(startOperator, left, right, operator, List.copyOf(values));
		}
	}

	private static final class ConditionSide {
		private static final ConditionSide EMPTY = new ConditionSide(null, null);

		private final Column column;
		private final AggregateOperator aggregate;

		private ConditionSide(Column column, AggregateOperator aggregate) {
			this.column = column;
			this.aggregate = aggregate;
		}

		static ConditionSide empty() {
			return EMPTY;
		}

		ConditionSide withColumn(Column column) {
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

		Column column() {
			return column;
		}

		AggregateOperator aggregate() {
			return aggregate;
		}
	}

}
