package org.in.media.res.sqlBuilder.interfaces.query;

import java.util.List;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.Operator;
import org.in.media.res.sqlBuilder.implementation.Condition.ConditionValue;
import org.in.media.res.sqlBuilder.interfaces.model.IColumn;

public interface ICondition extends ITranspilable {

	List<ConditionValue> values();

	IColumn getLeft();

	IColumn getRight();

	Operator getStartOperator();

	Operator getOperator();

	AggregateOperator getLeftAgg();

	AggregateOperator getRightAgg();

}
