package org.in.media.res.sqlBuilder.api.query;

import java.util.function.Consumer;

import org.in.media.res.sqlBuilder.core.query.ConditionGroupBuilder;
import org.in.media.res.sqlBuilder.core.query.predicate.ConditionGroup;

public final class QueryHelper {

    private QueryHelper() {
    }

    public static ConditionGroupBuilder createGroup() {
        return new ConditionGroupBuilder();
    }

    public static ConditionGroup group(Consumer<ConditionGroupBuilder> consumer) {
        ConditionGroupBuilder builder = new ConditionGroupBuilder();
        consumer.accept(builder);
        return builder.build();
    }
}
