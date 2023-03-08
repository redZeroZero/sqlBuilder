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
import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.query.IAggregator;
import org.in.media.res.sqlBuilder.interfaces.query.IComparator;
import org.in.media.res.sqlBuilder.interfaces.query.ICondition;
import org.in.media.res.sqlBuilder.interfaces.query.IConnector;
import org.in.media.res.sqlBuilder.interfaces.query.IWhere;

public class Where implements IWhere {

	private StringBuilder sb = new StringBuilder();

	private String WHERE_ = " WHERE ";

	private List<ICondition> filters = new ArrayList<>();

	public String transpile() {
		resetBuilder();
		sb.append(WHERE_);
		if (haveData()) {
			for (ICondition f : filters)
				sb.append(f.transpile());
		}
		return sb.toString();
	}

	private boolean haveData() {
		return !filters.isEmpty();
	}

	public void reset() {
		resetBuilder();
	}

	private void resetBuilder() {
		sb.setLength(0);
	}

	@Override
	public IWhere where(IColumn column) {
		filters.add(Condition.builder().leftColumn(column).build());
		return this;
	}

	@Override
	public IWhere and(IColumn column) {
		filters.add(Condition.builder().and().leftColumn(column).build());
		return this;
	}

	@Override
	public IWhere or(IColumn column) {
		filters.add(Condition.builder().or().leftColumn(column).build());
		return this;
	}

	@Override
	public IWhere and() {
		filters.add(Condition.builder().and().build());
		return this;
	}

	@Override
	public IWhere or() {
		filters.add(Condition.builder().or().build());
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
		filters.get(filters.size() - 1).setOperator(EQ);
		return this;
	}

	@Override
	public IAggregator supTo() {
		filters.get(filters.size() - 1).setOperator(MORE);
		return this;
	}

	@Override
	public IAggregator infTo() {
		filters.get(filters.size() - 1).setOperator(LESS);
		return this;
	}

	@Override
	public IAggregator supOrEqTo() {
		filters.get(filters.size() - 1).setOperator(MORE_OR_EQ);
		return this;
	}

	@Override
	public IAggregator infOrEqTo() {
		filters.get(filters.size() - 1).setOperator(Operator.LESS_OR_EQ);
		return this;
	}

	@Override
	public IAggregator in() {
		filters.get(filters.size() - 1).setOperator(IN);
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
		this.filters.get(filters.size() - 1).setLeft(column);
		return this;
	}

	private void updateLastCondition(Operator op, String... value) {
		filters.get(filters.size() - 1).setOperator(op);
		for (String v : value)
			filters.get(filters.size() - 1).addValue(v);
	}

	private void updateLastCondition(Operator op, Integer... value) {
		filters.get(filters.size() - 1).setOperator(op);
		for (Integer v : value)
			filters.get(filters.size() - 1).addValue(v);
	}

	private void updateLastCondition(Operator op, Date... value) {
		filters.get(filters.size() - 1).setOperator(op);
		for (Date v : value)
			filters.get(filters.size() - 1).addValue(v);
	}

	private void updateLastCondition(Operator op, Double... value) {
		filters.get(filters.size() - 1).setOperator(op);
		for (Double v : value)
			filters.get(filters.size() - 1).addValue(v);
	}

	private void updateLastCondition(Operator op, AggregateOperator agg, IColumn value) {
		if (op != null)
			filters.get(filters.size() - 1).setOperator(op);
		if (agg != null)
			setAggAtRightPlace(agg);
		setColumnAtRightPlace(value);
	}

	private void setAggAtRightPlace(AggregateOperator agg) {
		if (filters.get(filters.size() - 1).getLeftAgg() == null)
			filters.get(filters.size() - 1).setLeftAgg(agg);
		else if (filters.get(filters.size() - 1).getRightAgg() == null)
			filters.get(filters.size() - 1).setRightAgg(agg);

	}

	private void setColumnAtRightPlace(IColumn value) {
		if (filters.get(filters.size() - 1).getLeft() == null)
			filters.get(filters.size() - 1).setLeft(value);
		else if (filters.get(filters.size() - 1).getRight() == null)
			filters.get(filters.size() - 1).setRight(value);
	}

	@Override
	public IWhere condition(ICondition condition) {
		this.filters.add(condition);
		return this;
	}
}
