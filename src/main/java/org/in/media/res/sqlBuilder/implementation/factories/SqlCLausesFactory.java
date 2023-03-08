package org.in.media.res.sqlBuilder.implementation.factories;

import static java.lang.Class.forName;

import java.lang.reflect.InvocationTargetException;

import org.in.media.res.sqlBuilder.interfaces.query.IClause;
import org.in.media.res.sqlBuilder.interfaces.query.IFrom;
import org.in.media.res.sqlBuilder.interfaces.query.ISelect;
import org.in.media.res.sqlBuilder.interfaces.query.IWhere;

public class SqlCLausesFactory {

	public static ISelect instanciateSelect() {
		return (ISelect) instanciateClause(getClazz("com.coface.corp.sqlBuilder.implementation.Select"));
	}

	public static IFrom instanciateFrom() {
		return (IFrom) instanciateClause(getClazz("com.coface.corp.sqlBuilder.implementation.From"));
	}

	public static IWhere instanciateWhere() {
		return (IWhere) instanciateClause(getClazz("com.coface.corp.sqlBuilder.implementation.Where"));
	}

	public static IClause instanciateClause(Class<IClause> clazz) {
		IClause instance = null;
		if (IClause.class.isAssignableFrom(clazz)) {
			try {
				instance = (IClause) Class.forName(clazz.getName()).getDeclaredConstructor((Class<?>[]) null)
						.newInstance((Object[]) null);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				return null;
			}
		}
		return instance;
	}

	public static ISelect instanciateSelect(String className) {
		return (ISelect) instanciateClause(getClazz(className));
	}

	public static IFrom instanciateFrom(String className) {
		return (IFrom) instanciateClause(getClazz(className));
	}

	public static IWhere instanciateWhere(String className) {
		return (IWhere) instanciateClause(getClazz(className));
	}

	private static Class<IClause> getClazz(String className) {
		try {
			return (Class<IClause>) forName(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
