package org.in.media.res.sqlBuilder.core.query.factory;

import static java.lang.Class.forName;

import java.lang.reflect.InvocationTargetException;

import org.in.media.res.sqlBuilder.api.query.Clause;
import org.in.media.res.sqlBuilder.api.query.From;
import org.in.media.res.sqlBuilder.api.query.GroupBy;
import org.in.media.res.sqlBuilder.api.query.Having;
import org.in.media.res.sqlBuilder.api.query.Limit;
import org.in.media.res.sqlBuilder.api.query.OrderBy;
import org.in.media.res.sqlBuilder.api.query.Select;
import org.in.media.res.sqlBuilder.api.query.Where;

public class CLauseFactory {

	private static final String DEFAULT_NAME = "org.in.media.res.sqlBuilder.core.query.";

	public static Select instanciateSelect() {
		return (Select) instanciateClause(getClazz(DEFAULT_NAME + "SelectImpl"));
	}

	public static From instanciateFrom() {
		return (From) instanciateClause(getClazz(DEFAULT_NAME + "FromImpl"));
	}

	public static Where instanciateWhere() {
		return (Where) instanciateClause(getClazz(DEFAULT_NAME + "WhereImpl"));
	}

	public static GroupBy instanciateGroupBy() {
		return (GroupBy) instanciateClause(getClazz(DEFAULT_NAME + "GroupByImpl"));
	}

	public static OrderBy instanciateOrderBy() {
		return (OrderBy) instanciateClause(getClazz(DEFAULT_NAME + "OrderByImpl"));
	}

	public static Having instanciateHaving() {
		return (Having) instanciateClause(getClazz(DEFAULT_NAME + "HavingImpl"));
	}

	public static Limit instanciateLimit() {
		return (Limit) instanciateClause(getClazz(DEFAULT_NAME + "LimitImpl"));
	}

	private static Clause instanciateClause(Class<? extends Clause> clazz) {
		Clause instance = null;
		if (Clause.class.isAssignableFrom(clazz)) {
			try {
				instance = (Clause) Class.forName(clazz.getName()).getDeclaredConstructor((Class<?>[]) null)
						.newInstance((Object[]) null);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				return null;
			}
		}
		return instance;
	}

	private static Class<? extends Clause> getClazz(String className) {
		try {
			return forName(className).asSubclass(Clause.class);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}
