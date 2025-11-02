package org.in.media.res.sqlBuilder.api.query;

import java.util.List;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;

public interface Having extends Clause, Resetable, Transpilable {

	Having having(Condition condition);

	HavingBuilder having(Column column);

	default HavingBuilder having(TableDescriptor<?> descriptor) {
		return having(descriptor.column());
	}

	Having and(Condition condition);

	Having or(Condition condition);

	List<Condition> havingConditions();
}
