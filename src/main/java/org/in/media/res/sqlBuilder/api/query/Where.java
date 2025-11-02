package org.in.media.res.sqlBuilder.api.query;

import java.util.List;

public interface Where extends Comparator, Connector, Aggregator, Clause, Transpilable {

	List<Condition> conditions();

}
