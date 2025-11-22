package org.in.media.res.sqlBuilder.core.query;

import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

import org.in.media.res.sqlBuilder.api.query.Condition;
import org.in.media.res.sqlBuilder.api.query.ConditionValue;
import org.in.media.res.sqlBuilder.api.query.RawSqlFragment;
import org.in.media.res.sqlBuilder.constants.Operator;
import org.in.media.res.sqlBuilder.core.query.predicate.ConditionGroup;
import org.in.media.res.sqlBuilder.core.query.predicate.ParameterCondition;

/**
 * Utility to manage condition lists shared across WHERE / HAVING clauses.
 */
public final class ClauseConditionBuffer {

	private final List<Condition> conditions;
	private final String missingConditionMessage;

	public ClauseConditionBuffer(List<Condition> conditions, String missingConditionMessage) {
		this.conditions = Objects.requireNonNull(conditions, "conditions");
		this.missingConditionMessage = Objects.requireNonNull(missingConditionMessage, "missingConditionMessage");
	}

	public void add(Condition condition, Operator startOperator) {
		conditions.addLast(normalize(condition, startOperator));
	}

	public void addRaw(RawSqlFragment fragment, Operator startOperator) {
		conditions.addLast(new RawCondition(startOperator, fragment));
	}

	public boolean isEmpty() {
		return conditions.isEmpty();
	}

	public void clear() {
		conditions.clear();
	}

	public List<Condition> snapshot() {
		return List.copyOf(conditions);
	}

	public ConditionImpl last() {
		if (conditions.isEmpty() || !(conditions.getLast() instanceof ConditionImpl condition)) {
			throw new IllegalStateException(missingConditionMessage);
		}
		return condition;
	}

	public void replaceLast(UnaryOperator<ConditionImpl> mutator) {
		int lastIndex = conditions.size() - 1;
		conditions.set(lastIndex, Objects.requireNonNull(mutator, "mutator").apply(last()));
	}

	public void updateLast(Operator operator, List<ConditionValue> values) {
		replaceLast(condition -> applyValues(condition, operator, values));
	}

	public void setOperator(Operator operator) {
		replaceLast(condition -> condition.withOperator(operator));
	}

	public void appendStandalone(Operator operator, ConditionValue value) {
		if (conditions.isEmpty()) {
			conditions.addLast(ConditionImpl.builder().comparisonOp(operator).value(value).build());
			return;
		}
		Condition last = conditions.getLast();
		if (last instanceof ConditionImpl impl && impl.getOperator() == null && impl.getLeft() == null
				&& impl.values().isEmpty()) {
			conditions.set(conditions.size() - 1, impl.withOperator(operator).appendValue(value));
		} else {
			conditions.addLast(ConditionImpl.builder().and().comparisonOp(operator).value(value).build());
		}
	}

	public Condition normalize(Condition condition, Operator startOperator) {
		Objects.requireNonNull(condition, "condition");
		if (condition instanceof ParameterCondition parameterCondition) {
			return startOperator != null ? parameterCondition.withStartOperator(startOperator) : parameterCondition;
		}
		if (condition instanceof RawCondition rawCondition) {
			return startOperator != null ? rawCondition.withStartOperator(startOperator) : rawCondition;
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

	public static ConditionImpl applyValues(ConditionImpl condition, Operator operator, List<ConditionValue> newValues) {
		ConditionImpl updated = condition;
		boolean expectsValues = operator != null && operator != Operator.IS_NULL && operator != Operator.IS_NOT_NULL;
		if (expectsValues && (newValues == null || newValues.isEmpty())) {
			String label = operator.name();
			throw new IllegalArgumentException(label + " requires at least one value");
		}
		if (operator != null) {
			updated = updated.withOperator(operator);
		}
		if (newValues != null && !newValues.isEmpty()) {
			updated = updated.appendValues(newValues);
		}
		return updated;
	}
}
