package org.in.media.res.sqlBuilder.implementation;

import static org.in.media.res.sqlBuilder.constants.AggregateOperator.AVG;
import static org.in.media.res.sqlBuilder.constants.AggregateOperator.MAX;
import static org.in.media.res.sqlBuilder.constants.AggregateOperator.MIN;
import static org.in.media.res.sqlBuilder.constants.AggregateOperator.SUM;
import static org.in.media.res.sqlBuilder.constants.Operator.EQ;
import static org.in.media.res.sqlBuilder.constants.Operator.IN;
import static org.in.media.res.sqlBuilder.constants.Operator.LESS;
import static org.in.media.res.sqlBuilder.constants.Operator.LESS_OR_EQ;
import static org.in.media.res.sqlBuilder.constants.Operator.MORE;
import static org.in.media.res.sqlBuilder.constants.Operator.MORE_OR_EQ;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.Operator;
import org.in.media.res.sqlBuilder.implementation.factories.WhereTranspilerFactory;
import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.query.IAggregator;
import org.in.media.res.sqlBuilder.interfaces.query.IComparator;
import org.in.media.res.sqlBuilder.interfaces.query.ICondition;
import org.in.media.res.sqlBuilder.interfaces.query.IConnector;
import org.in.media.res.sqlBuilder.interfaces.query.IWhere;
import org.in.media.res.sqlBuilder.interfaces.query.IWhereTranspiler;

public class Where implements IWhere {

	private List<ICondition> filters = new ArrayList<>();

	private IWhereTranspiler whereTranspiler = WhereTranspilerFactory.instanciateWhereTranspiler();

	public String transpile() {
		return this.whereTranspiler.transpile(this);
	}

	public List<ICondition> conditions() {
		return filters;
	}

	@Override
	public IWhere where(IColumn column) {
		filters.addLast(Condition.builder().leftColumn(column).build());
		return this;
	}

	@Override
	public IWhere and(IColumn column) {
		filters.addLast(Condition.builder().and().leftColumn(column).build());
		return this;
	}

	@Override
	public IWhere or(IColumn column) {
		filters.addLast(Condition.builder().or().leftColumn(column).build());
		return this;
	}

	@Override
	public IWhere and() {
		filters.addLast(Condition.builder().and().build());
		return this;
	}

	@Override
	public IWhere or() {
		filters.addLast(Condition.builder().or().build());
		return this;
	}

	@Override
	public IConnector eq(IColumn column) {
		this.updateLastCondition(EQ, null, column);
		return this;
	}

	@Override
	public IConnector supTo(IColumn column) {
		this.updateLastCondition(MORE, null, column);
		return this;
	}

	@Override
	public IConnector infTo(IColumn column) {
		this.updateLastCondition(LESS, null, column);
		return this;
	}

	@Override
	public IConnector supOrEqTo(IColumn column) {
		this.updateLastCondition(MORE_OR_EQ, null, column);
		return this;
	}

	@Override
	public IConnector infOrEqTo(IColumn column) {
		this.updateLastCondition(LESS_OR_EQ, null, column);
		return this;
	}

	@Override
	public IConnector eq(String value) {
		this.updateLastCondition(EQ, value);
		return this;
	}

	@Override
	public IConnector supTo(String value) {
		this.updateLastCondition(MORE, value);
		return this;
	}

	@Override
	public IConnector infTo(String value) {
		this.updateLastCondition(LESS, value);
		return this;
	}

	@Override
	public IConnector supOrEqTo(String value) {
		this.updateLastCondition(MORE_OR_EQ, value);
		return this;
	}

	@Override
	public IConnector infOrEqTo(String value) {
		this.updateLastCondition(LESS_OR_EQ, value);
		return this;
	}

	@Override
	public IConnector in(String... value) {
		this.updateLastCondition(IN, value);
		return this;
	}

	@Override
	public IConnector eq(Integer value) {
		this.updateLastCondition(EQ, value);
		return this;
	}

	@Override
	public IConnector supTo(Integer value) {
		this.updateLastCondition(MORE, value);
		return this;
	}

	@Override
	public IConnector infTo(Integer value) {
		this.updateLastCondition(LESS, value);
		return this;
	}

	@Override
	public IConnector supOrEqTo(Integer value) {
		this.updateLastCondition(MORE_OR_EQ, value);
		return this;
	}

	@Override
	public IConnector infOrEqTo(Integer value) {
		this.updateLastCondition(LESS_OR_EQ, value);
		return this;
	}

	@Override
	public IConnector in(Integer... value) {
		this.updateLastCondition(IN, value);
		return this;
	}

	@Override
	public IConnector eq(Date value) {
		this.updateLastCondition(EQ, value);
		return this;
	}

	@Override
	public IConnector supTo(Date value) {
		this.updateLastCondition(MORE, value);
		return this;
	}

	@Override
	public IConnector infTo(Date value) {
		this.updateLastCondition(LESS, value);
		return this;
	}

	@Override
	public IConnector supOrEqTo(Date value) {
		this.updateLastCondition(MORE_OR_EQ, value);
		return this;
	}

	@Override
	public IConnector infOrEqTo(Date value) {
		this.updateLastCondition(LESS_OR_EQ, value);
		return this;
	}

	@Override
	public IConnector in(Date... value) {
		this.updateLastCondition(IN, value);
		return this;
	}

	@Override
	public IConnector eq(Double value) {
		this.updateLastCondition(EQ, value);
		return this;
	}

	@Override
	public IConnector supTo(Double value) {
		this.updateLastCondition(MORE, value);
		return this;
	}

	@Override
	public IConnector infTo(Double value) {
		this.updateLastCondition(LESS, value);
		return this;
	}

	@Override
	public IConnector supOrEqTo(Double value) {
		this.updateLastCondition(MORE_OR_EQ, value);
		return this;
	}

	@Override
	public IConnector infOrEqTo(Double value) {
		this.updateLastCondition(LESS_OR_EQ, value);
		return this;
	}

	@Override
	public IConnector in(Double... value) {
		this.updateLastCondition(IN, value);
		return this;
	}

	@Override
	public IAggregator eq() {
		lastCondition().setOperator(EQ);
		return this;
	}

	@Override
	public IAggregator supTo() {
		lastCondition().setOperator(MORE);
		return this;
	}

	@Override
	public IAggregator infTo() {
		lastCondition().setOperator(LESS);
		return this;
	}

	@Override
	public IAggregator supOrEqTo() {
		lastCondition().setOperator(MORE_OR_EQ);
		return this;
	}

	@Override
	public IAggregator infOrEqTo() {
		lastCondition().setOperator(LESS_OR_EQ);
		return this;
	}

	@Override
	public IAggregator in() {
		lastCondition().setOperator(IN);
		return this;
	}

	@Override
	public IComparator min(IColumn column) {
		this.updateLastCondition(null, MIN, column);
		return this;
	}

	@Override
	public IComparator max(IColumn column) {
		this.updateLastCondition(null, MAX, column);
		return this;
	}

	@Override
	public IComparator sum(IColumn column) {
		this.updateLastCondition(null, SUM, column);
		return this;
	}

	@Override
	public IComparator avg(IColumn column) {
		this.updateLastCondition(null, AVG, column);
		return this;
	}

	@Override
	public IComparator col(IColumn column) {
		this.lastCondition().setLeft(column);
		return this;
	}

	private void updateLastCondition(Operator op, String... value) {
		ICondition condition = lastCondition();
		condition.setOperator(op);
		for (String v : value) {
			condition.addValue(v);
		}
	}

	private void updateLastCondition(Operator op, Integer... value) {
		ICondition condition = lastCondition();
		condition.setOperator(op);
		for (Integer v : value) {
			condition.addValue(v);
		}
	}

	private void updateLastCondition(Operator op, Date... value) {
		ICondition condition = lastCondition();
		condition.setOperator(op);
		for (Date v : value) {
			condition.addValue(v);
		}
	}

	private void updateLastCondition(Operator op, Double... value) {
		ICondition condition = lastCondition();
		condition.setOperator(op);
		for (Double v : value) {
			condition.addValue(v);
		}
	}

	private void updateLastCondition(Operator op, AggregateOperator agg, IColumn value) {
		ICondition condition = lastCondition();
		if (op != null) {
			condition.setOperator(op);
		}
		if (agg != null) {
			setAggAtRightPlace(condition, agg);
		}
		setColumnAtRightPlace(condition, value);
	}

	private void setAggAtRightPlace(ICondition condition, AggregateOperator agg) {
		if (condition.getLeftAgg() == null)
			condition.setLeftAgg(agg);
		else if (condition.getRightAgg() == null)
			condition.setRightAgg(agg);

	}

	private void setColumnAtRightPlace(ICondition condition, IColumn value) {
		if (condition.getLeft() == null)
			condition.setLeft(value);
		else if (condition.getRight() == null)
			condition.setRight(value);
	}

	@Override
	public IWhere condition(ICondition condition) {
		this.filters.addLast(condition);
		return this;
	}

	private ICondition lastCondition() {
		if (filters.isEmpty()) {
			throw new IllegalStateException("Cannot apply operators without a starting condition. Call where(...) first.");
		}
		return filters.getLast();
	}
}
