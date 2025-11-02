package org.in.media.res.sqlBuilder.core.query.factory;

import static java.lang.Class.forName;

import java.lang.reflect.InvocationTargetException;

import org.in.media.res.sqlBuilder.api.query.FromTranspiler;

public class FromTranspilerFactory {


	private static final String DEFAULT_NAME = "org.in.media.res.sqlBuilder.core.query.transpiler.oracle.";

	public static FromTranspiler instanciateFromTranspiler() {
		return (FromTranspiler) instanciateFromTranspiler(getClazz(DEFAULT_NAME + "OracleFromTranspilerImpl"));
	}

	private static FromTranspiler instanciateFromTranspiler(Class<? extends FromTranspiler> clazz) {
		FromTranspiler instance = null;
		if (FromTranspiler.class.isAssignableFrom(clazz)) {
			try {
				instance = (FromTranspiler) Class.forName(clazz.getName()).getDeclaredConstructor((Class<?>[]) null)
						.newInstance((Object[]) null);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				return null;
			}
		}
		return instance;
	}

	private static Class<? extends FromTranspiler> getClazz(String className) {
		try {
			return forName(className).asSubclass(FromTranspiler.class);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
