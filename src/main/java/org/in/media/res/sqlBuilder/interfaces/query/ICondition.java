package org.in.media.res.sqlBuilder.interfaces.query;

import java.util.Date;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.Operator;
import org.in.media.res.sqlBuilder.interfaces.model.IColumn;

public interface ICondition extends ITranspilable {

	void addValue(String value);

	void addValue(Integer value);

	void addValue(Double value);

	void addValue(Date value);

	void resetBuilder();

	IColumn getLeft();

	void setLeft(IColumn left);

	IColumn getRight();

	void setRight(IColumn right);

	Operator getStartOperator();

	void setStartOperator(Operator startOperator);

	Operator getOperator();

	void setOperator(Operator operator);

	AggregateOperator getLeftAgg();

	void setLeftAgg(AggregateOperator leftAgg);

	AggregateOperator getRightAgg();

	void setRightAgg(AggregateOperator rightAgg);

}