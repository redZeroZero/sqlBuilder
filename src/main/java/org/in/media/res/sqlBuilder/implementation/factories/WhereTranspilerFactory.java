package org.in.media.res.sqlBuilder.implementation.factories;

import static java.lang.Class.forName;

import java.lang.reflect.InvocationTargetException;

import org.in.media.res.sqlBuilder.interfaces.query.IWhereTranspiler;

public class WhereTranspilerFactory {


	private static final String DEFAULT_NAME = "org.in.media.res.sqlBuilder.implementation.transpilers.clauses.";

	public static IWhereTranspiler instanciateWhereTranspiler() {
		return (IWhereTranspiler) instanciateWhereTranspiler(getClazz(DEFAULT_NAME + "OracleWhereTranspilerImpl"));
	}

	private static IWhereTranspiler instanciateWhereTranspiler(Class<? extends IWhereTranspiler> clazz) {
		IWhereTranspiler instance = null;
		if (IWhereTranspiler.class.isAssignableFrom(clazz)) {
			try {
				instance = (IWhereTranspiler) Class.forName(clazz.getName()).getDeclaredConstructor((Class<?>[]) null)
						.newInstance((Object[]) null);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				return null;
			}
		}
		return instance;
	}

	private static Class<? extends IWhereTranspiler> getClazz(String className) {
		try {
			return forName(className).asSubclass(IWhereTranspiler.class);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
