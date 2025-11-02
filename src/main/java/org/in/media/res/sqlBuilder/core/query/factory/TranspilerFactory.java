package org.in.media.res.sqlBuilder.core.query.factory;

import org.in.media.res.sqlBuilder.api.query.ColumnTranspiler;
import org.in.media.res.sqlBuilder.api.query.ConditionTranspiler;
import org.in.media.res.sqlBuilder.api.query.FromTranspiler;
import org.in.media.res.sqlBuilder.api.query.GroupByTranspiler;
import org.in.media.res.sqlBuilder.api.query.HavingTranspiler;
import org.in.media.res.sqlBuilder.api.query.LimitTranspiler;
import org.in.media.res.sqlBuilder.api.query.OrderByTranspiler;
import org.in.media.res.sqlBuilder.api.query.SelectTranspiler;
import org.in.media.res.sqlBuilder.api.query.WhereTranspiler;

public class TranspilerFactory {
	
	public static ConditionTranspiler instanciateConditionTranspiler() {
		return ConditionTranspilerFactory.instanciateConditionTranspiler();
	}

	public static ColumnTranspiler instanciateColumnTranspiler() {
		return ColumnTranspilerFactory.instanciateColumnTranspiler();
	}

	public static SelectTranspiler instanciateSelectTranspiler() {
		return SelectTranspilerFactory.instanciateSelectTranspiler();
	}
	
	public static FromTranspiler instanciateFromTranspiler() {
		return FromTranspilerFactory.instanciateFromTranspiler();
	}

	public static WhereTranspiler instanciateWhereTranspiler() {
		return WhereTranspilerFactory.instanciateWhereTranspiler();
	}

	public static GroupByTranspiler instanciateGroupByTranspiler() {
		return GroupByTranspilerFactory.instanciateGroupByTranspiler();
	}

	public static OrderByTranspiler instanciateOrderByTranspiler() {
		return OrderByTranspilerFactory.instanciateOrderByTranspiler();
	}

	public static HavingTranspiler instanciateHavingTranspiler() {
		return HavingTranspilerFactory.instanciateHavingTranspiler();
	}

	public static LimitTranspiler instanciateLimitTranspiler() {
		return LimitTranspilerFactory.instanciateLimitTranspiler();
	}

}
