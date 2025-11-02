package org.in.media.res.sqlBuilder.core.query.factory;

import static java.lang.Class.forName;

import java.lang.reflect.InvocationTargetException;

import org.in.media.res.sqlBuilder.api.query.HavingTranspiler;

public class HavingTranspilerFactory {

	private static final String DEFAULT_NAME = "org.in.media.res.sqlBuilder.core.query.transpiler.oracle.";

	public static HavingTranspiler instanciateHavingTranspiler() {
		return instanciateHavingTranspiler(getClazz(DEFAULT_NAME + "OracleHavingTranspilerImpl"));
	}

	private static HavingTranspiler instanciateHavingTranspiler(Class<? extends HavingTranspiler> clazz) {
		HavingTranspiler instance = null;
		if (HavingTranspiler.class.isAssignableFrom(clazz)) {
			try {
				instance = (HavingTranspiler) Class.forName(clazz.getName()).getDeclaredConstructor((Class<?>[]) null)
						.newInstance((Object[]) null);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				return null;
			}
		}
		return instance;
	}

	private static Class<? extends HavingTranspiler> getClazz(String className) {
		try {
			return forName(className).asSubclass(HavingTranspiler.class);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
