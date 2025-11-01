package org.in.media.res.sqlBuilder.implementation.factories;

import org.in.media.res.sqlBuilder.interfaces.query.IColumnTranspiler;
import org.in.media.res.sqlBuilder.interfaces.query.IConditionTranspiler;
import org.in.media.res.sqlBuilder.interfaces.query.IFromTranspiler;
import org.in.media.res.sqlBuilder.interfaces.query.IGroupByTranspiler;
import org.in.media.res.sqlBuilder.interfaces.query.IHavingTranspiler;
import org.in.media.res.sqlBuilder.interfaces.query.ILimitTranspiler;
import org.in.media.res.sqlBuilder.interfaces.query.IOrderByTranspiler;
import org.in.media.res.sqlBuilder.interfaces.query.ISelectTranspiler;
import org.in.media.res.sqlBuilder.interfaces.query.IWhereTranspiler;

public class TranspilerFactory {
	
	public static IConditionTranspiler instanciateConditionTranspiler() {
		return ConditionTranspilerFactory.instanciateConditionTranspiler();
	}

	public static IColumnTranspiler instanciateColumnTranspiler() {
		return ColumnTranspilerFactory.instanciateColumnTranspiler();
	}

	public static ISelectTranspiler instanciateSelectTranspiler() {
		return SelectTranspilerFactory.instanciateSelectTranspiler();
	}
	
	public static IFromTranspiler instanciateFromTranspiler() {
		return FromTranspilerFactory.instanciateFromTranspiler();
	}

	public static IWhereTranspiler instanciateWhereTranspiler() {
		return WhereTranspilerFactory.instanciateWhereTranspiler();
	}

	public static IGroupByTranspiler instanciateGroupByTranspiler() {
		return GroupByTranspilerFactory.instanciateGroupByTranspiler();
	}

	public static IOrderByTranspiler instanciateOrderByTranspiler() {
		return OrderByTranspilerFactory.instanciateOrderByTranspiler();
	}

	public static IHavingTranspiler instanciateHavingTranspiler() {
		return HavingTranspilerFactory.instanciateHavingTranspiler();
	}

	public static ILimitTranspiler instanciateLimitTranspiler() {
		return LimitTranspilerFactory.instanciateLimitTranspiler();
	}

}
