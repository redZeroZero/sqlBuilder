package org.in.media.res.sqlBuilder.implementation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.Operator;
import org.in.media.res.sqlBuilder.constants.ValueType;
import org.in.media.res.sqlBuilder.implementation.factories.TranspilerFactory;
import org.in.media.res.sqlBuilder.implementation.transpilers.clauses.OracleConditionTranspilerImpl;
import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.query.ICondition;
import org.in.media.res.sqlBuilder.interfaces.query.IConditionTranspiler;

public class Condition implements ICondition {

	private IColumn left;

	private IColumn right;

	private Operator startOperator;

	private Operator operator;

	private AggregateOperator leftAgg;

	private AggregateOperator rightAgg;

	private List<ConditionItem> values = new ArrayList<>();

	private IConditionTranspiler transpiler = TranspilerFactory.instanciateConditionTranspiler();

	@Override
	public String transpile() {
		return transpiler.transpile(this);
	}

	public List<ConditionItem> values() {
		return this.values;
	}

	@Override
	public void addValue(String value) {
		this.addValue(new ConditionItem(value, ValueType.TY_STR));
	}

	@Override
	public void addValue(Integer value) {
		this.addValue(new ConditionItem(value, ValueType.TY_INT));
	}

	@Override
	public void addValue(Double value) {
		this.addValue(new ConditionItem(value, ValueType.TY_DBL));
	}

	@Override
	public void addValue(Date value) {
		this.addValue(new ConditionItem(value, ValueType.TY_DATE));
	}

	private void addValue(ConditionItem value) {
		this.values.add(value);
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public IColumn getLeft() {
		return left;
	}

	@Override
	public void setLeft(IColumn left) {
		this.left = left;
	}

	@Override
	public IColumn getRight() {
		return right;
	}

	@Override
	public void setRight(IColumn right) {
		this.right = right;
	}

	@Override
	public Operator getStartOperator() {
		return startOperator;
	}

	@Override
	public void setStartOperator(Operator startOperator) {
		this.startOperator = startOperator;
	}

	@Override
	public Operator getOperator() {
		return operator;
	}

	@Override
	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	@Override
	public AggregateOperator getLeftAgg() {
		return leftAgg;
	}

	@Override
	public void setLeftAgg(AggregateOperator leftAgg) {
		this.leftAgg = leftAgg;
	}

	@Override
	public AggregateOperator getRightAgg() {
		return rightAgg;
	}

	@Override
	public void setRightAgg(AggregateOperator rightAgg) {
		this.rightAgg = rightAgg;
	}

	public List<ConditionItem> getValues() {
		return values;
	}

	public void setValues(List<ConditionItem> values) {
		this.values = values;
	}

	public static class ConditionItem {

		public Object value;

		public ValueType type;

		public ConditionItem(Object value, ValueType type) {
			this.value = value;
			this.type = type;
		}
	}

	public static class Builder {

		Condition c = null;

		public Builder() {
			c = new Condition();
		}

		public Builder newBuilder() {
			return new Builder();
		}

		public Builder startOp(Operator startOperator) {
			this.c.startOperator = startOperator;
			return this;
		}

		public Builder or() {
			this.c.startOperator = Operator.OR;
			return this;
		}

		public Builder and() {
			this.c.startOperator = Operator.AND;
			return this;
		}

		public Builder comparisonOp(Operator operator) {
			this.c.operator = operator;
			return this;
		}

		public Builder eq() {
			this.c.operator = Operator.EQ;
			return this;
		}

		public Builder in() {
			this.c.operator = Operator.IN;
			return this;
		}

		public Builder less() {
			this.c.operator = Operator.LESS;
			return this;
		}

		public Builder lessOrEq() {
			this.c.operator = Operator.LESS_OR_EQ;
			return this;
		}

		public Builder more() {
			this.c.operator = Operator.MORE;
			return this;
		}

		public Builder moreOrEq() {
			this.c.operator = Operator.MORE_OR_EQ;
			return this;
		}

		public Builder value(String value) {
			this.c.values.add(new Condition.ConditionItem(value, ValueType.TY_STR));
			return this;
		}

		public Builder value(AggregateOperator agg, String value) {
			this.c.rightAgg = agg;
			this.c.values.add(new Condition.ConditionItem(value, ValueType.TY_STR));
			return this;
		}

		public Builder values(String... values) {
			for (String v : values)
				c.values.add(new Condition.ConditionItem(v, ValueType.TY_STR));
			return this;
		}

		public Builder value(Integer value) {
			this.c.values.add(new Condition.ConditionItem(value, ValueType.TY_INT));
			return this;
		}

		public Builder value(AggregateOperator agg, Integer value) {
			this.c.rightAgg = agg;
			this.c.values.add(new Condition.ConditionItem(value, ValueType.TY_INT));
			return this;
		}

		public Builder values(Integer... values) {
			for (Integer v : values)
				this.c.values.add(new Condition.ConditionItem(v, ValueType.TY_INT));
			return this;
		}

		public Builder value(Date value) {
			this.c.values.add(new Condition.ConditionItem(value, ValueType.TY_DATE));
			return this;
		}

		public Builder value(AggregateOperator agg, Date value) {
			this.c.rightAgg = agg;
			this.c.values.add(new Condition.ConditionItem(value, ValueType.TY_DATE));
			return this;
		}

		public Builder values(Date... values) {
			for (Date v : values)
				this.c.values.add(new Condition.ConditionItem(v, ValueType.TY_DATE));
			return this;
		}

		public Builder value(Double value) {
			this.c.values.add(new Condition.ConditionItem(value, ValueType.TY_DBL));
			return this;
		}

		public Builder value(AggregateOperator agg, Double value) {
			this.c.rightAgg = agg;
			this.c.values.add(new Condition.ConditionItem(value, ValueType.TY_DBL));
			return this;
		}

		public Builder values(Double... values) {
			for (Double v : values)
				this.c.values.add(new Condition.ConditionItem(v, ValueType.TY_DBL));
			return this;
		}

		public Builder leftColumn(AggregateOperator agg, IColumn col) {
			this.c.leftAgg = agg;
			this.c.left = col;
			return this;
		}

		public Builder leftColumn(IColumn col) {
			this.c.left = col;
			return this;
		}

		public Builder rightColumn(AggregateOperator agg, IColumn col) {
			this.c.rightAgg = agg;
			this.c.right = col;
			return this;
		}

		public Builder rightColumn(IColumn col) {
			this.c.right = col;
			return this;
		}

		public Condition build() {
			return c;
		}

	}
}