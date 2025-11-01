package org.in.media.res.sqlBuilder.implementation.factories;

import static java.lang.Class.forName;

import java.lang.reflect.InvocationTargetException;

import org.in.media.res.sqlBuilder.interfaces.query.IGroupByTranspiler;

public class GroupByTranspilerFactory {

	private static final String DEFAULT_NAME = "org.in.media.res.sqlBuilder.implementation.transpilers.clauses.";

	public static IGroupByTranspiler instanciateGroupByTranspiler() {
		return instanciateGroupByTranspiler(getClazz(DEFAULT_NAME + "OracleGroupByTranspilerImpl"));
	}

	private static IGroupByTranspiler instanciateGroupByTranspiler(Class<? extends IGroupByTranspiler> clazz) {
		IGroupByTranspiler instance = null;
		if (IGroupByTranspiler.class.isAssignableFrom(clazz)) {
			try {
				instance = (IGroupByTranspiler) Class.forName(clazz.getName()).getDeclaredConstructor((Class<?>[]) null)
						.newInstance((Object[]) null);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				return null;
			}
		}
		return instance;
	}

	private static Class<? extends IGroupByTranspiler> getClazz(String className) {
		try {
			return forName(className).asSubclass(IGroupByTranspiler.class);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
