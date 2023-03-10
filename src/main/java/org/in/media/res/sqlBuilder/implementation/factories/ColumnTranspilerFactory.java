package org.in.media.res.sqlBuilder.implementation.factories;

import static java.lang.Class.forName;

import java.lang.reflect.InvocationTargetException;

import org.in.media.res.sqlBuilder.interfaces.query.IColumnTranspiler;

public class ColumnTranspilerFactory {

	private static final String DEFAULT_NAME = "org.in.media.res.sqlBuilder.implementation.transpilers.clauses.";

	public static IColumnTranspiler instanciateColumnTranspiler() {
		return (IColumnTranspiler) instanciateColumnTranspiler(getClazz(DEFAULT_NAME + "OracleColumnTranspilerImpl"));
	}

	private static IColumnTranspiler instanciateColumnTranspiler(Class<? extends IColumnTranspiler> clazz) {
		IColumnTranspiler instance = null;
		if (IColumnTranspiler.class.isAssignableFrom(clazz)) {
			try {
				instance = (IColumnTranspiler) Class.forName(clazz.getName()).getDeclaredConstructor((Class<?>[]) null)
						.newInstance((Object[]) null);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				return null;
			}
		}
		return instance;
	}

	private static Class<? extends IColumnTranspiler> getClazz(String className) {
		try {
			return forName(className).asSubclass(IColumnTranspiler.class);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
}
