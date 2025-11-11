package org.in.media.res.sqlBuilder.core.query.transpiler.defaults;

import java.util.ArrayList;
import java.util.List;

import org.in.media.res.sqlBuilder.api.query.spi.OrderBy;
import org.in.media.res.sqlBuilder.api.query.spi.OrderBy.Ordering;
import org.in.media.res.sqlBuilder.api.query.spi.OrderByTranspiler;
import org.in.media.res.sqlBuilder.api.query.RawSqlFragment;
import org.in.media.res.sqlBuilder.core.query.OrderByRawSupport;

public class DefaultOrderByTranspiler implements OrderByTranspiler {

    private static final String ORDER_BY = " ORDER BY ";
    private static final String SEP = ", ";

    @Override
    public String transpile(OrderBy orderBy) {
        List<String> segments = new ArrayList<>();
        for (Ordering ordering : orderBy.orderings()) {
            segments.add(ordering.column().transpile(false) + ' ' + ordering.direction().value());
        }
		if (orderBy instanceof OrderByRawSupport support) {
			for (RawSqlFragment fragment : support.orderByFragments()) {
				segments.add(fragment.sql());
			}
		}
        if (segments.isEmpty()) {
            return "";
        }
        DefaultSqlBuilder builder = DefaultSqlBuilder.from(ORDER_BY);
        builder.append(segments.getFirst());
        for (int i = 1; i < segments.size(); i++) {
            builder.append(SEP).append(segments.get(i));
        }
        return builder.toString();
    }
}
