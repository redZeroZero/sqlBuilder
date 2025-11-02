package org.in.media.res.sqlBuilder.api.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SqlTable {
	String name() default "";
	String alias() default "";
	String schema() default "";
}
