package org.in.media.res.sqlBuilder.implementation.transpilers.clauses;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;

import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.model.ITable;

final class SqlBuilder {

	private final StringBuilder delegate;

	SqlBuilder() {
		this.delegate = new StringBuilder();
	}

	SqlBuilder append(String value) {
		delegate.append(value);
		return this;
	}

	SqlBuilder append(char value) {
		delegate.append(value);
		return this;
	}

	SqlBuilder appendColumn(IColumn column) {
		delegate.append(column.transpile(false));
		return this;
	}

	SqlBuilder appendTable(ITable table) {
		delegate.append(table.getName());
		return this;
	}

    <T> SqlBuilder join(Collection<T> items, String separator, Consumer<T> appender) {
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

	static SqlBuilder from(String prefix) {
		return new SqlBuilder().append(prefix);
	}
}
