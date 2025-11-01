package org.in.media.res.sqlBuilder.implementation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.UnaryOperator;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.Operator;
import org.in.media.res.sqlBuilder.implementation.Condition.ConditionValue;
import org.in.media.res.sqlBuilder.implementation.factories.HavingTranspilerFactory;
import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.query.ICondition;
import org.in.media.res.sqlBuilder.interfaces.query.IHaving;
import org.in.media.res.sqlBuilder.interfaces.query.IHavingBuilder;
import org.in.media.res.sqlBuilder.interfaces.query.IHavingTranspiler;

public class Having implements IHaving {

    private final List<Condition> conditions = new ArrayList<>();

    private final IHavingTranspiler havingTranspiler = HavingTranspilerFactory.instanciateHavingTranspiler();

    @Override
    public String transpile() {
        return conditions.isEmpty() ? "" : havingTranspiler.transpile(this);
    }

    @Override
    public void reset() {
        conditions.clear();
    }

    @Override
    public IHaving having(ICondition condition) {
        conditions.addLast(copyOf(condition));
        return this;
    }

    @Override
    public IHaving and(ICondition condition) {
        conditions.addLast(copyOf(condition));
        return this;
    }

    @Override
    public IHaving or(ICondition condition) {
        conditions.addLast(copyOf(condition));
        return this;
    }

    @Override
    public IHavingBuilder having(IColumn column) {
        conditions.addLast(Condition.builder().leftColumn(requireColumn(column)).build());
        return new BuilderDelegate();
    }

    @Override
    public List<ICondition> havingConditions() {
        return List.copyOf(conditions);
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
        if (conditions.isEmpty()) {
            throw new IllegalStateException("Cannot apply operators without a HAVING condition. Call having(...) first.");
        }
        return conditions.getLast();
    }

    private void replaceLast(UnaryOperator<Condition> mutator) {
        Condition current = lastCondition();
        int lastIndex = conditions.size() - 1;
        conditions.set(lastIndex, mutator.apply(current));
    }

    private ConditionValue numericValue(Number value) {
        return (value instanceof Double || value instanceof Float)
                ? ConditionValue.of(value.doubleValue())
                : ConditionValue.of(value.intValue());
    }

    private Operator resolveOperator(Operator operator, int valueCount) {
        if (operator == Operator.EQ && valueCount > 1) {
            return Operator.IN;
        }
        return operator;
    }

    private void setAggregate(AggregateOperator aggregate, IColumn column) {
        replaceLast(condition -> condition.withLeftAggregate(aggregate).withLeftColumn(requireColumn(column)));
    }

    private class BuilderDelegate implements IHavingBuilder {

        @Override
        public IHaving eq(String value) {
            replaceLast(condition -> condition.withOperator(Operator.EQ)
                    .appendValue(ConditionValue.of(value)));
            return Having.this;
        }

        @Override
        public IHaving eq(Number value) {
            replaceLast(condition -> condition.withOperator(Operator.EQ)
                    .appendValue(numericValue(value)));
            return Having.this;
        }

        @Override
        public IHaving eq(Date value) {
            replaceLast(condition -> condition.withOperator(Operator.EQ)
                    .appendValue(ConditionValue.of(value)));
            return Having.this;
        }

        @Override
        public IHaving in(String... values) {
            replaceLast(condition -> condition.withOperator(Operator.IN)
                    .appendValues(Arrays.stream(values).map(ConditionValue::of).toList()));
            return Having.this;
        }

        @Override
        public IHaving in(Number... values) {
            replaceLast(condition -> condition.withOperator(Operator.IN)
                    .appendValues(Arrays.stream(values).map(Having.this::numericValue).toList()));
            return Having.this;
        }

        @Override
        public IHaving supTo(Number value) {
            replaceLast(condition -> condition.withOperator(Operator.MORE)
                    .appendValue(numericValue(value)));
            return Having.this;
        }

        @Override
        public IHaving supOrEqTo(Number value) {
            replaceLast(condition -> condition.withOperator(Operator.MORE_OR_EQ)
                    .appendValue(numericValue(value)));
            return Having.this;
        }

        @Override
        public IHaving infTo(Number value) {
            replaceLast(condition -> condition.withOperator(Operator.LESS)
                    .appendValue(numericValue(value)));
            return Having.this;
        }

        @Override
        public IHaving infOrEqTo(Number value) {
            replaceLast(condition -> condition.withOperator(Operator.LESS_OR_EQ)
                    .appendValue(numericValue(value)));
            return Having.this;
        }

        @Override
        public IHaving supTo(IColumn column) {
            replaceLast(condition -> condition.withOperator(Operator.MORE)
                    .withRightColumn(requireColumn(column)));
            return Having.this;
        }

        @Override
        public IHaving supOrEqTo(IColumn column) {
            replaceLast(condition -> condition.withOperator(Operator.MORE_OR_EQ)
                    .withRightColumn(requireColumn(column)));
            return Having.this;
        }

        @Override
        public IHaving infTo(IColumn column) {
            replaceLast(condition -> condition.withOperator(Operator.LESS)
                    .withRightColumn(requireColumn(column)));
            return Having.this;
        }

        @Override
        public IHaving infOrEqTo(IColumn column) {
            replaceLast(condition -> condition.withOperator(Operator.LESS_OR_EQ)
                    .withRightColumn(requireColumn(column)));
            return Having.this;
        }

        @Override
        public IHavingBuilder and(IColumn column) {
            conditions.addLast(Condition.builder().and().leftColumn(column).build());
            return this;
        }

        @Override
        public IHavingBuilder or(IColumn column) {
            conditions.addLast(Condition.builder().or().leftColumn(requireColumn(column)).build());
            return this;
        }

        @Override
        public IHavingBuilder min(IColumn column) {
            setAggregate(AggregateOperator.MIN, column);
            return this;
        }

        @Override
        public IHavingBuilder max(IColumn column) {
            setAggregate(AggregateOperator.MAX, column);
            return this;
        }

        @Override
        public IHavingBuilder sum(IColumn column) {
            setAggregate(AggregateOperator.SUM, column);
            return this;
        }

        @Override
        public IHavingBuilder avg(IColumn column) {
            setAggregate(AggregateOperator.AVG, column);
            return this;
        }

        @Override
        public IHavingBuilder col(IColumn column) {
            replaceLast(condition -> condition.withRightColumn(requireColumn(column)));
            return this;
        }
    }

    private IColumn requireColumn(IColumn column) {
        QueryValidation.requireTable(column, "Column must belong to a table for HAVING clause");
        return column;
    }
}
