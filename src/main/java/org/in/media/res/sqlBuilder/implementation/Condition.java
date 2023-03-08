package org.in.media.res.sqlBuilder.implementation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.Operator;
import org.in.media.res.sqlBuilder.constants.ValueType;
import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.query.ICondition;

public class Condition implements ICondition {

	private final String OPENING_PARENTHESIS = "(";

	private final String CLOSING_PARENTHESIS = ")";

	private final String POUIC = "'";

	private final String SEP_ = ", ";

	private IColumn left;

	private IColumn right;

	private Operator startOperator;

	private Operator operator;

	private AggregateOperator leftAgg;

	private AggregateOperator rightAgg;

	private List<Item> values = new ArrayList<>();

	private final StringBuilder sb = new StringBuilder();

	@Override
	public String transpile() {
		resetBuilder();
		changeOperatorIfNeeded();
		if (startOperator != null)
			sb.append(startOperator.value());
		transpileColumn(left, leftAgg);
		sb.append(operator.value());
		if (right != null)
			transpileColumn(right, rightAgg);
		else
			transpileValues();
		return sb.toString();
	}

	private void transpileColumn(IColumn col, AggregateOperator agg) {
		if (agg != null)
			sb.append(agg.value()).append(OPENING_PARENTHESIS);
		sb.append(col.transpile(false));
		if (agg != null)
			sb.append(CLOSING_PARENTHESIS);
	}

	private void changeOperatorIfNeeded() {
		if (values.size() > 1 && operator.equals(Operator.EQ))
			operator = Operator.IN;
	}

	private void transpileValues() {
		if (values.size() == 1) {
			switchOnOpType(0, true);
		} else {
			sb.append(OPENING_PARENTHESIS);
			for (int i = 0; i < values.size(); i++)
				switchOnOpType(i, isLast(i));
			sb.append(CLOSING_PARENTHESIS);
		}
	}

	private void switchOnOpType(int index, boolean isLast) {
		switch (values.get(index).type) {
		case TY_DATE:
		case TY_STR:
			this.builStringItem(index, isLast);
			break;
		case TY_DBL:
		case TY_INT:
			this.builIntItem(index, isLast);
			break;
		default:
			this.builStringItem(index, isLast);
			break;
		}
	}

	private boolean isLast(int i) {
		return values.size() - 1 == i;
	}

	private void builStringItem(int index, boolean last) {
		sb.append(POUIC).append(values.get(index).value).append(POUIC);
		if (!last)
			sb.append(SEP_);
	}

	private void builIntItem(int index, boolean last) {
		sb.append(values.get(index).value);
		if (!last)
			sb.append(SEP_);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.coface.corp.sqlBuilder.implementation.ICondition#addValue(java.lang.
	 * String)
	 */
	@Override
	public void addValue(String value) {
		this.addValue(new Item(value, ValueType.TY_STR));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.coface.corp.sqlBuilder.implementation.ICondition#addValue(java.lang.
	 * Integer)
	 */
	@Override
	public void addValue(Integer value) {
		this.addValue(new Item(value, ValueType.TY_INT));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.coface.corp.sqlBuilder.implementation.ICondition#addValue(java.lang.
	 * Double)
	 */
	@Override
	public void addValue(Double value) {
		this.addValue(new Item(value, ValueType.TY_DBL));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.coface.corp.sqlBuilder.implementation.ICondition#addValue(java.util.Date)
	 */
	@Override
	public void addValue(Date value) {
		this.addValue(new Item(value, ValueType.TY_DATE));
	}

	private void addValue(Item value) {
		this.values.add(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.coface.corp.sqlBuilder.implementation.ICondition#resetBuilder()
	 */
	@Override
	public void resetBuilder() {
		this.sb.setLength(0);
	}

	public static Builder builder() {
		return new Builder();
	}

	private static class Item {

		Object value;

		ValueType type;

		public Item(Object value, ValueType type) {
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
			this.c.values.add(new Condition.Item(value, ValueType.TY_STR));
			return this;
		}

		public Builder value(AggregateOperator agg, String value) {
			this.c.rightAgg = agg;
			this.c.values.add(new Condition.Item(value, ValueType.TY_STR));
			return this;
		}

		public Builder values(String... values) {
			for (String v : values)
				c.values.add(new Condition.Item(v, ValueType.TY_STR));
			return this;
		}

		public Builder value(Integer value) {
			this.c.values.add(new Condition.Item(value, ValueType.TY_INT));
			return this;
		}

		public Builder value(AggregateOperator agg, Integer value) {
			this.c.rightAgg = agg;
			this.c.values.add(new Condition.Item(value, ValueType.TY_INT));
			return this;
		}

		public Builder values(Integer... values) {
			for (Integer v : values)
				this.c.values.add(new Condition.Item(v, ValueType.TY_INT));
			return this;
		}

		public Builder value(Date value) {
			this.c.values.add(new Condition.Item(value, ValueType.TY_DATE));
			return this;
		}

		public Builder value(AggregateOperator agg, Date value) {
			this.c.rightAgg = agg;
			this.c.values.add(new Condition.Item(value, ValueType.TY_DATE));
			return this;
		}

		public Builder values(Date... values) {
			for (Date v : values)
				this.c.values.add(new Condition.Item(v, ValueType.TY_DATE));
			return this;
		}

		public Builder value(Double value) {
			this.c.values.add(new Condition.Item(value, ValueType.TY_DBL));
			return this;
		}

		public Builder value(AggregateOperator agg, Double value) {
			this.c.rightAgg = agg;
			this.c.values.add(new Condition.Item(value, ValueType.TY_DBL));
			return this;
		}

		public Builder values(Double... values) {
			for (Double v : values)
				this.c.values.add(new Condition.Item(v, ValueType.TY_DBL));
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.coface.corp.sqlBuilder.implementation.ICondition#getLeft()
	 */
	@Override
	public IColumn getLeft() {
		return left;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.coface.corp.sqlBuilder.implementation.ICondition#setLeft(com.coface.corp.
	 * sqlBuilder.interfaces.model.IColumn)
	 */
	@Override
	public void setLeft(IColumn left) {
		this.left = left;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.coface.corp.sqlBuilder.implementation.ICondition#getRight()
	 */
	@Override
	public IColumn getRight() {
		return right;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.coface.corp.sqlBuilder.implementation.ICondition#setRight(com.coface.corp
	 * .sqlBuilder.interfaces.model.IColumn)
	 */
	@Override
	public void setRight(IColumn right) {
		this.right = right;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.coface.corp.sqlBuilder.implementation.ICondition#getStartOperator()
	 */
	@Override
	public Operator getStartOperator() {
		return startOperator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.coface.corp.sqlBuilder.implementation.ICondition#setStartOperator(com.
	 * coface.corp.sqlBuilder.constants.Operator)
	 */
	@Override
	public void setStartOperator(Operator startOperator) {
		this.startOperator = startOperator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.coface.corp.sqlBuilder.implementation.ICondition#getOperator()
	 */
	@Override
	public Operator getOperator() {
		return operator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.coface.corp.sqlBuilder.implementation.ICondition#setOperator(com.coface.
	 * corp.sqlBuilder.constants.Operator)
	 */
	@Override
	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.coface.corp.sqlBuilder.implementation.ICondition#getLeftAgg()
	 */
	@Override
	public AggregateOperator getLeftAgg() {
		return leftAgg;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.coface.corp.sqlBuilder.implementation.ICondition#setLeftAgg(com.coface.
	 * corp.sqlBuilder.constants.AggregateOperator)
	 */
	@Override
	public void setLeftAgg(AggregateOperator leftAgg) {
		this.leftAgg = leftAgg;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.coface.corp.sqlBuilder.implementation.ICondition#getRightAgg()
	 */
	@Override
	public AggregateOperator getRightAgg() {
		return rightAgg;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.coface.corp.sqlBuilder.implementation.ICondition#setRightAgg(com.coface.
	 * corp.sqlBuilder.constants.AggregateOperator)
	 */
	@Override
	public void setRightAgg(AggregateOperator rightAgg) {
		this.rightAgg = rightAgg;
	}

	public List<Item> getValues() {
		return values;
	}

	public void setValues(List<Item> values) {
		this.values = values;
	}
}