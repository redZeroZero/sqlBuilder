package org.in.media.res.sqlBuilder.implementation.factories;

import static java.lang.Class.forName;

import java.lang.reflect.InvocationTargetException;

import org.in.media.res.sqlBuilder.interfaces.query.IFromTranspiler;

public class FromTranspilerFactory {


	private static final String DEFAULT_NAME = "org.in.media.res.sqlBuilder.implementation.transpilers.clauses.";

	public static IFromTranspiler instanciateFromTranspiler() {
		return (IFromTranspiler) instanciateFromTranspiler(getClazz(DEFAULT_NAME + "OracleFromTranspilerImpl"));
	}

	private static IFromTranspiler instanciateFromTranspiler(Class<? extends IFromTranspiler> clazz) {
		IFromTranspiler instance = null;
		if (IFromTranspiler.class.isAssignableFrom(clazz)) {
			try {
				instance = (IFromTranspiler) Class.forName(clazz.getName()).getDeclaredConstructor((Class<?>[]) null)
						.newInstance((Object[]) null);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				return null;
			}
		}
		return instance;
	}

	private static Class<? extends IFromTranspiler> getClazz(String className) {
		try {
			return forName(className).asSubclass(IFromTranspiler.class);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
