package org.in.media.res.sqlBuilder.implementation.factories;

import static java.lang.Class.forName;

import java.lang.reflect.InvocationTargetException;

import org.in.media.res.sqlBuilder.interfaces.query.ILimitTranspiler;

public class LimitTranspilerFactory {

 private static final String DEFAULT_NAME = "org.in.media.res.sqlBuilder.implementation.transpilers.clauses.";

 public static ILimitTranspiler instanciateLimitTranspiler() {
  return instanciateLimitTranspiler(getClazz(DEFAULT_NAME + "OracleLimitTranspilerImpl"));
 }

 private static ILimitTranspiler instanciateLimitTranspiler(Class<? extends ILimitTranspiler> clazz) {
  ILimitTranspiler instance = null;
  if (ILimitTranspiler.class.isAssignableFrom(clazz)) {
   try {
    instance = (ILimitTranspiler) Class.forName(clazz.getName()).getDeclaredConstructor((Class<?>[]) null)
      .newInstance((Object[]) null);
   } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
     | InvocationTargetException | NoSuchMethodException | SecurityException e) {
    return null;
   }
  }
  return instance;
 }

 private static Class<? extends ILimitTranspiler> getClazz(String className) {
  try {
   return forName(className).asSubclass(ILimitTranspiler.class);
  } catch (ClassNotFoundException e) {
   throw new RuntimeException(e);
  }
 }
}
