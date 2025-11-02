package org.in.media.res.sqlBuilder.core.query.factory;

import static java.lang.Class.forName;

import java.lang.reflect.InvocationTargetException;

import org.in.media.res.sqlBuilder.api.query.ColumnTranspiler;

public class ColumnTranspilerFactory {

	private static final String DEFAULT_NAME = "org.in.media.res.sqlBuilder.core.query.transpiler.oracle.";

	public static ColumnTranspiler instanciateColumnTranspiler() {
		return (ColumnTranspiler) instanciateColumnTranspiler(getClazz(DEFAULT_NAME + "OracleColumnTranspilerImpl"));
	}

	private static ColumnTranspiler instanciateColumnTranspiler(Class<? extends ColumnTranspiler> clazz) {
		ColumnTranspiler instance = null;
		if (ColumnTranspiler.class.isAssignableFrom(clazz)) {
			try {
				instance = (ColumnTranspiler) Class.forName(clazz.getName()).getDeclaredConstructor((Class<?>[]) null)
						.newInstance((Object[]) null);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				return null;
			}
		}
		return instance;
	}

	private static Class<? extends ColumnTranspiler> getClazz(String className) {
		try {
			return forName(className).asSubclass(ColumnTranspiler.class);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
}
