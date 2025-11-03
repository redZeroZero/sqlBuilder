package org.in.media.res.sqlBuilder.core.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.Operator;
import org.in.media.res.sqlBuilder.api.query.ConditionValue;
import org.in.media.res.sqlBuilder.core.query.factory.TranspilerFactory;
import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.query.Condition;
import org.in.media.res.sqlBuilder.api.query.Having;
import org.in.media.res.sqlBuilder.api.query.HavingBuilder;
import org.in.media.res.sqlBuilder.api.query.HavingTranspiler;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.core.query.ConditionGroupBuilder.ConditionGroup;

public class HavingImpl implements Having {

    private final List<Condition> conditions = new ArrayList<>();

	private final HavingTranspiler havingTranspiler = TranspilerFactory.instanciateHavingTranspiler();

    @Override
    public String transpile() {
        return conditions.isEmpty() ? "" : havingTranspiler.transpile(this);
    }

    @Override
    public void reset() {
        conditions.clear();
    }

    @Override
    public Having having(Condition condition) {
        conditions.addLast(normalize(condition, null));
        return this;
    }

    @Override
    public Having and(Condition condition) {
        conditions.addLast(normalize(condition, Operator.AND));
        return this;
    }

    @Override
    public Having or(Condition condition) {
        conditions.addLast(normalize(condition, Operator.OR));
        return this;
    }

    @Override
    public HavingBuilder having(Column column) {
        conditions.addLast(ConditionImpl.builder().leftColumn(requireColumn(column)).build());
        return new BuilderDelegate();
    }

    @Override
    public List<Condition> havingConditions() {
        return List.copyOf(conditions);
    }

	private Condition normalize(Condition condition, Operator startOperator) {
		Objects.requireNonNull(condition, "condition");
		if (condition instanceof ConditionGroup group) {
			return startOperator != null ? group.withStartOperator(startOperator) : group;
		}
		ConditionImpl normalized = condition instanceof ConditionImpl concrete ? concrete : ConditionImpl.copyOf(condition);
		return startOperator != null ? normalized.withStartOperator(startOperator) : normalized;
	}

    private ConditionImpl lastCondition() {
        if (conditions.isEmpty() || !(conditions.getLast() instanceof ConditionImpl condition)) {
            throw new IllegalStateException("Cannot apply operators without a HAVING condition. Call having(...) first.");
        }
        return condition;
    }

	private void replaceLast(UnaryOperator<ConditionImpl> mutator) {
		ConditionImpl current = lastCondition();
		int lastIndex = conditions.size() - 1;
		conditions.set(lastIndex, mutator.apply(current));
	}

	private void appendStandaloneCondition(Operator operator, ConditionValue value) {
		if (conditions.isEmpty()) {
			conditions.addLast(ConditionImpl.builder().comparisonOp(operator).value(value).build());
			return;
		}
		ConditionImpl current = lastCondition();
		if (current.getOperator() == null && current.getLeft() == null && current.values().isEmpty()) {
			int lastIndex = conditions.size() - 1;
			conditions.set(lastIndex, current.withOperator(operator).appendValue(value));
		} else {
			conditions.addLast(ConditionImpl.builder().and().comparisonOp(operator).value(value).build());
		}
	}

    private ConditionValue numericValue(Number value) {
        return (value instanceof Double || value instanceof Float)
                ? ConditionValue.of(value.doubleValue())
                : ConditionValue.of(value.intValue());
    }

    private void setAggregate(AggregateOperator aggregate, Column column) {
        replaceLast(condition -> condition.withLeftAggregate(aggregate).withLeftColumn(requireColumn(column)));
    }

	private class BuilderDelegate implements HavingBuilder {

        @Override
        public Having eq(String value) {
            replaceLast(condition -> condition.withOperator(Operator.EQ)
                    .appendValue(ConditionValue.of(value)));
            return HavingImpl.this;
        }

        @Override
        public Having eq(Number value) {
            replaceLast(condition -> condition.withOperator(Operator.EQ)
                    .appendValue(numericValue(value)));
            return HavingImpl.this;
        }

        @Override
        public Having eq(Date value) {
            replaceLast(condition -> condition.withOperator(Operator.EQ)
                    .appendValue(ConditionValue.of(value)));
            return HavingImpl.this;
        }

        @Override
        public Having notEq(String value) {
            replaceLast(condition -> condition.withOperator(Operator.NOT_EQ)
                    .appendValue(ConditionValue.of(value)));
            return HavingImpl.this;
        }

        @Override
        public Having notEq(Number value) {
            replaceLast(condition -> condition.withOperator(Operator.NOT_EQ)
                    .appendValue(numericValue(value)));
            return HavingImpl.this;
        }

        @Override
        public Having notEq(Date value) {
            replaceLast(condition -> condition.withOperator(Operator.NOT_EQ)
                    .appendValue(ConditionValue.of(value)));
            return HavingImpl.this;
        }

        @Override
        public Having in(String... values) {
            replaceLast(condition -> condition.withOperator(Operator.IN)
                    .appendValues(Arrays.stream(values).map(ConditionValue::of).toList()));
            return HavingImpl.this;
        }

        @Override
        public Having in(Number... values) {
            replaceLast(condition -> condition.withOperator(Operator.IN)
                    .appendValues(Arrays.stream(values).map(HavingImpl.this::numericValue).toList()));
            return HavingImpl.this;
        }

        @Override
        public Having in(Date... values) {
            replaceLast(condition -> condition.withOperator(Operator.IN)
                    .appendValues(Arrays.stream(values).map(ConditionValue::of).toList()));
            return HavingImpl.this;
        }

        @Override
        public Having notIn(String... values) {
            replaceLast(condition -> condition.withOperator(Operator.NOT_IN)
                    .appendValues(Arrays.stream(values).map(ConditionValue::of).toList()));
            return HavingImpl.this;
        }

        @Override
        public Having notIn(Number... values) {
            replaceLast(condition -> condition.withOperator(Operator.NOT_IN)
                    .appendValues(Arrays.stream(values).map(HavingImpl.this::numericValue).toList()));
            return HavingImpl.this;
        }

        @Override
		public Having notIn(Date... values) {
			replaceLast(condition -> condition.withOperator(Operator.NOT_IN)
				.appendValues(Arrays.stream(values).map(ConditionValue::of).toList()));
			return HavingImpl.this;
		}

		@Override
		public Having eq(Query subquery) {
			QueryValidation.requireScalarSubquery(subquery, "HAVING = subquery");
			replaceLast(condition -> condition.withOperator(Operator.EQ)
				.appendValue(ConditionValue.of(subquery)));
			return HavingImpl.this;
		}

		@Override
		public Having notEq(Query subquery) {
			QueryValidation.requireScalarSubquery(subquery, "HAVING <> subquery");
			replaceLast(condition -> condition.withOperator(Operator.NOT_EQ)
				.appendValue(ConditionValue.of(subquery)));
			return HavingImpl.this;
		}

		@Override
		public Having in(Query subquery) {
			QueryValidation.requireScalarSubquery(subquery, "HAVING IN (subquery)");
			replaceLast(condition -> condition.withOperator(Operator.IN)
				.appendValue(ConditionValue.of(subquery)));
			return HavingImpl.this;
		}

		@Override
		public Having notIn(Query subquery) {
			QueryValidation.requireScalarSubquery(subquery, "HAVING NOT IN (subquery)");
			replaceLast(condition -> condition.withOperator(Operator.NOT_IN)
				.appendValue(ConditionValue.of(subquery)));
			return HavingImpl.this;
		}

		@Override
		public Having supTo(Query subquery) {
			QueryValidation.requireScalarSubquery(subquery, "HAVING > subquery");
			replaceLast(condition -> condition.withOperator(Operator.MORE)
				.appendValue(ConditionValue.of(subquery)));
			return HavingImpl.this;
		}

		@Override
		public Having supOrEqTo(Query subquery) {
			QueryValidation.requireScalarSubquery(subquery, "HAVING >= subquery");
			replaceLast(condition -> condition.withOperator(Operator.MORE_OR_EQ)
				.appendValue(ConditionValue.of(subquery)));
			return HavingImpl.this;
		}

		@Override
		public Having infTo(Query subquery) {
			QueryValidation.requireScalarSubquery(subquery, "HAVING < subquery");
			replaceLast(condition -> condition.withOperator(Operator.LESS)
				.appendValue(ConditionValue.of(subquery)));
			return HavingImpl.this;
		}

		@Override
		public Having infOrEqTo(Query subquery) {
			QueryValidation.requireScalarSubquery(subquery, "HAVING <= subquery");
			replaceLast(condition -> condition.withOperator(Operator.LESS_OR_EQ)
				.appendValue(ConditionValue.of(subquery)));
			return HavingImpl.this;
		}

		@Override
		public Having exists(Query subquery) {
			QueryValidation.requireAnyProjection(subquery, "HAVING EXISTS (subquery)");
			appendStandaloneCondition(Operator.EXISTS, ConditionValue.of(subquery));
			return HavingImpl.this;
		}

		@Override
		public Having notExists(Query subquery) {
			QueryValidation.requireAnyProjection(subquery, "HAVING NOT EXISTS (subquery)");
			appendStandaloneCondition(Operator.NOT_EXISTS, ConditionValue.of(subquery));
			return HavingImpl.this;
		}

        @Override
        public Having like(String value) {
            replaceLast(condition -> condition.withOperator(Operator.LIKE)
                    .appendValue(ConditionValue.of(value)));
            return HavingImpl.this;
        }

        @Override
        public Having notLike(String value) {
            replaceLast(condition -> condition.withOperator(Operator.NOT_LIKE)
                    .appendValue(ConditionValue.of(value)));
            return HavingImpl.this;
        }

        @Override
        public Having between(Number lower, Number upper) {
            replaceLast(condition -> condition.withOperator(Operator.BETWEEN)
                    .appendValues(Arrays.asList(numericValue(lower), numericValue(upper))));
            return HavingImpl.this;
        }

        @Override
        public Having between(Date lower, Date upper) {
            replaceLast(condition -> condition.withOperator(Operator.BETWEEN)
                    .appendValues(Arrays.asList(ConditionValue.of(lower), ConditionValue.of(upper))));
            return HavingImpl.this;
        }

        @Override
        public Having isNull() {
            replaceLast(condition -> condition.withOperator(Operator.IS_NULL));
            return HavingImpl.this;
        }

        @Override
        public Having isNotNull() {
            replaceLast(condition -> condition.withOperator(Operator.IS_NOT_NULL));
            return HavingImpl.this;
        }

        @Override
        public Having supTo(Number value) {
            replaceLast(condition -> condition.withOperator(Operator.MORE)
                    .appendValue(numericValue(value)));
            return HavingImpl.this;
        }

        @Override
        public Having supOrEqTo(Number value) {
            replaceLast(condition -> condition.withOperator(Operator.MORE_OR_EQ)
                    .appendValue(numericValue(value)));
            return HavingImpl.this;
        }

        @Override
        public Having infTo(Number value) {
            replaceLast(condition -> condition.withOperator(Operator.LESS)
                    .appendValue(numericValue(value)));
            return HavingImpl.this;
        }

        @Override
        public Having infOrEqTo(Number value) {
            replaceLast(condition -> condition.withOperator(Operator.LESS_OR_EQ)
                    .appendValue(numericValue(value)));
            return HavingImpl.this;
        }

        @Override
        public Having supTo(Column column) {
            replaceLast(condition -> condition.withOperator(Operator.MORE)
                    .withRightColumn(requireColumn(column)));
            return HavingImpl.this;
        }

        @Override
        public Having supOrEqTo(Column column) {
            replaceLast(condition -> condition.withOperator(Operator.MORE_OR_EQ)
                    .withRightColumn(requireColumn(column)));
            return HavingImpl.this;
        }

        @Override
        public Having infTo(Column column) {
            replaceLast(condition -> condition.withOperator(Operator.LESS)
                    .withRightColumn(requireColumn(column)));
            return HavingImpl.this;
        }

        @Override
        public Having infOrEqTo(Column column) {
            replaceLast(condition -> condition.withOperator(Operator.LESS_OR_EQ)
                    .withRightColumn(requireColumn(column)));
            return HavingImpl.this;
        }

        @Override
        public Having notEq(Column column) {
            replaceLast(condition -> condition.withOperator(Operator.NOT_EQ)
                    .withRightColumn(requireColumn(column)));
            return HavingImpl.this;
        }

        @Override
        public HavingBuilder and(Column column) {
            conditions.addLast(ConditionImpl.builder().and().leftColumn(column).build());
            return this;
        }

        @Override
        public HavingBuilder or(Column column) {
            conditions.addLast(ConditionImpl.builder().or().leftColumn(requireColumn(column)).build());
            return this;
        }

        @Override
        public HavingBuilder min(Column column) {
            setAggregate(AggregateOperator.MIN, column);
            return this;
        }

        @Override
        public HavingBuilder max(Column column) {
            setAggregate(AggregateOperator.MAX, column);
            return this;
        }

        @Override
        public HavingBuilder sum(Column column) {
            setAggregate(AggregateOperator.SUM, column);
            return this;
        }

        @Override
        public HavingBuilder avg(Column column) {
            setAggregate(AggregateOperator.AVG, column);
            return this;
        }

        @Override
        public HavingBuilder col(Column column) {
            replaceLast(condition -> condition.withRightColumn(requireColumn(column)));
            return this;
        }
    }

    private Column requireColumn(Column column) {
        QueryValidation.requireTable(column, "Column must belong to a table for HAVING clause");
        return column;
    }
}
