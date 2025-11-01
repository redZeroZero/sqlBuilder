package org.in.media.res.sqlBuilder.interfaces.query;

import java.util.Date;

import org.in.media.res.sqlBuilder.interfaces.model.IColumn;

public interface IHavingBuilder {

	IHaving eq(String value);

	IHaving eq(Number value);

	IHaving eq(Date value);

	IHaving in(String... values);

	IHaving in(Number... values);

	IHaving supTo(Number value);

	IHaving supOrEqTo(Number value);

	IHaving infTo(Number value);

	IHaving infOrEqTo(Number value);

	IHaving supTo(IColumn column);

	IHaving supOrEqTo(IColumn column);

	IHaving infTo(IColumn column);

	IHaving infOrEqTo(IColumn column);

	IHavingBuilder and(IColumn column);

	IHavingBuilder or(IColumn column);

	IHavingBuilder min(IColumn column);

	IHavingBuilder max(IColumn column);

	IHavingBuilder sum(IColumn column);

	IHavingBuilder avg(IColumn column);

	IHavingBuilder col(IColumn column);
}
