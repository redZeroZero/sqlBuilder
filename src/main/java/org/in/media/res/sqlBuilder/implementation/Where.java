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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.UnaryOperator;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.Operator;
import org.in.media.res.sqlBuilder.implementation.Condition.ConditionValue;
import org.in.media.res.sqlBuilder.implementation.factories.WhereTranspilerFactory;
import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.query.IAggregator;
import org.in.media.res.sqlBuilder.interfaces.query.IComparator;
import org.in.media.res.sqlBuilder.interfaces.query.ICondition;
import org.in.media.res.sqlBuilder.interfaces.query.IConnector;
import org.in.media.res.sqlBuilder.interfaces.query.IWhere;
import org.in.media.res.sqlBuilder.interfaces.query.IWhereTranspiler;

public class Where implements IWhere {

	private final List<Condition> filters = new ArrayList<>();

	private final IWhereTranspiler whereTranspiler = WhereTranspilerFactory.instanciateWhereTranspiler();

	@Override
	public String transpile() {
		return this.whereTranspiler.transpile(this);
	}

	@Override
	public List<ICondition> conditions() {
		return List.copyOf(filters);
	}

	@Override
	public IWhere where(IColumn column) {
		filters.addLast(Condition.builder().leftColumn(requireColumn(column)).build());
		return this;
	}

	@Override
	public IWhere and(IColumn column) {
		filters.addLast(Condition.builder().and().leftColumn(requireColumn(column)).build());
		return this;
	}

	@Override
	public IWhere or(IColumn column) {
		filters.addLast(Condition.builder().or().leftColumn(requireColumn(column)).build());
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
		this.updateLastCondition(EQ, null, requireColumn(column));
		return this;
	}

	@Override
	public IConnector supTo(IColumn column) {
		this.updateLastCondition(MORE, null, requireColumn(column));
		return this;
	}

	@Override
	public IConnector infTo(IColumn column) {
		this.updateLastCondition(LESS, null, requireColumn(column));
		return this;
	}

	@Override
	public IConnector supOrEqTo(IColumn column) {
		this.updateLastCondition(MORE_OR_EQ, null, requireColumn(column));
		return this;
	}

	@Override
	public IConnector infOrEqTo(IColumn column) {
		this.updateLastCondition(LESS_OR_EQ, null, requireColumn(column));
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
		replaceLast(condition -> condition.withOperator(EQ));
		return this;
	}

	@Override
	public IAggregator supTo() {
		replaceLast(condition -> condition.withOperator(MORE));
		return this;
	}

	@Override
	public IAggregator infTo() {
		replaceLast(condition -> condition.withOperator(LESS));
		return this;
	}

	@Override
	public IAggregator supOrEqTo() {
		replaceLast(condition -> condition.withOperator(MORE_OR_EQ));
		return this;
	}

	@Override
	public IAggregator infOrEqTo() {
		replaceLast(condition -> condition.withOperator(LESS_OR_EQ));
		return this;
	}

	@Override
	public IAggregator in() {
		replaceLast(condition -> condition.withOperator(IN));
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
		replaceLast(condition -> condition.withLeftColumn(column));
		return this;
	}

	@Override
	public IWhere condition(ICondition condition) {
		filters.addLast(copyOf(condition));
		return this;
	}

	private void updateLastCondition(Operator operator, String... values) {
		replaceLast(condition -> applyValues(condition, resolveOperator(operator, values.length),
				Arrays.stream(values).map(ConditionValue::of).toList()));
	}

	private void updateLastCondition(Operator operator, Integer... values) {
		replaceLast(condition -> applyValues(condition, resolveOperator(operator, values.length),
				Arrays.stream(values).map(ConditionValue::of).toList()));
	}

	private void updateLastCondition(Operator operator, Double... values) {
		replaceLast(condition -> applyValues(condition, resolveOperator(operator, values.length),
				Arrays.stream(values).map(ConditionValue::of).toList()));
	}

	private void updateLastCondition(Operator operator, Date... values) {
		replaceLast(condition -> applyValues(condition, resolveOperator(operator, values.length),
				Arrays.stream(values).map(ConditionValue::of).toList()));
	}

	private Condition applyValues(Condition condition, Operator operator, List<ConditionValue> newValues) {
		Condition updated = condition;
		if (operator != null) {
			updated = updated.withOperator(operator);
		}
		if (!newValues.isEmpty()) {
			updated = updated.appendValues(newValues);
		}
		return updated;
	}

	private IColumn requireColumn(IColumn column) {
		QueryValidation.requireTable(column, "Column must belong to a table for WHERE clause");
		return column;
	}

	private void updateLastCondition(Operator operator, AggregateOperator aggregate, IColumn column) {
		replaceLast(condition -> {
			Condition updated = condition;
			if (operator != null) {
				updated = updated.withOperator(operator);
			}
			if (aggregate != null) {
				updated = updated.withNextAggregate(aggregate);
			}
			if (column != null) {
				updated = updated.withNextColumn(column);
			}
			return updated;
		});
	}

	private Condition copyOf(ICondition condition) {
		if (condition instanceof Condition concrete) {
			return concrete;
		}
		Condition.Builder builder = Condition.builder();
		if (condition.getStartOperator() != null) {
			builder.startOp(condition.getStartOperator());
		}
		if (condition.getLeft() != null) {
			if (condition.getLeftAgg() != null) {
				builder.leftColumn(condition.getLeftAgg(), condition.getLeft());
			} else {
				builder.leftColumn(condition.getLeft());
			}
		}
		if (condition.getRight() != null) {
			if (condition.getRightAgg() != null) {
				builder.rightColumn(condition.getRightAgg(), condition.getRight());
			} else {
				builder.rightColumn(condition.getRight());
			}
		}
		if (condition.getOperator() != null) {
			builder.comparisonOp(condition.getOperator());
		}
		condition.values().forEach(value -> {
			switch (value.type()) {
			case TY_STR -> builder.value((String) value.value());
			case TY_INT -> builder.value((Integer) value.value());
			case TY_DBL -> builder.value((Double) value.value());
			case TY_DATE -> builder.value((Date) value.value());
			default -> builder.value(String.valueOf(value.value()));
			}
		});
		return builder.build();
	}

	private Condition lastCondition() {
		if (filters.isEmpty()) {
			throw new IllegalStateException("Cannot apply operators without a starting condition. Call where(...) first.");
		}
		return filters.getLast();
	}

	private void replaceLast(UnaryOperator<Condition> mutator) {
		Condition current = lastCondition();
		int lastIndex = filters.size() - 1;
		filters.set(lastIndex, mutator.apply(current));
	}

	private Operator resolveOperator(Operator operator, int valueCount) {
		if (operator == EQ && valueCount > 1) {
			return IN;
		}
		return operator;
	}
}
