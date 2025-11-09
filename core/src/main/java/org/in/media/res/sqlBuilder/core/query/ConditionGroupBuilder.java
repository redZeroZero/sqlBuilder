package org.in.media.res.sqlBuilder.core.query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.api.query.Condition;
import org.in.media.res.sqlBuilder.api.query.ConditionValue;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.Operator;
import org.in.media.res.sqlBuilder.core.model.ColumnRef;
import org.in.media.res.sqlBuilder.core.query.predicate.ConditionGroup;
import org.in.media.res.sqlBuilder.core.query.predicate.PredicateValues;
import org.in.media.res.sqlBuilder.core.query.util.SqlEscapers;

/**
 * Builder for grouped boolean expressions that can be used inside WHERE / HAVING clauses.
 */
public final class ConditionGroupBuilder implements Condition {

	private final List<Condition> conditions = new ArrayList<>();
	private final ConditionGroupBuilder parent;
	private final Operator parentConnector;
	private boolean closed;

	public ConditionGroupBuilder() {
		this(null, null);
	}

	private ConditionGroupBuilder(ConditionGroupBuilder parent, Operator parentConnector) {
		this.parent = parent;
		this.parentConnector = parentConnector;
	}

	public ConditionGroupBuilder where(Column column) {
		addCondition(ConditionImpl.builder().leftColumn(requireColumn(column)).build());
		return this;
	}

	public ConditionGroupBuilder where(TableDescriptor<?> descriptor) {
		return where(descriptor.column());
	}

	public ConditionGroupBuilder where(Condition condition) {
		addCondition(condition, null);
		return this;
	}

	public ConditionGroupBuilder group() {
		return beginNested(null);
	}

	public ConditionGroupBuilder andGroup() {
		return beginNested(Operator.AND);
	}

	public ConditionGroupBuilder orGroup() {
		return beginNested(Operator.OR);
	}

	public ConditionGroupBuilder endGroup() {
		if (parent == null)
			throw new IllegalStateException("Root group cannot be ended. Call build() instead.");
		if (closed)
			throw new IllegalStateException("Group already ended");
		ConditionGroup group = materialize();
		closed = true;
		parent.addCondition(group, parentConnector);
		return parent;
	}

	public ConditionGroupBuilder and(Column column) {
		addCondition(ConditionImpl.builder().and().leftColumn(requireColumn(column)).build());
		return this;
	}

	public ConditionGroupBuilder and(TableDescriptor<?> descriptor) {
		return and(descriptor.column());
	}

	public ConditionGroupBuilder and(Condition condition) {
		addCondition(condition, Operator.AND);
		return this;
	}

	public ConditionGroupBuilder and(Consumer<ConditionGroupBuilder> consumer) {
		return addGroup(Operator.AND, consumer);
	}

	public ConditionGroupBuilder or(Column column) {
		addCondition(ConditionImpl.builder().or().leftColumn(requireColumn(column)).build());
		return this;
	}

	public ConditionGroupBuilder or(TableDescriptor<?> descriptor) {
		return or(descriptor.column());
	}

	public ConditionGroupBuilder or(Condition condition) {
		addCondition(condition, Operator.OR);
		return this;
	}

	public ConditionGroupBuilder or(Consumer<ConditionGroupBuilder> consumer) {
		return addGroup(Operator.OR, consumer);
	}

	public ConditionGroupBuilder and() {
		addCondition(ConditionImpl.builder().and().build());
		return this;
	}

	public ConditionGroupBuilder or() {
		addCondition(ConditionImpl.builder().or().build());
		return this;
	}

	public ConditionGroupBuilder eq(String value) {
		updateLastCondition(Operator.EQ, value);
		return this;
	}

	public ConditionGroupBuilder eq(SqlParameter<?> parameter) {
		updateLastCondition(Operator.EQ, ConditionValue.of(parameter));
		return this;
	}

	public ConditionGroupBuilder eq(Integer value) {
		updateLastCondition(Operator.EQ, value);
		return this;
	}

	public ConditionGroupBuilder eq(Double value) {
		updateLastCondition(Operator.EQ, value);
		return this;
	}

	public ConditionGroupBuilder eq(Date value) {
		updateLastCondition(Operator.EQ, value);
		return this;
	}

	public ConditionGroupBuilder eq(Query subquery) {
		updateLastCondition(Operator.EQ, ConditionValue.of(subquery));
		return this;
	}

	public ConditionGroupBuilder notEq(String value) {
		updateLastCondition(Operator.NOT_EQ, value);
		return this;
	}

	public ConditionGroupBuilder notEq(SqlParameter<?> parameter) {
		updateLastCondition(Operator.NOT_EQ, ConditionValue.of(parameter));
		return this;
	}

	public ConditionGroupBuilder notEq(Integer value) {
		updateLastCondition(Operator.NOT_EQ, value);
		return this;
	}

	public ConditionGroupBuilder notEq(Double value) {
		updateLastCondition(Operator.NOT_EQ, value);
		return this;
	}

	public ConditionGroupBuilder notEq(Date value) {
		updateLastCondition(Operator.NOT_EQ, value);
		return this;
	}

	public ConditionGroupBuilder notEq(Query subquery) {
		updateLastCondition(Operator.NOT_EQ, ConditionValue.of(subquery));
		return this;
	}

	public ConditionGroupBuilder supTo(Integer value) {
		updateLastCondition(Operator.MORE, value);
		return this;
	}

	public ConditionGroupBuilder supTo(Double value) {
		updateLastCondition(Operator.MORE, value);
		return this;
	}

	public ConditionGroupBuilder supTo(Query subquery) {
		updateLastCondition(Operator.MORE, ConditionValue.of(subquery));
		return this;
	}

	public ConditionGroupBuilder supTo(SqlParameter<?> parameter) {
		updateLastCondition(Operator.MORE, ConditionValue.of(parameter));
		return this;
	}

	public ConditionGroupBuilder supOrEqTo(Integer value) {
		updateLastCondition(Operator.MORE_OR_EQ, value);
		return this;
	}

	public ConditionGroupBuilder supOrEqTo(Double value) {
		updateLastCondition(Operator.MORE_OR_EQ, value);
		return this;
	}

	public ConditionGroupBuilder supOrEqTo(Query subquery) {
		updateLastCondition(Operator.MORE_OR_EQ, ConditionValue.of(subquery));
		return this;
	}

	public ConditionGroupBuilder supOrEqTo(SqlParameter<?> parameter) {
		updateLastCondition(Operator.MORE_OR_EQ, ConditionValue.of(parameter));
		return this;
	}

	public ConditionGroupBuilder infTo(Integer value) {
		updateLastCondition(Operator.LESS, value);
		return this;
	}

	public ConditionGroupBuilder infTo(Double value) {
		updateLastCondition(Operator.LESS, value);
		return this;
	}

	public ConditionGroupBuilder infTo(Query subquery) {
		updateLastCondition(Operator.LESS, ConditionValue.of(subquery));
		return this;
	}

	public ConditionGroupBuilder infTo(SqlParameter<?> parameter) {
		updateLastCondition(Operator.LESS, ConditionValue.of(parameter));
		return this;
	}

	public ConditionGroupBuilder infOrEqTo(Integer value) {
		updateLastCondition(Operator.LESS_OR_EQ, value);
		return this;
	}

	public ConditionGroupBuilder infOrEqTo(Double value) {
		updateLastCondition(Operator.LESS_OR_EQ, value);
		return this;
	}

	public ConditionGroupBuilder infOrEqTo(Query subquery) {
		updateLastCondition(Operator.LESS_OR_EQ, ConditionValue.of(subquery));
		return this;
	}

	public ConditionGroupBuilder infOrEqTo(SqlParameter<?> parameter) {
		updateLastCondition(Operator.LESS_OR_EQ, ConditionValue.of(parameter));
		return this;
	}

	public ConditionGroupBuilder in(String... values) {
		updateLastCondition(Operator.IN, values);
		return this;
	}

	public ConditionGroupBuilder notIn(String... values) {
		updateLastCondition(Operator.NOT_IN, values);
		return this;
	}

	public ConditionGroupBuilder in(Integer... values) {
		updateLastCondition(Operator.IN, values);
		return this;
	}

	public ConditionGroupBuilder notIn(Integer... values) {
		updateLastCondition(Operator.NOT_IN, values);
		return this;
	}

	public ConditionGroupBuilder in(Double... values) {
		updateLastCondition(Operator.IN, values);
		return this;
	}

	public ConditionGroupBuilder notIn(Double... values) {
		updateLastCondition(Operator.NOT_IN, values);
		return this;
	}

	public ConditionGroupBuilder in(Query subquery) {
		updateLastCondition(Operator.IN, ConditionValue.of(subquery));
		return this;
	}

	public ConditionGroupBuilder notIn(Query subquery) {
		updateLastCondition(Operator.NOT_IN, ConditionValue.of(subquery));
		return this;
	}

	public ConditionGroupBuilder between(String lower, String upper) {
		updateBetween(ConditionValue.of(lower), ConditionValue.of(upper));
		return this;
	}

	public ConditionGroupBuilder between(SqlParameter<?> lower, SqlParameter<?> upper) {
		updateBetween(ConditionValue.of(lower), ConditionValue.of(upper));
		return this;
	}

	public ConditionGroupBuilder between(Integer lower, Integer upper) {
		updateBetween(ConditionValue.of(lower), ConditionValue.of(upper));
		return this;
	}

	public ConditionGroupBuilder between(Double lower, Double upper) {
		updateBetween(ConditionValue.of(lower), ConditionValue.of(upper));
		return this;
	}

	public ConditionGroupBuilder between(Date lower, Date upper) {
		updateBetween(ConditionValue.of(lower), ConditionValue.of(upper));
		return this;
	}

	public ConditionGroupBuilder like(String value) {
		updateLastCondition(Operator.LIKE, SqlEscapers.escapeLikePattern(value));
		return this;
	}

	public ConditionGroupBuilder like(SqlParameter<?> parameter) {
		updateLastCondition(Operator.LIKE, ConditionValue.of(parameter));
		return this;
	}

	public ConditionGroupBuilder notLike(String value) {
		updateLastCondition(Operator.NOT_LIKE, SqlEscapers.escapeLikePattern(value));
		return this;
	}

	public ConditionGroupBuilder notLike(SqlParameter<?> parameter) {
		updateLastCondition(Operator.NOT_LIKE, ConditionValue.of(parameter));
		return this;
	}

	public ConditionGroupBuilder isNull() {
		updateLastCondition(Operator.IS_NULL);
		return this;
	}

	public ConditionGroupBuilder isNotNull() {
		updateLastCondition(Operator.IS_NOT_NULL);
		return this;
	}

	public ConditionGroupBuilder exists(Query subquery) {
		QueryValidation.requireAnyProjection(subquery, "GROUP EXISTS (subquery)");
		appendStandaloneCondition(Operator.EXISTS, ConditionValue.of(subquery));
		return this;
	}

	public ConditionGroupBuilder notExists(Query subquery) {
		QueryValidation.requireAnyProjection(subquery, "GROUP NOT EXISTS (subquery)");
		appendStandaloneCondition(Operator.NOT_EXISTS, ConditionValue.of(subquery));
		return this;
	}

	public ConditionGroupBuilder min(Column column) {
		applyAggregate(AggregateOperator.MIN, requireColumn(column));
		return this;
	}

	public <T extends Comparable<? super T>> ConditionGroupBuilder min(ColumnRef<T> descriptor) {
		return min(descriptor.column());
	}

	public ConditionGroupBuilder min(TableDescriptor<?> descriptor) {
		return min(descriptor.column());
	}

	public ConditionGroupBuilder max(Column column) {
		applyAggregate(AggregateOperator.MAX, requireColumn(column));
		return this;
	}

	public <T extends Comparable<? super T>> ConditionGroupBuilder max(ColumnRef<T> descriptor) {
		return max(descriptor.column());
	}

	public ConditionGroupBuilder max(TableDescriptor<?> descriptor) {
		return max(descriptor.column());
	}

	public ConditionGroupBuilder sum(Column column) {
		applyAggregate(AggregateOperator.SUM, requireColumn(column));
		return this;
	}

	public <N extends Number> ConditionGroupBuilder sum(ColumnRef<N> descriptor) {
		return sum(descriptor.column());
	}

	public ConditionGroupBuilder sum(TableDescriptor<?> descriptor) {
		return sum(descriptor.column());
	}

	public ConditionGroupBuilder avg(Column column) {
		applyAggregate(AggregateOperator.AVG, requireColumn(column));
		return this;
	}

	public <N extends Number> ConditionGroupBuilder avg(ColumnRef<N> descriptor) {
		return avg(descriptor.column());
	}

	public ConditionGroupBuilder avg(TableDescriptor<?> descriptor) {
		return avg(descriptor.column());
	}

	public ConditionGroupBuilder col(Column column) {
		replaceLast(condition -> condition.withLeftColumn(requireColumn(column)));
		return this;
	}

	public ConditionGroupBuilder col(TableDescriptor<?> descriptor) {
		return col(descriptor.column());
	}

	public ConditionGroupBuilder andGroup(Consumer<ConditionGroupBuilder> consumer) {
		return addGroup(Operator.AND, consumer);
	}

	public ConditionGroupBuilder orGroup(Consumer<ConditionGroupBuilder> consumer) {
		return addGroup(Operator.OR, consumer);
	}

	public ConditionGroupBuilder where(Consumer<ConditionGroupBuilder> consumer) {
		return addGroup(null, consumer);
	}

	public ConditionGroup build() {
		if (parent != null)
			throw new IllegalStateException("Call endGroup() on nested condition groups");
		return materialize();
	}

	@Override
	public List<ConditionValue> values() {
		return build().values();
	}

	@Override
	public Column getLeft() {
		return build().getLeft();
	}

	@Override
	public Column getRight() {
		return build().getRight();
	}

	@Override
	public Operator getStartOperator() {
		return build().getStartOperator();
	}

	@Override
	public Operator getOperator() {
		return build().getOperator();
	}

	@Override
	public AggregateOperator getLeftAgg() {
		return build().getLeftAgg();
	}

	@Override
	public AggregateOperator getRightAgg() {
		return build().getRightAgg();
	}

	@Override
	public String transpile() {
		return build().transpile();
	}

	private ConditionGroup materialize() {
		if (conditions.isEmpty()) {
			throw new IllegalStateException("Condition group must contain at least one predicate");
		}
		return new ConditionGroup(List.copyOf(conditions), null);
	}

	private ConditionGroupBuilder addGroup(Operator connector, Consumer<ConditionGroupBuilder> consumer) {
		Objects.requireNonNull(consumer, "consumer");
		ConditionGroupBuilder nested = beginNested(connector);
		consumer.accept(nested);
		nested.endGroup();
		return this;
	}

	private ConditionGroupBuilder beginNested(Operator connector) {
		return new ConditionGroupBuilder(this, connector);
	}

	private void addCondition(Condition condition) {
		conditions.add(normalize(condition, null));
	}

	private void addCondition(Condition condition, Operator startOperator) {
		conditions.add(normalize(condition, startOperator));
	}

	private Condition normalize(Condition condition, Operator startOperator) {
		Objects.requireNonNull(condition, "condition");
		if (condition instanceof org.in.media.res.sqlBuilder.core.query.predicate.ParameterCondition parameterCondition) {
			return startOperator != null ? parameterCondition.withStartOperator(startOperator) : parameterCondition;
		}
		if (condition instanceof ConditionGroupBuilder builder) {
			ConditionGroup group = builder.build();
			return startOperator != null ? group.withStartOperator(startOperator) : group;
		}
		if (condition instanceof ConditionGroup group) {
			return startOperator != null ? group.withStartOperator(startOperator) : group;
		}
		ConditionImpl normalized = condition instanceof ConditionImpl concrete ? concrete : ConditionImpl.copyOf(condition);
		return startOperator != null ? normalized.withStartOperator(startOperator) : normalized;
	}

	private void updateLastCondition(Operator operator, String... values) {
		PredicateValues.Result update = PredicateValues.strings(operator, values);
		replaceLast(condition -> applyValues(condition, update.operator(), update.values()));
	}

	private void updateLastCondition(Operator operator, Integer... values) {
		PredicateValues.Result update = PredicateValues.integers(operator, values);
		replaceLast(condition -> applyValues(condition, update.operator(), update.values()));
	}

	private void updateLastCondition(Operator operator, Double... values) {
		PredicateValues.Result update = PredicateValues.doubles(operator, values);
		replaceLast(condition -> applyValues(condition, update.operator(), update.values()));
	}

	private void updateLastCondition(Operator operator, Date... values) {
		PredicateValues.Result update = PredicateValues.dates(operator, values);
		replaceLast(condition -> applyValues(condition, update.operator(), update.values()));
	}

	private void updateLastCondition(Operator operator, ConditionValue value) {
		replaceLast(condition -> applyValues(condition, operator, List.of(value)));
	}

	private void updateLastCondition(Operator operator) {
		replaceLast(condition -> condition.withOperator(operator));
	}

	private void applyAggregate(AggregateOperator aggregate, Column column) {
		replaceLast(condition -> condition.withLeftAggregate(aggregate).withLeftColumn(column));
	}

	private void appendStandaloneCondition(Operator operator, ConditionValue value) {
		if (conditions.isEmpty()) {
			conditions.add(ConditionImpl.builder().comparisonOp(operator).value(value).build());
			return;
		}
		Condition last = conditions.get(conditions.size() - 1);
		if (last instanceof ConditionImpl impl && impl.getOperator() == null && impl.getLeft() == null
				&& impl.values().isEmpty()) {
			conditions.set(conditions.size() - 1, impl.withOperator(operator).appendValue(value));
		} else {
			conditions.add(ConditionImpl.builder().and().comparisonOp(operator).value(value).build());
		}
	}

	private void updateBetween(ConditionValue lower, ConditionValue upper) {
		replaceLast(condition -> condition.withOperator(Operator.BETWEEN).appendValues(List.of(lower, upper)));
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

	private ConditionImpl lastCondition() {
		if (conditions.isEmpty() || !(conditions.get(conditions.size() - 1) instanceof ConditionImpl condition)) {
			throw new IllegalStateException("Cannot apply operators without a starting condition. Call where(...) first.");
		}
		return condition;
	}

	private void replaceLast(UnaryOperator<ConditionImpl> mutator) {
		ConditionImpl current = lastCondition();
		conditions.set(conditions.size() - 1, mutator.apply(current));
	}

	private Column requireColumn(Column column) {
		QueryValidation.requireTable(column, "Column must belong to a table for grouped condition");
		return column;
	}

}
