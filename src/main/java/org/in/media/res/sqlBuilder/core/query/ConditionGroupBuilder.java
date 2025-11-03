package org.in.media.res.sqlBuilder.core.query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.api.query.Condition;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.Operator;

/**
 * Builder for grouped boolean expressions that can be used inside WHERE / HAVING clauses.
 */
public final class ConditionGroupBuilder {

    private final WhereImpl delegate = new WhereImpl();

    public ConditionGroupBuilder where(Column column) {
        delegate.where(column);
        return this;
    }

	public ConditionGroupBuilder where(TableDescriptor<?> descriptor) {
		return where(descriptor.column());
	}

    public ConditionGroupBuilder where(Condition condition) {
        delegate.condition(condition);
        return this;
    }

    public ConditionGroupBuilder and(Column column) {
        delegate.and(column);
        return this;
    }

	public ConditionGroupBuilder and(TableDescriptor<?> descriptor) {
		return and(descriptor.column());
	}

    public ConditionGroupBuilder and(Consumer<ConditionGroupBuilder> consumer) {
        return addGroup(Operator.AND, consumer);
    }

    public ConditionGroupBuilder or(Column column) {
        delegate.or(column);
        return this;
    }

	public ConditionGroupBuilder or(TableDescriptor<?> descriptor) {
		return or(descriptor.column());
	}

    public ConditionGroupBuilder or(Consumer<ConditionGroupBuilder> consumer) {
        return addGroup(Operator.OR, consumer);
    }

    public ConditionGroupBuilder and() {
        delegate.and();
        return this;
    }

    public ConditionGroupBuilder or() {
        delegate.or();
        return this;
    }

    public ConditionGroupBuilder eq(String value) {
        delegate.eq(value);
        return this;
    }

    public ConditionGroupBuilder eq(Integer value) {
        delegate.eq(value);
        return this;
    }

    public ConditionGroupBuilder eq(Double value) {
        delegate.eq(value);
        return this;
    }

    public ConditionGroupBuilder eq(Date value) {
        delegate.eq(value);
        return this;
    }

    public ConditionGroupBuilder eq(Query subquery) {
        delegate.eq(subquery);
        return this;
    }

    public ConditionGroupBuilder notEq(String value) {
        delegate.notEq(value);
        return this;
    }

    public ConditionGroupBuilder notEq(Integer value) {
        delegate.notEq(value);
        return this;
    }

    public ConditionGroupBuilder notEq(Double value) {
        delegate.notEq(value);
        return this;
    }

    public ConditionGroupBuilder notEq(Date value) {
        delegate.notEq(value);
        return this;
    }

    public ConditionGroupBuilder notEq(Query subquery) {
        delegate.notEq(subquery);
        return this;
    }

	public ConditionGroupBuilder supTo(Integer value) {
		delegate.supTo(value);
		return this;
	}

	public ConditionGroupBuilder supTo(Double value) {
		delegate.supTo(value);
		return this;
	}

	public ConditionGroupBuilder supTo(Query subquery) {
        delegate.supTo(subquery);
        return this;
    }

	public ConditionGroupBuilder supOrEqTo(Integer value) {
		delegate.supOrEqTo(value);
		return this;
	}

	public ConditionGroupBuilder supOrEqTo(Double value) {
		delegate.supOrEqTo(value);
		return this;
	}

    public ConditionGroupBuilder supOrEqTo(Query subquery) {
        delegate.supOrEqTo(subquery);
        return this;
    }

	public ConditionGroupBuilder infTo(Integer value) {
		delegate.infTo(value);
		return this;
	}

	public ConditionGroupBuilder infTo(Double value) {
		delegate.infTo(value);
		return this;
	}

    public ConditionGroupBuilder infTo(Query subquery) {
        delegate.infTo(subquery);
        return this;
    }

	public ConditionGroupBuilder infOrEqTo(Integer value) {
		delegate.infOrEqTo(value);
		return this;
	}

	public ConditionGroupBuilder infOrEqTo(Double value) {
		delegate.infOrEqTo(value);
		return this;
	}

    public ConditionGroupBuilder infOrEqTo(Query subquery) {
        delegate.infOrEqTo(subquery);
        return this;
    }

    public ConditionGroupBuilder in(String... values) {
        delegate.in(values);
        return this;
    }

	public ConditionGroupBuilder in(Integer... values) {
		delegate.in(values);
		return this;
	}

	public ConditionGroupBuilder in(Double... values) {
		delegate.in(values);
		return this;
	}

    public ConditionGroupBuilder in(Query subquery) {
        delegate.in(subquery);
        return this;
    }

    public ConditionGroupBuilder notIn(String... values) {
        delegate.notIn(values);
        return this;
    }

	public ConditionGroupBuilder notIn(Integer... values) {
		delegate.notIn(values);
		return this;
	}

	public ConditionGroupBuilder notIn(Double... values) {
		delegate.notIn(values);
		return this;
	}

    public ConditionGroupBuilder notIn(Query subquery) {
        delegate.notIn(subquery);
        return this;
    }

    public ConditionGroupBuilder like(String value) {
        delegate.like(value);
        return this;
    }

    public ConditionGroupBuilder notLike(String value) {
        delegate.notLike(value);
        return this;
    }

	public ConditionGroupBuilder between(Integer lower, Integer upper) {
		delegate.between(lower, upper);
		return this;
	}

	public ConditionGroupBuilder between(Double lower, Double upper) {
		delegate.between(lower, upper);
		return this;
	}

    public ConditionGroupBuilder between(Date lower, Date upper) {
        delegate.between(lower, upper);
        return this;
    }

    public ConditionGroupBuilder isNull() {
        delegate.isNull();
        return this;
    }

    public ConditionGroupBuilder isNotNull() {
        delegate.isNotNull();
        return this;
    }

	public ConditionGroupBuilder min(Column column) {
		delegate.aggregate(AggregateOperator.MIN, column);
		return this;
	}

	public ConditionGroupBuilder min(TableDescriptor<?> descriptor) {
		return min(descriptor.column());
	}

	public ConditionGroupBuilder max(Column column) {
		delegate.aggregate(AggregateOperator.MAX, column);
		return this;
	}

	public ConditionGroupBuilder max(TableDescriptor<?> descriptor) {
		return max(descriptor.column());
	}

	public ConditionGroupBuilder sum(Column column) {
		delegate.aggregate(AggregateOperator.SUM, column);
		return this;
	}

	public ConditionGroupBuilder sum(TableDescriptor<?> descriptor) {
		return sum(descriptor.column());
	}

	public ConditionGroupBuilder avg(Column column) {
		delegate.aggregate(AggregateOperator.AVG, column);
		return this;
	}

	public ConditionGroupBuilder avg(TableDescriptor<?> descriptor) {
		return avg(descriptor.column());
	}

	public ConditionGroupBuilder col(Column column) {
		delegate.col(column);
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

    public Condition build() {
        List<Condition> conditions = new ArrayList<>(delegate.conditions());
        if (conditions.isEmpty()) {
            throw new IllegalStateException("Condition group must contain at least one predicate");
        }
        return new ConditionGroup(conditions, null);
    }

    private ConditionGroupBuilder addGroup(Operator connector, Consumer<ConditionGroupBuilder> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        ConditionGroupBuilder nested = new ConditionGroupBuilder();
        consumer.accept(nested);
        Condition groupCondition = nested.build();
        if (connector != null) {
            groupCondition = ((ConditionGroup) groupCondition).withStartOperator(connector);
        }
        delegate.condition(groupCondition);
        return this;
    }

    static final class ConditionGroup implements Condition {

        private final List<Condition> children;
        private final Operator startOperator;

        ConditionGroup(List<Condition> children, Operator startOperator) {
            this.children = children;
            this.startOperator = startOperator;
        }

        ConditionGroup withStartOperator(Operator operator) {
            if (operator == startOperator) {
                return this;
            }
            return new ConditionGroup(children, operator);
        }

        @Override
        public List<org.in.media.res.sqlBuilder.api.query.ConditionValue> values() {
            return List.of();
        }

        @Override
        public Column getLeft() {
            return null;
        }

        @Override
        public Column getRight() {
            return null;
        }

        @Override
        public Operator getStartOperator() {
            return startOperator;
        }

        @Override
        public Operator getOperator() {
            return null;
        }

        @Override
        public org.in.media.res.sqlBuilder.constants.AggregateOperator getLeftAgg() {
            return null;
        }

        @Override
        public org.in.media.res.sqlBuilder.constants.AggregateOperator getRightAgg() {
            return null;
        }

		@Override
		public String transpile() {
			StringBuilder sb = new StringBuilder();
			if (startOperator != null) {
				sb.append(startOperator.value());
			}
			sb.append('(');
			boolean first = true;
			for (Condition condition : children) {
				String rendered = condition.transpile();
				if (!first && rendered.length() > 0 && !Character.isWhitespace(rendered.charAt(0))) {
					sb.append(' ');
				}
				sb.append(rendered);
				first = false;
			}
			sb.append(")");
			return sb.toString();
		}
    }
}
