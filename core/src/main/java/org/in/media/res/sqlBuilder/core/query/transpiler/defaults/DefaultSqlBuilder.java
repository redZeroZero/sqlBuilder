package org.in.media.res.sqlBuilder.core.query.transpiler.defaults;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;

final class DefaultSqlBuilder {

	private final StringBuilder delegate;

	DefaultSqlBuilder() {
		this.delegate = new StringBuilder();
	}

	DefaultSqlBuilder append(String value) {
		delegate.append(value);
		return this;
	}

	DefaultSqlBuilder append(char value) {
		delegate.append(value);
		return this;
	}

	DefaultSqlBuilder appendColumn(Column column) {
		delegate.append(column.transpile(false));
		return this;
	}

	DefaultSqlBuilder appendTable(Table table) {
		delegate.append(org.in.media.res.sqlBuilder.core.query.dialect.DialectContext.current().quoteIdent(table.getName()));
		return this;
	}

    <T> DefaultSqlBuilder join(Collection<T> items, String separator, Consumer<T> appender) {
        Iterator<T> iterator = items.iterator();
        while (iterator.hasNext()) {
            appender.accept(iterator.next());
            if (iterator.hasNext()) {
                delegate.append(separator);
            }
        }
        return this;
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	static DefaultSqlBuilder from(String prefix) {
		return new DefaultSqlBuilder().append(prefix);
	}
}
