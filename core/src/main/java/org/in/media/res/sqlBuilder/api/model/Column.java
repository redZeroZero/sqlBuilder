package org.in.media.res.sqlBuilder.api.model;

import org.in.media.res.sqlBuilder.api.query.spi.Transpilable;

public interface Column extends Transpilable {

	String transpile(boolean useAlias);

	Table table();

	String getName();

	String getAlias();

	boolean hasColumnAlias();

}
