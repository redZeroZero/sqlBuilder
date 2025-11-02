package org.in.media.res.sqlBuilder.core.query.factory;

import static java.lang.Class.forName;

import java.lang.reflect.InvocationTargetException;

import org.in.media.res.sqlBuilder.api.query.GroupByTranspiler;

public class GroupByTranspilerFactory {

	private static final String DEFAULT_NAME = "org.in.media.res.sqlBuilder.core.query.transpiler.oracle.";

	public static GroupByTranspiler instanciateGroupByTranspiler() {
		return instanciateGroupByTranspiler(getClazz(DEFAULT_NAME + "OracleGroupByTranspilerImpl"));
	}

	private static GroupByTranspiler instanciateGroupByTranspiler(Class<? extends GroupByTranspiler> clazz) {
		GroupByTranspiler instance = null;
		if (GroupByTranspiler.class.isAssignableFrom(clazz)) {
			try {
				instance = (GroupByTranspiler) Class.forName(clazz.getName()).getDeclaredConstructor((Class<?>[]) null)
						.newInstance((Object[]) null);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				return null;
			}
		}
		return instance;
	}

	private static Class<? extends GroupByTranspiler> getClazz(String className) {
		try {
			return forName(className).asSubclass(GroupByTranspiler.class);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
