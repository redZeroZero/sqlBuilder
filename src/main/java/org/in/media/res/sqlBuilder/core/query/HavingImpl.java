package org.in.media.res.sqlBuilder.core.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.UnaryOperator;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.Operator;
import org.in.media.res.sqlBuilder.api.query.ConditionValue;
import org.in.media.res.sqlBuilder.core.query.factory.HavingTranspilerFactory;
import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.query.Condition;
import org.in.media.res.sqlBuilder.api.query.Having;
import org.in.media.res.sqlBuilder.api.query.HavingBuilder;
import org.in.media.res.sqlBuilder.api.query.HavingTranspiler;

public class HavingImpl implements Having {

    private final List<Condition> conditions = new ArrayList<>();

    private final HavingTranspiler havingTranspiler = HavingTranspilerFactory.instanciateHavingTranspiler();

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
        conditions.addLast(copyOf(condition));
        return this;
    }

    @Override
    public Having and(Condition condition) {
        conditions.addLast(copyOf(condition));
        return this;
    }

    @Override
    public Having or(Condition condition) {
        conditions.addLast(copyOf(condition));
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
        if (conditions.isEmpty()) {
            throw new IllegalStateException("Cannot apply operators without a HAVING condition. Call having(...) first.");
        }
        return (ConditionImpl) conditions.getLast();
    }

    private void replaceLast(UnaryOperator<ConditionImpl> mutator) {
        ConditionImpl current = lastCondition();
        int lastIndex = conditions.size() - 1;
        conditions.set(lastIndex, mutator.apply(current));
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
