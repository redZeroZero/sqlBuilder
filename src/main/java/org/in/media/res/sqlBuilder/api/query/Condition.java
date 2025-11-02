package org.in.media.res.sqlBuilder.api.query;

import java.util.List;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.Operator;
import org.in.media.res.sqlBuilder.api.model.Column;

public interface Condition extends Transpilable {

	List<ConditionValue> values();

	Column getLeft();

	Column getRight();

	Operator getStartOperator();

	Operator getOperator();

	AggregateOperator getLeftAgg();

	AggregateOperator getRightAgg();

}
