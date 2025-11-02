package org.in.media.res.sqlBuilder.core.query.factory;

import static java.lang.Class.forName;

import java.lang.reflect.InvocationTargetException;

import org.in.media.res.sqlBuilder.api.query.ConditionTranspiler;

public class ConditionTranspilerFactory {

	private static final String DEFAULT_NAME = "org.in.media.res.sqlBuilder.core.query.transpiler.oracle.";

	public static ConditionTranspiler instanciateConditionTranspiler() {
		return (ConditionTranspiler) instanciateConditionTranspiler(
				getClazz(DEFAULT_NAME + "OracleConditionTranspilerImpl"));
	}

	private static ConditionTranspiler instanciateConditionTranspiler(Class<? extends ConditionTranspiler> clazz) {
		ConditionTranspiler instance = null;
		if (ConditionTranspiler.class.isAssignableFrom(clazz)) {
			try {
				instance = (ConditionTranspiler) Class.forName(clazz.getName())
						.getDeclaredConstructor((Class<?>[]) null).newInstance((Object[]) null);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				return null;
			}
		}
		return instance;
	}

	private static Class<? extends ConditionTranspiler> getClazz(String className) {
		try {
			return forName(className).asSubclass(ConditionTranspiler.class);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}
