package org.in.media.res.sqlBuilder.core.query.factory;

import static java.lang.Class.forName;

import java.lang.reflect.InvocationTargetException;

import org.in.media.res.sqlBuilder.api.query.SelectTranspiler;

public class SelectTranspilerFactory {

	private static final String DEFAULT_NAME = "org.in.media.res.sqlBuilder.core.query.transpiler.oracle.";

	public static SelectTranspiler instanciateSelectTranspiler() {
		return (SelectTranspiler) instanciateSelectTranspiler(getClazz(DEFAULT_NAME + "OracleSelectTranspilerImpl"));
	}

	private static SelectTranspiler instanciateSelectTranspiler(Class<? extends SelectTranspiler> clazz) {
		SelectTranspiler instance = null;
		if (SelectTranspiler.class.isAssignableFrom(clazz)) {
			try {
				instance = (SelectTranspiler) Class.forName(clazz.getName()).getDeclaredConstructor((Class<?>[]) null)
						.newInstance((Object[]) null);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				return null;
			}
		}
		return instance;
	}

	private static Class<? extends SelectTranspiler> getClazz(String className) {
		try {
			return forName(className).asSubclass(SelectTranspiler.class);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}
