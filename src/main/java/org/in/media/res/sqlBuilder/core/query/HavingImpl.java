package org.in.media.res.sqlBuilder.core.query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.query.Condition;
import org.in.media.res.sqlBuilder.api.query.ConditionValue;
import org.in.media.res.sqlBuilder.api.query.Having;
import org.in.media.res.sqlBuilder.api.query.HavingBuilder;
import org.in.media.res.sqlBuilder.api.query.HavingTranspiler;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.Operator;
import org.in.media.res.sqlBuilder.core.query.factory.TranspilerFactory;
import org.in.media.res.sqlBuilder.core.query.predicate.ClauseConditionBuffer;

public class HavingImpl implements Having {

    private final List<Condition> conditions = new ArrayList<>();

    private final ClauseConditionBuffer buffer = new ClauseConditionBuffer(conditions,
            "Cannot apply operators without a HAVING condition. Call having(...) first.");

	private final HavingTranspiler havingTranspiler = TranspilerFactory.instanciateHavingTranspiler();

    @Override
    public String transpile() {
        return conditions.isEmpty() ? "" : havingTranspiler.transpile(this);
    }

    @Override
    public void reset() {
        buffer.clear();
    }

    @Override
    public Having having(Condition condition) {
        buffer.add(condition, null);
        return this;
    }

    @Override
    public Having and(Condition condition) {
        buffer.add(condition, Operator.AND);
        return this;
    }

    @Override
    public Having or(Condition condition) {
        buffer.add(condition, Operator.OR);
        return this;
    }

    @Override
    public HavingBuilder having(Column column) {
        buffer.add(ConditionImpl.builder().leftColumn(requireColumn(column)).build(), null);
        return new BuilderDelegate();
    }

    @Override
    public List<Condition> havingConditions() {
        return buffer.snapshot();
    }

	private void appendStandaloneCondition(Operator operator, ConditionValue value) {
		buffer.appendStandalone(operator, value);
	}

    private ConditionValue numericValue(Number value) {
        return (value instanceof Double || value instanceof Float)
                ? ConditionValue.of(value.doubleValue())
                : ConditionValue.of(value.intValue());
    }

    private Having applyOperator(Operator operator, ConditionValue... values) {
        buffer.replaceLast(condition -> ClauseConditionBuffer.applyValues(condition, operator, List.of(values)));
        return HavingImpl.this;
    }

    private IllegalArgumentException emptyValues(Operator operator) {
        return new IllegalArgumentException(operator.name() + " requires at least one value");
    }

    private ConditionValue[] numericValues(Operator operator, Number... values) {
        if (values.length == 0) {
            throw emptyValues(operator);
        }
        ConditionValue[] result = new ConditionValue[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = numericValue(values[i]);
        }
        return result;
    }

    private ConditionValue[] stringValues(Operator operator, String... values) {
        if (values.length == 0) {
            throw emptyValues(operator);
        }
        ConditionValue[] result = new ConditionValue[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = ConditionValue.of(values[i]);
        }
        return result;
    }

    private ConditionValue[] dateValues(Operator operator, Date... values) {
        if (values.length == 0) {
            throw emptyValues(operator);
        }
        ConditionValue[] result = new ConditionValue[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = ConditionValue.of(values[i]);
        }
        return result;
    }

    private void setAggregate(AggregateOperator aggregate, Column column) {
        buffer.replaceLast(condition -> condition.withLeftAggregate(aggregate).withLeftColumn(requireColumn(column)));
    }

	private class BuilderDelegate implements HavingBuilder {

        @Override
        public Having eq(String value) {
            return applyOperator(Operator.EQ, ConditionValue.of(value));
        }

        @Override
        public Having eq(Number value) {
            return applyOperator(Operator.EQ, numericValue(value));
        }

        @Override
        public Having eq(Date value) {
            return applyOperator(Operator.EQ, ConditionValue.of(value));
        }

        @Override
        public Having notEq(String value) {
            return applyOperator(Operator.NOT_EQ, ConditionValue.of(value));
        }

        @Override
        public Having notEq(Number value) {
            return applyOperator(Operator.NOT_EQ, numericValue(value));
        }

        @Override
        public Having notEq(Date value) {
            return applyOperator(Operator.NOT_EQ, ConditionValue.of(value));
        }

        @Override
        public Having in(String... values) {
            return applyOperator(Operator.IN, stringValues(Operator.IN, values));
        }

        @Override
        public Having in(Number... values) {
            return applyOperator(Operator.IN, numericValues(Operator.IN, values));
        }

        @Override
        public Having in(Date... values) {
            return applyOperator(Operator.IN, dateValues(Operator.IN, values));
        }

        @Override
        public Having notIn(String... values) {
            return applyOperator(Operator.NOT_IN, stringValues(Operator.NOT_IN, values));
        }

        @Override
        public Having notIn(Number... values) {
            return applyOperator(Operator.NOT_IN, numericValues(Operator.NOT_IN, values));
        }

        @Override
		public Having notIn(Date... values) {
			return applyOperator(Operator.NOT_IN, dateValues(Operator.NOT_IN, values));
		}

		@Override
		public Having eq(Query subquery) {
			QueryValidation.requireScalarSubquery(subquery, "HAVING = subquery");
			return applyOperator(Operator.EQ, ConditionValue.of(subquery));
		}

		@Override
		public Having notEq(Query subquery) {
			QueryValidation.requireScalarSubquery(subquery, "HAVING <> subquery");
			return applyOperator(Operator.NOT_EQ, ConditionValue.of(subquery));
		}

		@Override
		public Having in(Query subquery) {
			QueryValidation.requireScalarSubquery(subquery, "HAVING IN (subquery)");
			return applyOperator(Operator.IN, ConditionValue.of(subquery));
		}

		@Override
		public Having notIn(Query subquery) {
			QueryValidation.requireScalarSubquery(subquery, "HAVING NOT IN (subquery)");
			return applyOperator(Operator.NOT_IN, ConditionValue.of(subquery));
		}

		@Override
		public Having supTo(Query subquery) {
			QueryValidation.requireScalarSubquery(subquery, "HAVING > subquery");
			return applyOperator(Operator.MORE, ConditionValue.of(subquery));
		}

		@Override
		public Having supOrEqTo(Query subquery) {
			QueryValidation.requireScalarSubquery(subquery, "HAVING >= subquery");
			return applyOperator(Operator.MORE_OR_EQ, ConditionValue.of(subquery));
		}

		@Override
		public Having infTo(Query subquery) {
			QueryValidation.requireScalarSubquery(subquery, "HAVING < subquery");
			return applyOperator(Operator.LESS, ConditionValue.of(subquery));
		}

		@Override
		public Having infOrEqTo(Query subquery) {
			QueryValidation.requireScalarSubquery(subquery, "HAVING <= subquery");
			return applyOperator(Operator.LESS_OR_EQ, ConditionValue.of(subquery));
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
            return applyOperator(Operator.LIKE, ConditionValue.of(value));
        }

        @Override
        public Having notLike(String value) {
            return applyOperator(Operator.NOT_LIKE, ConditionValue.of(value));
        }

        @Override
        public Having between(Number lower, Number upper) {
            return applyOperator(Operator.BETWEEN, numericValue(lower), numericValue(upper));
        }

        @Override
        public Having between(Date lower, Date upper) {
            return applyOperator(Operator.BETWEEN, ConditionValue.of(lower), ConditionValue.of(upper));
        }

        @Override
        public Having isNull() {
            return applyOperator(Operator.IS_NULL);
        }

        @Override
        public Having isNotNull() {
            return applyOperator(Operator.IS_NOT_NULL);
        }

        @Override
        public Having supTo(Number value) {
            return applyOperator(Operator.MORE, numericValue(value));
        }

        @Override
        public Having supOrEqTo(Number value) {
            return applyOperator(Operator.MORE_OR_EQ, numericValue(value));
        }

        @Override
        public Having infTo(Number value) {
            return applyOperator(Operator.LESS, numericValue(value));
        }

        @Override
        public Having infOrEqTo(Number value) {
            return applyOperator(Operator.LESS_OR_EQ, numericValue(value));
        }

        @Override
        public Having supTo(Column column) {
            buffer.replaceLast(condition -> condition.withOperator(Operator.MORE)
                    .withRightColumn(requireColumn(column)));
            return HavingImpl.this;
        }

        @Override
        public Having supOrEqTo(Column column) {
            buffer.replaceLast(condition -> condition.withOperator(Operator.MORE_OR_EQ)
                    .withRightColumn(requireColumn(column)));
            return HavingImpl.this;
        }

        @Override
        public Having infTo(Column column) {
            buffer.replaceLast(condition -> condition.withOperator(Operator.LESS)
                    .withRightColumn(requireColumn(column)));
            return HavingImpl.this;
        }

        @Override
        public Having infOrEqTo(Column column) {
            buffer.replaceLast(condition -> condition.withOperator(Operator.LESS_OR_EQ)
                    .withRightColumn(requireColumn(column)));
            return HavingImpl.this;
        }

        @Override
        public Having notEq(Column column) {
            buffer.replaceLast(condition -> condition.withOperator(Operator.NOT_EQ)
                    .withRightColumn(requireColumn(column)));
            return HavingImpl.this;
        }

        @Override
        public HavingBuilder and(Column column) {
            buffer.add(ConditionImpl.builder().and().leftColumn(requireColumn(column)).build(), null);
            return this;
        }

        @Override
        public HavingBuilder or(Column column) {
            buffer.add(ConditionImpl.builder().or().leftColumn(requireColumn(column)).build(), null);
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
            buffer.replaceLast(condition -> condition.withRightColumn(requireColumn(column)));
            return this;
        }
    }

    private Column requireColumn(Column column) {
        QueryValidation.requireTable(column, "Column must belong to a table for HAVING clause");
        return column;
    }
}
