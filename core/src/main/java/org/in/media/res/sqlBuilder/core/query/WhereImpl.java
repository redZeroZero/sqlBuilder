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

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.query.Aggregator;
import org.in.media.res.sqlBuilder.api.query.Comparator;
import org.in.media.res.sqlBuilder.api.query.Condition;
import org.in.media.res.sqlBuilder.api.query.ConditionValue;
import org.in.media.res.sqlBuilder.api.query.Connector;
import org.in.media.res.sqlBuilder.api.query.Dialect;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.api.query.Where;
import org.in.media.res.sqlBuilder.api.query.WhereTranspiler;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.Operator;
import org.in.media.res.sqlBuilder.core.query.factory.TranspilerFactory;
import org.in.media.res.sqlBuilder.core.query.predicate.ClauseConditionBuffer;
import org.in.media.res.sqlBuilder.core.query.util.SqlEscapers;

public class WhereImpl implements Where {

	private final List<Condition> filters = new ArrayList<>();

	private final ClauseConditionBuffer buffer = new ClauseConditionBuffer(filters,
			"Cannot apply operators without a starting condition. Call where(...) first.");

	private final WhereTranspiler whereTranspiler = TranspilerFactory.instanciateWhereTranspiler();

	private final Dialect dialect;

	public WhereImpl(Dialect dialect) {
		this.dialect = java.util.Objects.requireNonNull(dialect, "dialect");
	}

	@Override
	public String transpile() {
		return this.whereTranspiler.transpile(this);
	}

	@Override
	public List<Condition> conditions() {
		return buffer.snapshot();
	}

	@Override
	public Where where(Column column) {
		buffer.add(ConditionImpl.builder().leftColumn(requireColumn(column)).build(), null);
		return this;
	}

	@Override
	public Where and(Column column) {
		buffer.add(ConditionImpl.builder().and().leftColumn(requireColumn(column)).build(), null);
		return this;
	}

	@Override
	public Where or(Column column) {
		buffer.add(ConditionImpl.builder().or().leftColumn(requireColumn(column)).build(), null);
		return this;
	}

	@Override
	public Where and() {
		buffer.add(ConditionImpl.builder().and().build(), null);
		return this;
	}

	@Override
	public Where or() {
		buffer.add(ConditionImpl.builder().or().build(), null);
		return this;
	}

	@Override
	public Connector eq(Column column) {
		this.updateLastCondition(EQ, null, requireColumn(column));
		return this;
	}

	@Override
	public Connector notEq(Column column) {
		this.updateLastCondition(Operator.NOT_EQ, null, requireColumn(column));
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
	public Connector notEq(String value) {
		this.updateLastCondition(Operator.NOT_EQ, value);
		return this;
	}

	@Override
	public Connector eq(SqlParameter<?> parameter) {
		this.updateLastCondition(EQ, parameter);
		return this;
	}

	@Override
	public Connector notEq(SqlParameter<?> parameter) {
		this.updateLastCondition(Operator.NOT_EQ, parameter);
		return this;
	}

	@Override
	public Connector like(String value) {
		this.updateLastCondition(Operator.LIKE, SqlEscapers.escapeLikePattern(value, dialect.likeEscapeChar()));
		return this;
	}

	@Override
	public Connector notLike(String value) {
		this.updateLastCondition(Operator.NOT_LIKE, SqlEscapers.escapeLikePattern(value, dialect.likeEscapeChar()));
		return this;
	}


	@Override
	public Connector between(String lower, String upper) {
		this.updateBetween(ConditionValue.of(lower), ConditionValue.of(upper));
		return this;
	}

	@Override
	public Connector between(SqlParameter<?> lower, SqlParameter<?> upper) {
		this.updateBetween(ConditionValue.of(lower), ConditionValue.of(upper));
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
	public Connector supTo(SqlParameter<?> parameter) {
		this.updateLastCondition(MORE, parameter);
		return this;
	}

	@Override
	public Connector infTo(SqlParameter<?> parameter) {
		this.updateLastCondition(LESS, parameter);
		return this;
	}

	@Override
	public Connector supOrEqTo(SqlParameter<?> parameter) {
		this.updateLastCondition(MORE_OR_EQ, parameter);
		return this;
	}

	@Override
	public Connector infOrEqTo(SqlParameter<?> parameter) {
		this.updateLastCondition(LESS_OR_EQ, parameter);
		return this;
	}

	@Override
	public Connector in(String... value) {
		this.updateLastCondition(IN, value);
		return this;
	}

	@Override
	public Connector notIn(String... value) {
		this.updateLastCondition(Operator.NOT_IN, value);
		return this;
	}

	@Override
	public Connector eq(Integer value) {
		this.updateLastCondition(EQ, value);
		return this;
	}

	@Override
	public Connector notEq(Integer value) {
		this.updateLastCondition(Operator.NOT_EQ, value);
		return this;
	}

	@Override
	public Connector between(Integer lower, Integer upper) {
		this.updateBetween(ConditionValue.of(lower), ConditionValue.of(upper));
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
	public Connector notIn(Integer... value) {
		this.updateLastCondition(Operator.NOT_IN, value);
		return this;
	}

	@Override
	public Connector eq(Date value) {
		this.updateLastCondition(EQ, value);
		return this;
	}

	@Override
	public Connector notEq(Date value) {
		this.updateLastCondition(Operator.NOT_EQ, value);
		return this;
	}

	@Override
	public Connector between(Date lower, Date upper) {
		this.updateBetween(ConditionValue.of(lower), ConditionValue.of(upper));
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
	public Connector notIn(Date... value) {
		this.updateLastCondition(Operator.NOT_IN, value);
		return this;
	}

	@Override
	public Connector eq(Double value) {
		this.updateLastCondition(EQ, value);
		return this;
	}

	@Override
	public Connector notEq(Double value) {
		this.updateLastCondition(Operator.NOT_EQ, value);
		return this;
	}

	@Override
	public Connector between(Double lower, Double upper) {
		this.updateBetween(ConditionValue.of(lower), ConditionValue.of(upper));
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
	public Connector notIn(Double... value) {
		this.updateLastCondition(Operator.NOT_IN, value);
		return this;
	}

	@Override
	public Connector eq(Query subquery) {
		QueryValidation.requireScalarSubquery(subquery, "WHERE = subquery");
		this.updateLastCondition(Operator.EQ, ConditionValue.of(subquery));
		return this;
	}

	@Override
	public Connector notEq(Query subquery) {
		QueryValidation.requireScalarSubquery(subquery, "WHERE <> subquery");
		this.updateLastCondition(Operator.NOT_EQ, ConditionValue.of(subquery));
		return this;
	}

	@Override
	public Connector in(Query subquery) {
		QueryValidation.requireScalarSubquery(subquery, "WHERE IN (subquery)");
		this.updateLastCondition(Operator.IN, ConditionValue.of(subquery));
		return this;
	}

	@Override
	public Connector notIn(Query subquery) {
		QueryValidation.requireScalarSubquery(subquery, "WHERE NOT IN (subquery)");
		this.updateLastCondition(Operator.NOT_IN, ConditionValue.of(subquery));
		return this;
	}

	@Override
	public Connector supTo(Query subquery) {
		QueryValidation.requireScalarSubquery(subquery, "WHERE > subquery");
		this.updateLastCondition(Operator.MORE, ConditionValue.of(subquery));
		return this;
	}

	@Override
	public Connector infTo(Query subquery) {
		QueryValidation.requireScalarSubquery(subquery, "WHERE < subquery");
		this.updateLastCondition(Operator.LESS, ConditionValue.of(subquery));
		return this;
	}

	@Override
	public Connector supOrEqTo(Query subquery) {
		QueryValidation.requireScalarSubquery(subquery, "WHERE >= subquery");
		this.updateLastCondition(Operator.MORE_OR_EQ, ConditionValue.of(subquery));
		return this;
	}

	@Override
	public Connector infOrEqTo(Query subquery) {
		QueryValidation.requireScalarSubquery(subquery, "WHERE <= subquery");
		this.updateLastCondition(Operator.LESS_OR_EQ, ConditionValue.of(subquery));
		return this;
	}

	@Override
	public Connector exists(Query subquery) {
		QueryValidation.requireAnyProjection(subquery, "WHERE EXISTS (subquery)");
		appendStandaloneCondition(Operator.EXISTS, ConditionValue.of(subquery));
		return this;
	}

	@Override
	public Connector notExists(Query subquery) {
		QueryValidation.requireAnyProjection(subquery, "WHERE NOT EXISTS (subquery)");
		appendStandaloneCondition(Operator.NOT_EXISTS, ConditionValue.of(subquery));
		return this;
	}

	@Override
	public Aggregator eq() {
		buffer.setOperator(EQ);
		return this;
	}

	@Override
	public Aggregator supTo() {
		buffer.setOperator(MORE);
		return this;
	}

	@Override
	public Aggregator infTo() {
		buffer.setOperator(LESS);
		return this;
	}

	@Override
	public Aggregator supOrEqTo() {
		buffer.setOperator(MORE_OR_EQ);
		return this;
	}

	@Override
	public Aggregator infOrEqTo() {
		buffer.setOperator(LESS_OR_EQ);
		return this;
	}

	@Override
	public Aggregator in() {
		buffer.setOperator(IN);
		return this;
	}

	@Override
	public Connector isNull() {
		updateLastCondition(Operator.IS_NULL);
		return this;
	}

	@Override
	public Connector isNotNull() {
		updateLastCondition(Operator.IS_NOT_NULL);
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
		buffer.replaceLast(condition -> condition.withLeftColumn(column));
		return this;
	}

	@Override
	public Where condition(Condition condition) {
		buffer.add(condition, null);
		return this;
	}

	@Override
	public Where and(Condition condition) {
		buffer.add(condition, Operator.AND);
		return this;
	}

	@Override
	public Where or(Condition condition) {
		buffer.add(condition, Operator.OR);
		return this;
	}

	Where aggregate(AggregateOperator aggregate, Column column) {
		buffer.replaceLast(condition -> condition.withLeftAggregate(aggregate).withLeftColumn(requireColumn(column)));
		return this;
	}

	private void updateLastCondition(Operator operator, String... values) {
		requireValues(operator, values.length);
		buffer.updateLast(resolveOperator(operator, values.length),
				Arrays.stream(values).map(ConditionValue::of).toList());
	}

	private void updateLastCondition(Operator operator, Integer... values) {
		requireValues(operator, values.length);
		buffer.updateLast(resolveOperator(operator, values.length),
				Arrays.stream(values).map(ConditionValue::of).toList());
	}

	private void updateLastCondition(Operator operator, Double... values) {
		requireValues(operator, values.length);
		buffer.updateLast(resolveOperator(operator, values.length),
				Arrays.stream(values).map(ConditionValue::of).toList());
	}

	private void updateLastCondition(Operator operator, Date... values) {
		requireValues(operator, values.length);
		buffer.updateLast(resolveOperator(operator, values.length),
				Arrays.stream(values).map(ConditionValue::of).toList());
	}

	private void updateLastCondition(Operator operator, ConditionValue value) {
		buffer.updateLast(resolveOperator(operator, 1), List.of(value));
	}

	private void updateLastCondition(Operator operator, SqlParameter<?> parameter) {
		updateLastCondition(operator, ConditionValue.of(parameter));
	}

	private void updateLastCondition(Operator operator) {
		buffer.setOperator(operator);
	}

	private void appendStandaloneCondition(Operator operator, ConditionValue value) {
		buffer.appendStandalone(operator, value);
	}

	private void updateBetween(ConditionValue lower, ConditionValue upper) {
		buffer.updateLast(Operator.BETWEEN, List.of(lower, upper));
	}

	private Column requireColumn(Column column) {
		QueryValidation.requireTable(column, "Column must belong to a table for WHERE clause");
		return column;
	}

	private void requireValues(Operator operator, int valueCount) {
		if (valueCount == 0) {
			String label = operator != null ? operator.name() : "condition";
			throw new IllegalArgumentException(label + " requires at least one value");
		}
	}

	private void updateLastCondition(Operator operator, AggregateOperator aggregate, Column column) {
		buffer.replaceLast(condition -> {
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

	private Operator resolveOperator(Operator operator, int valueCount) {
		if (operator == EQ && valueCount > 1) {
			return IN;
		}
		if (operator == Operator.EXISTS || operator == Operator.NOT_EXISTS) {
			return operator;
		}
		return operator;
	}
}
