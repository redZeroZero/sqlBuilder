package org.in.media.res.sqlBuilder.implementation;

import java.util.ArrayList;
import java.util.List;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.Operator;
import org.in.media.res.sqlBuilder.implementation.factories.HavingTranspilerFactory;
import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.query.ICondition;
import org.in.media.res.sqlBuilder.interfaces.query.IHaving;
import org.in.media.res.sqlBuilder.interfaces.query.IHavingBuilder;
import org.in.media.res.sqlBuilder.interfaces.query.IHavingTranspiler;

public class Having implements IHaving {

	private final List<ICondition> conditions = new ArrayList<>();

	private final IHavingTranspiler havingTranspiler = HavingTranspilerFactory.instanciateHavingTranspiler();

	@Override
	public String transpile() {
		return conditions.isEmpty() ? "" : havingTranspiler.transpile(this);
	}

	@Override
	public void reset() {
		conditions.clear();
	}

	@Override
	public IHaving having(ICondition condition) {
		conditions.addLast(condition);
		return this;
	}

	@Override
	public IHaving and(ICondition condition) {
		conditions.addLast(condition);
		return this;
	}

	@Override
	public IHaving or(ICondition condition) {
		conditions.addLast(condition);
		return this;
	}

	@Override
	public IHavingBuilder having(IColumn column) {
		conditions.addLast(Condition.builder().leftColumn(column).build());
		return new BuilderDelegate();
	}


	@Override
	public List<ICondition> havingConditions() {
		return conditions;
	}

	private ICondition lastCondition() {
		if (conditions.isEmpty()) {
			throw new IllegalStateException("Cannot apply operators without a HAVING condition. Call having(...) first.");
		}
		return conditions.getLast();
	}

	private class BuilderDelegate implements IHavingBuilder {

		@Override
		public IHaving eq(String value) {
			lastCondition().setOperator(Operator.EQ);
			lastCondition().addValue(value);
			return Having.this;
		}

		@Override
		public IHaving eq(Number value) {
			lastCondition().setOperator(Operator.EQ);
			addNumericValue(value);
			return Having.this;
		}

		@Override
		public IHaving eq(java.util.Date value) {
			lastCondition().setOperator(Operator.EQ);
			lastCondition().addValue(value);
			return Having.this;
		}

		@Override
		public IHaving in(String... values) {
			lastCondition().setOperator(Operator.IN);
			for (String v : values) {
				lastCondition().addValue(v);
			}
			return Having.this;
		}

		@Override
		public IHaving in(Number... values) {
			lastCondition().setOperator(Operator.IN);
			for (Number v : values) {
				addNumericValue(v);
			}
			return Having.this;
		}

		@Override
		public IHaving supTo(Number value) {
			lastCondition().setOperator(Operator.MORE);
			addNumericValue(value);
			return Having.this;
		}

		@Override
		public IHaving supOrEqTo(Number value) {
			lastCondition().setOperator(Operator.MORE_OR_EQ);
			addNumericValue(value);
			return Having.this;
		}

		@Override
		public IHaving infTo(Number value) {
			lastCondition().setOperator(Operator.LESS);
			addNumericValue(value);
			return Having.this;
		}

		@Override
		public IHaving infOrEqTo(Number value) {
			lastCondition().setOperator(Operator.LESS_OR_EQ);
			addNumericValue(value);
			return Having.this;
		}

		@Override
		public IHaving supTo(IColumn column) {
			lastCondition().setOperator(Operator.MORE);
			lastCondition().setRight(column);
			return Having.this;
		}

		@Override
		public IHaving supOrEqTo(IColumn column) {
			lastCondition().setOperator(Operator.MORE_OR_EQ);
			lastCondition().setRight(column);
			return Having.this;
		}

		@Override
		public IHaving infTo(IColumn column) {
			lastCondition().setOperator(Operator.LESS);
			lastCondition().setRight(column);
			return Having.this;
		}

		@Override
		public IHaving infOrEqTo(IColumn column) {
			lastCondition().setOperator(Operator.LESS_OR_EQ);
			lastCondition().setRight(column);
			return Having.this;
		}

		@Override
		public IHavingBuilder and(IColumn column) {
			conditions.addLast(Condition.builder().and().leftColumn(column).build());
			return this;
		}

		@Override
		public IHavingBuilder or(IColumn column) {
			conditions.addLast(Condition.builder().or().leftColumn(column).build());
			return this;
		}

		@Override
		public IHavingBuilder min(IColumn column) {
			setAggregate(AggregateOperator.MIN, column);
			return this;
		}

		@Override
		public IHavingBuilder max(IColumn column) {
			setAggregate(AggregateOperator.MAX, column);
			return this;
		}

		@Override
		public IHavingBuilder sum(IColumn column) {
			setAggregate(AggregateOperator.SUM, column);
			return this;
		}

		@Override
		public IHavingBuilder avg(IColumn column) {
			setAggregate(AggregateOperator.AVG, column);
			return this;
		}

		@Override
		public IHavingBuilder col(IColumn column) {
			lastCondition().setRight(column);
			return this;
		}
	}

	private void addNumericValue(Number value) {
		if (value instanceof Double || value instanceof Float) {
			lastCondition().addValue(value.doubleValue());
		} else {
			lastCondition().addValue(value.intValue());
		}
	}

	private void setAggregate(AggregateOperator aggregate, IColumn column) {
		lastCondition().setLeftAgg(aggregate);
		lastCondition().setLeft(column);
	}
}
