package org.in.media.res.sqlBuilder.implementation.factories;

import static java.lang.Class.forName;

import java.lang.reflect.InvocationTargetException;

import org.in.media.res.sqlBuilder.interfaces.query.IConditionTranspiler;

public class ConditionTranspilerFactory {

	private static final String DEFAULT_NAME = "org.in.media.res.sqlBuilder.implementation.transpilers.clauses.";

	public static IConditionTranspiler instanciateConditionTranspiler() {
		return (IConditionTranspiler) instanciateConditionTranspiler(
				getClazz(DEFAULT_NAME + "OracleConditionTranspilerImpl"));
	}

	private static IConditionTranspiler instanciateConditionTranspiler(Class<? extends IConditionTranspiler> clazz) {
		IConditionTranspiler instance = null;
		if (IConditionTranspiler.class.isAssignableFrom(clazz)) {
			try {
				instance = (IConditionTranspiler) Class.forName(clazz.getName())
						.getDeclaredConstructor((Class<?>[]) null).newInstance((Object[]) null);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				return null;
			}
		}
		return instance;
	}

	private static Class<? extends IConditionTranspiler> getClazz(String className) {
		try {
			return forName(className).asSubclass(IConditionTranspiler.class);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}
