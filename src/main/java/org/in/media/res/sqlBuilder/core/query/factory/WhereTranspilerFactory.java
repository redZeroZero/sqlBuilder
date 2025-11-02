package org.in.media.res.sqlBuilder.core.query.factory;

import static java.lang.Class.forName;

import java.lang.reflect.InvocationTargetException;

import org.in.media.res.sqlBuilder.api.query.WhereTranspiler;

public class WhereTranspilerFactory {


	private static final String DEFAULT_NAME = "org.in.media.res.sqlBuilder.core.query.transpiler.oracle.";

	public static WhereTranspiler instanciateWhereTranspiler() {
		return (WhereTranspiler) instanciateWhereTranspiler(getClazz(DEFAULT_NAME + "OracleWhereTranspilerImpl"));
	}

	private static WhereTranspiler instanciateWhereTranspiler(Class<? extends WhereTranspiler> clazz) {
		WhereTranspiler instance = null;
		if (WhereTranspiler.class.isAssignableFrom(clazz)) {
			try {
				instance = (WhereTranspiler) Class.forName(clazz.getName()).getDeclaredConstructor((Class<?>[]) null)
						.newInstance((Object[]) null);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				return null;
			}
		}
		return instance;
	}

	private static Class<? extends WhereTranspiler> getClazz(String className) {
		try {
			return forName(className).asSubclass(WhereTranspiler.class);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
