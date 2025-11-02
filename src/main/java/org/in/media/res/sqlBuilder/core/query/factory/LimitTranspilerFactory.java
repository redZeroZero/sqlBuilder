package org.in.media.res.sqlBuilder.core.query.factory;

import static java.lang.Class.forName;

import java.lang.reflect.InvocationTargetException;

import org.in.media.res.sqlBuilder.api.query.LimitTranspiler;

public class LimitTranspilerFactory {

 private static final String DEFAULT_NAME = "org.in.media.res.sqlBuilder.core.query.transpiler.oracle.";

 public static LimitTranspiler instanciateLimitTranspiler() {
  return instanciateLimitTranspiler(getClazz(DEFAULT_NAME + "OracleLimitTranspilerImpl"));
 }

 private static LimitTranspiler instanciateLimitTranspiler(Class<? extends LimitTranspiler> clazz) {
  LimitTranspiler instance = null;
  if (LimitTranspiler.class.isAssignableFrom(clazz)) {
   try {
    instance = (LimitTranspiler) Class.forName(clazz.getName()).getDeclaredConstructor((Class<?>[]) null)
      .newInstance((Object[]) null);
   } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
     | InvocationTargetException | NoSuchMethodException | SecurityException e) {
    return null;
   }
  }
  return instance;
 }

 private static Class<? extends LimitTranspiler> getClazz(String className) {
  try {
   return forName(className).asSubclass(LimitTranspiler.class);
  } catch (ClassNotFoundException e) {
   throw new RuntimeException(e);
  }
 }
}
