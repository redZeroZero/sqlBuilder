package org.in.media.res.sqlBuilder.implementation.factories;

import static java.lang.Class.forName;

import java.lang.reflect.InvocationTargetException;

import org.in.media.res.sqlBuilder.interfaces.query.IClause;
import org.in.media.res.sqlBuilder.interfaces.query.IFrom;
import org.in.media.res.sqlBuilder.interfaces.query.ISelect;
import org.in.media.res.sqlBuilder.interfaces.query.IWhere;

public class CLauseFactory {

	private static final String DEFAULT_NAME = "org.in.media.res.sqlBuilder.implementation.";

	public static ISelect instanciateSelect() {
		return (ISelect) instanciateClause(getClazz(DEFAULT_NAME + "Select"));
	}

	public static IFrom instanciateFrom() {
		return (IFrom) instanciateClause(getClazz(DEFAULT_NAME + "From"));
	}

	public static IWhere instanciateWhere() {
		return (IWhere) instanciateClause(getClazz(DEFAULT_NAME + "Where"));
	}

	private static IClause instanciateClause(Class<? extends IClause> clazz) {
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

	private static Class<? extends IClause> getClazz(String className) {
		try {
			return forName(className).asSubclass(IClause.class);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}
