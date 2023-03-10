package org.in.media.res.sqlBuilder.implementation.factories;

import static java.lang.Class.forName;

import java.lang.reflect.InvocationTargetException;

import org.in.media.res.sqlBuilder.interfaces.query.ISelectTranspiler;

public class SelectTranspilerFactory {

	private static final String DEFAULT_NAME = "org.in.media.res.sqlBuilder.implementation.transpilers.clauses.";

	public static ISelectTranspiler instanciateSelectTranspiler() {
		return (ISelectTranspiler) instanciateSelectTranspiler(getClazz(DEFAULT_NAME + "OracleSelectTranspilerImpl"));
	}

	private static ISelectTranspiler instanciateSelectTranspiler(Class<? extends ISelectTranspiler> clazz) {
		ISelectTranspiler instance = null;
		if (ISelectTranspiler.class.isAssignableFrom(clazz)) {
			try {
				instance = (ISelectTranspiler) Class.forName(clazz.getName()).getDeclaredConstructor((Class<?>[]) null)
						.newInstance((Object[]) null);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				return null;
			}
		}
		return instance;
	}

	private static Class<? extends ISelectTranspiler> getClazz(String className) {
		try {
			return forName(className).asSubclass(ISelectTranspiler.class);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}
