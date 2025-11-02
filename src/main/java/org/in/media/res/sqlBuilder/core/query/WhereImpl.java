package org.in.media.res.sqlBuilder.core.query;

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
import org.in.media.res.sqlBuilder.api.query.ConditionValue;
import org.in.media.res.sqlBuilder.core.query.factory.WhereTranspilerFactory;
import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.query.Aggregator;
import org.in.media.res.sqlBuilder.api.query.Comparator;
import org.in.media.res.sqlBuilder.api.query.Condition;
import org.in.media.res.sqlBuilder.api.query.Connector;
import org.in.media.res.sqlBuilder.api.query.Where;
import org.in.media.res.sqlBuilder.api.query.WhereTranspiler;

public class WhereImpl implements Where {

	private final List<Condition> filters = new ArrayList<>();

	private final WhereTranspiler whereTranspiler = WhereTranspilerFactory.instanciateWhereTranspiler();

	@Override
	public String transpile() {
		return this.whereTranspiler.transpile(this);
	}

	@Override
	public List<Condition> conditions() {
		return List.copyOf(filters);
	}

	@Override
	public Where where(Column column) {
		filters.addLast(ConditionImpl.builder().leftColumn(requireColumn(column)).build());
		return this;
	}

	@Override
	public Where and(Column column) {
		filters.addLast(ConditionImpl.builder().and().leftColumn(requireColumn(column)).build());
		return this;
	}

	@Override
	public Where or(Column column) {
		filters.addLast(ConditionImpl.builder().or().leftColumn(requireColumn(column)).build());
		return this;
	}

	@Override
	public Where and() {
		filters.addLast(ConditionImpl.builder().and().build());
		return this;
	}

	@Override
	public Where or() {
		filters.addLast(ConditionImpl.builder().or().build());
		return this;
	}

	@Override
	public Connector eq(Column column) {
		this.updateLastCondition(EQ, null, requireColumn(column));
		return this;
	}

	@Override
	public Connector supTo(Column column) {
		this.updateLastCondition(MORE, null, requireColumn(column));
		return this;
	}

	@Override
	public Connector infTo(Column column) {
		this.updateLastCondition(LESS, null, requireColumn(column));
		return this;
	}

	@Override
	public Connector supOrEqTo(Column column) {
		this.updateLastCondition(MORE_OR_EQ, null, requireColumn(column));
		return this;
	}

	@Override
	public Connector infOrEqTo(Column column) {
		this.updateLastCondition(LESS_OR_EQ, null, requireColumn(column));
		return this;
	}

	@Override
	public Connector eq(String value) {
		this.updateLastCondition(EQ, value);
		return this;
	}

	@Override
	public Connector supTo(String value) {
		this.updateLastCondition(MORE, value);
		return this;
	}

	@Override
	public Connector infTo(String value) {
		this.updateLastCondition(LESS, value);
		return this;
	}

	@Override
	public Connector supOrEqTo(String value) {
		this.updateLastCondition(MORE_OR_EQ, value);
		return this;
	}

	@Override
	public Connector infOrEqTo(String value) {
		this.updateLastCondition(LESS_OR_EQ, value);
		return this;
	}

	@Override
	public Connector in(String... value) {
		this.updateLastCondition(IN, value);
		return this;
	}

	@Override
	public Connector eq(Integer value) {
		this.updateLastCondition(EQ, value);
		return this;
	}

	@Override
	public Connector supTo(Integer value) {
		this.updateLastCondition(MORE, value);
		return this;
	}

	@Override
	public Connector infTo(Integer value) {
		this.updateLastCondition(LESS, value);
		return this;
	}

	@Override
	public Connector supOrEqTo(Integer value) {
		this.updateLastCondition(MORE_OR_EQ, value);
		return this;
	}

	@Override
	public Connector infOrEqTo(Integer value) {
		this.updateLastCondition(LESS_OR_EQ, value);
		return this;
	}

	@Override
	public Connector in(Integer... value) {
		this.updateLastCondition(IN, value);
		return this;
	}

	@Override
	public Connector eq(Date value) {
		this.updateLastCondition(EQ, value);
		return this;
	}

	@Override
	public Connector supTo(Date value) {
		this.updateLastCondition(MORE, value);
		return this;
	}

	@Override
	public Connector infTo(Date value) {
		this.updateLastCondition(LESS, value);
		return this;
	}

	@Override
	public Connector supOrEqTo(Date value) {
		this.updateLastCondition(MORE_OR_EQ, value);
		return this;
	}

	@Override
	public Connector infOrEqTo(Date value) {
		this.updateLastCondition(LESS_OR_EQ, value);
		return this;
	}

	@Override
	public Connector in(Date... value) {
		this.updateLastCondition(IN, value);
		return this;
	}

	@Override
	public Connector eq(Double value) {
		this.updateLastCondition(EQ, value);
		return this;
	}

	@Override
	public Connector supTo(Double value) {
		this.updateLastCondition(MORE, value);
		return this;
	}

	@Override
	public Connector infTo(Double value) {
		this.updateLastCondition(LESS, value);
		return this;
	}

	@Override
	public Connector supOrEqTo(Double value) {
		this.updateLastCondition(MORE_OR_EQ, value);
		return this;
	}

	@Override
	public Connector infOrEqTo(Double value) {
		this.updateLastCondition(LESS_OR_EQ, value);
		return this;
	}

	@Override
	public Connector in(Double... value) {
		this.updateLastCondition(IN, value);
		return this;
	}

	@Override
	public Aggregator eq() {
		replaceLast(condition -> condition.withOperator(EQ));
		return this;
	}

	@Override
	public Aggregator supTo() {
		replaceLast(condition -> condition.withOperator(MORE));
		return this;
	}

	@Override
	public Aggregator infTo() {
		replaceLast(condition -> condition.withOperator(LESS));
		return this;
	}

	@Override
	public Aggregator supOrEqTo() {
		replaceLast(condition -> condition.withOperator(MORE_OR_EQ));
		return this;
	}

	@Override
	public Aggregator infOrEqTo() {
		replaceLast(condition -> condition.withOperator(LESS_OR_EQ));
		return this;
	}

	@Override
	public Aggregator in() {
		replaceLast(condition -> condition.withOperator(IN));
		return this;
	}

	@Override
	public Comparator min(Column column) {
		this.updateLastCondition(null, MIN, column);
		return this;
	}

	@Override
	public Comparator max(Column column) {
		this.updateLastCondition(null, MAX, column);
		return this;
	}

	@Override
	public Comparator sum(Column column) {
		this.updateLastCondition(null, SUM, column);
		return this;
	}

	@Override
	public Comparator avg(Column column) {
		this.updateLastCondition(null, AVG, column);
		return this;
	}

	@Override
	public Comparator col(Column column) {
		replaceLast(condition -> condition.withLeftColumn(column));
		return this;
	}

	@Override
	public Where condition(Condition condition) {
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

private ConditionImpl applyValues(ConditionImpl condition, Operator operator, List<ConditionValue> newValues) {
	ConditionImpl updated = condition;
	if (operator != null) {
		updated = updated.withOperator(operator);
	}
	if (!newValues.isEmpty()) {
		updated = updated.appendValues(newValues);
	}
	return updated;
}

	private Column requireColumn(Column column) {
		QueryValidation.requireTable(column, "Column must belong to a table for WHERE clause");
		return column;
	}

	private void updateLastCondition(Operator operator, AggregateOperator aggregate, Column column) {
		replaceLast(condition -> {
			ConditionImpl updated = condition;
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

	private ConditionImpl copyOf(Condition condition) {
		if (condition instanceof ConditionImpl concrete) {
			return concrete;
		}
		ConditionImpl.Builder builder = ConditionImpl.builder();
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

	private ConditionImpl lastCondition() {
		if (filters.isEmpty()) {
			throw new IllegalStateException("Cannot apply operators without a starting condition. Call where(...) first.");
		}
		return (ConditionImpl) filters.getLast();
	}

	private void replaceLast(UnaryOperator<ConditionImpl> mutator) {
		ConditionImpl current = lastCondition();
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
