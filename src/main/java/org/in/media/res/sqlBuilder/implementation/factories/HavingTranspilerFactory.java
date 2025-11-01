package org.in.media.res.sqlBuilder.implementation.factories;

import static java.lang.Class.forName;

import java.lang.reflect.InvocationTargetException;

import org.in.media.res.sqlBuilder.interfaces.query.IHavingTranspiler;

public class HavingTranspilerFactory {

	private static final String DEFAULT_NAME = "org.in.media.res.sqlBuilder.implementation.transpilers.clauses.";

	public static IHavingTranspiler instanciateHavingTranspiler() {
		return instanciateHavingTranspiler(getClazz(DEFAULT_NAME + "OracleHavingTranspilerImpl"));
	}

	private static IHavingTranspiler instanciateHavingTranspiler(Class<? extends IHavingTranspiler> clazz) {
		IHavingTranspiler instance = null;
		if (IHavingTranspiler.class.isAssignableFrom(clazz)) {
			try {
				instance = (IHavingTranspiler) Class.forName(clazz.getName()).getDeclaredConstructor((Class<?>[]) null)
						.newInstance((Object[]) null);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				return null;
			}
		}
		return instance;
	}

	private static Class<? extends IHavingTranspiler> getClazz(String className) {
		try {
			return forName(className).asSubclass(IHavingTranspiler.class);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
