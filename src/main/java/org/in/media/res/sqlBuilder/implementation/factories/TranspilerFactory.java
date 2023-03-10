package org.in.media.res.sqlBuilder.implementation.factories;

import org.in.media.res.sqlBuilder.interfaces.query.IColumnTranspiler;
import org.in.media.res.sqlBuilder.interfaces.query.IConditionTranspiler;
import org.in.media.res.sqlBuilder.interfaces.query.IFromTranspiler;
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

}
