package org.in.media.res.sqlBuilder.implementation.factories;

import static java.lang.Class.forName;

import java.lang.reflect.InvocationTargetException;

import org.in.media.res.sqlBuilder.interfaces.query.IOrderByTranspiler;

public class OrderByTranspilerFactory {

	private static final String DEFAULT_NAME = "org.in.media.res.sqlBuilder.implementation.transpilers.clauses.";

	public static IOrderByTranspiler instanciateOrderByTranspiler() {
		return instanciateOrderByTranspiler(getClazz(DEFAULT_NAME + "OracleOrderByTranspilerImpl"));
	}

	private static IOrderByTranspiler instanciateOrderByTranspiler(Class<? extends IOrderByTranspiler> clazz) {
		IOrderByTranspiler instance = null;
		if (IOrderByTranspiler.class.isAssignableFrom(clazz)) {
			try {
				instance = (IOrderByTranspiler) Class.forName(clazz.getName()).getDeclaredConstructor((Class<?>[]) null)
						.newInstance((Object[]) null);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				return null;
			}
		}
		return instance;
	}

	private static Class<? extends IOrderByTranspiler> getClazz(String className) {
		try {
			return forName(className).asSubclass(IOrderByTranspiler.class);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
