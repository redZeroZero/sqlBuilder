package org.in.media.res.sqlBuilder.core.query.factory;

import static java.lang.Class.forName;

import java.lang.reflect.InvocationTargetException;

import org.in.media.res.sqlBuilder.api.query.OrderByTranspiler;

public class OrderByTranspilerFactory {

	private static final String DEFAULT_NAME = "org.in.media.res.sqlBuilder.core.query.transpiler.oracle.";

	public static OrderByTranspiler instanciateOrderByTranspiler() {
		return instanciateOrderByTranspiler(getClazz(DEFAULT_NAME + "OracleOrderByTranspilerImpl"));
	}

	private static OrderByTranspiler instanciateOrderByTranspiler(Class<? extends OrderByTranspiler> clazz) {
		OrderByTranspiler instance = null;
		if (OrderByTranspiler.class.isAssignableFrom(clazz)) {
			try {
				instance = (OrderByTranspiler) Class.forName(clazz.getName()).getDeclaredConstructor((Class<?>[]) null)
						.newInstance((Object[]) null);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				return null;
			}
		}
		return instance;
	}

	private static Class<? extends OrderByTranspiler> getClazz(String className) {
		try {
			return forName(className).asSubclass(OrderByTranspiler.class);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
