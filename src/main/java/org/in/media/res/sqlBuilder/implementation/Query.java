package org.in.media.res.sqlBuilder.implementation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.implementation.factories.SqlCLausesFactory;
import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.model.ITable;
import org.in.media.res.sqlBuilder.interfaces.query.IAggregator;
import org.in.media.res.sqlBuilder.interfaces.query.IClause;
import org.in.media.res.sqlBuilder.interfaces.query.IComparator;
import org.in.media.res.sqlBuilder.interfaces.query.ICondition;
import org.in.media.res.sqlBuilder.interfaces.query.IConnector;
import org.in.media.res.sqlBuilder.interfaces.query.IFrom;
import org.in.media.res.sqlBuilder.interfaces.query.IJoinable;
import org.in.media.res.sqlBuilder.interfaces.query.IQuery;
import org.in.media.res.sqlBuilder.interfaces.query.ISelect;
import org.in.media.res.sqlBuilder.interfaces.query.ITranspilable;
import org.in.media.res.sqlBuilder.interfaces.query.IWhere;

public class Query implements IQuery, ITranspilable, IJoinable {

	private ISelect selectClause = SqlCLausesFactory.instanciateSelect();

	private IFrom fromClause = SqlCLausesFactory.instanciateFrom();

	private IWhere whereClause = SqlCLausesFactory.instanciateWhere();

	private StringBuilder sb = new StringBuilder();

	public String transpile() {
		resetBuilder();
		sb.append(selectClause.transpile());
		sb.append(fromClause.transpile());
		sb.append(whereClause.transpile());
		return sb.toString();
	}

	public void reset() {

	}

	@Override
	public IQuery select(IColumn column) {
		this.selectClause.select(column);
		return this;
	}

	@Override
	public IQuery select(IColumn... columns) {
		this.selectClause.select(columns);
		return this;
	}

	@Override
	public IQuery select(ITable table) {
		this.selectClause.select(table);
		return this;
	}

	@Override
	public IQuery select(AggregateOperator agg, IColumn column) {
		this.selectClause.select(agg, column);
		return this;
	}

	@Override
	public IQuery on(IColumn c1, IColumn c2) {
		this.fromClause.on(c1, c2);
		return this;
	}

	@Override
	public IQuery from(ITable table) {
		this.fromClause.from(table);
		return this;
	}

	@Override
	public IQuery from(ITable... tables) {
		this.from(tables);
		return this;
	}

	@Override
	public IQuery join(ITable t) {
		this.fromClause.join(t);
		return this;
	}

	@Override
	public IQuery innerJoin(ITable t) {
		this.fromClause.innerJoin(t);
		return this;
	}

	@Override
	public IQuery leftJoin(ITable t) {
		this.fromClause.leftJoin(t);
		return this;
	}

	@Override
	public IQuery rightJoin(ITable t) {
		this.fromClause.rightJoin(t);
		return this;
	}

	private void resetBuilder() {
		sb.setLength(0);
	}

	@Override
	public List<IClause> clauses() {
		List<IClause> c = new ArrayList<>();
		c.add(selectClause);
		c.add(fromClause);
		c.add(whereClause);
		return c;
	}

	@Override
	public IWhere where(IColumn column) {
		this.whereClause.where(column);
		return this;
	}

	@Override
	public IConnector eq(IColumn column) {
		this.whereClause.eq(column);
		return this;
	}

	@Override
	public IConnector supTo(IColumn column) {
		this.whereClause.supTo(column);
		return this;
	}

	@Override
	public IConnector infTo(IColumn column) {
		this.whereClause.infTo(column);
		return this;
	}

	@Override
	public IConnector supOrEqTo(IColumn column) {
		this.whereClause.supOrEqTo(column);
		return this;
	}

	@Override
	public IConnector infOrEqTo(IColumn column) {
		this.whereClause.infOrEqTo(column);
		return this;
	}

	@Override
	public IConnector eq(String value) {
		this.whereClause.eq(value);
		return this;
	}

	@Override
	public IConnector supTo(String value) {
		this.whereClause.supTo(value);
		return this;
	}

	@Override
	public IConnector infTo(String value) {
		this.whereClause.infTo(value);
		return this;
	}

	@Override
	public IConnector supOrEqTo(String value) {
		this.whereClause.supOrEqTo(value);
		return this;
	}

	@Override
	public IConnector infOrEqTo(String value) {
		this.whereClause.infOrEqTo(value);
		return this;
	}

	@Override
	public IConnector in(String... value) {
		this.whereClause.in(value);
		return this;
	}

	@Override
	public IConnector eq(Integer value) {
		this.whereClause.eq(value);
		return this;
	}

	@Override
	public IConnector supTo(Integer value) {
		this.whereClause.supTo(value);
		return this;
	}

	@Override
	public IConnector infTo(Integer value) {
		this.whereClause.infTo(value);
		return this;
	}

	@Override
	public IConnector supOrEqTo(Integer value) {
		this.whereClause.supOrEqTo(value);
		return this;
	}

	@Override
	public IConnector infOrEqTo(Integer value) {
		this.whereClause.infOrEqTo(value);
		return this;
	}

	@Override
	public IConnector in(Integer... value) {
		this.whereClause.in(value);
		return this;
	}

	@Override
	public IConnector eq(Date value) {
		this.whereClause.eq(value);
		return this;
	}

	@Override
	public IConnector supTo(Date value) {
		this.whereClause.supTo(value);
		return this;
	}

	@Override
	public IConnector infTo(Date value) {
		this.whereClause.infTo(value);
		return this;
	}

	@Override
	public IConnector supOrEqTo(Date value) {
		this.whereClause.supOrEqTo(value);
		return this;
	}

	@Override
	public IConnector infOrEqTo(Date value) {
		this.whereClause.infOrEqTo(value);
		return this;
	}

	@Override
	public IConnector in(Date... value) {
		this.whereClause.in(value);
		return this;
	}

	@Override
	public IConnector eq(Double value) {
		this.whereClause.eq(value);
		return this;
	}

	@Override
	public IConnector supTo(Double value) {
		this.whereClause.supTo(value);
		return this;
	}

	@Override
	public IConnector infTo(Double value) {
		this.whereClause.infTo(value);
		return this;
	}

	@Override
	public IConnector supOrEqTo(Double value) {
		this.whereClause.supOrEqTo(value);
		return this;
	}

	@Override
	public IConnector infOrEqTo(Double value) {
		this.whereClause.infOrEqTo(value);
		return this;
	}

	@Override
	public IConnector in(Double... value) {
		this.whereClause.in(value);
		return this;
	}

	@Override
	public IWhere and(IColumn column) {
		this.whereClause.and(column);
		return this;
	}

	@Override
	public IWhere or(IColumn column) {
		this.whereClause.or(column);
		return null;
	}

	@Override
	public IWhere and() {
		this.whereClause.and();
		return this;
	}

	@Override
	public IWhere or() {
		this.whereClause.or();
		return null;
	}

	@Override
	public IAggregator eq() {
		this.whereClause.eq();
		return this;
	}

	@Override
	public IAggregator supTo() {
		this.whereClause.supTo();
		return this;
	}

	@Override
	public IAggregator infTo() {
		this.whereClause.infTo();
		return this;
	}

	@Override
	public IAggregator supOrEqTo() {
		this.whereClause.supOrEqTo();
		return this;
	}

	@Override
	public IAggregator infOrEqTo() {
		this.whereClause.infOrEqTo();
		return this;
	}

	@Override
	public IAggregator in() {
		this.whereClause.in();
		return this;
	}

	@Override
	public IComparator min(IColumn column) {
		this.whereClause.min(column);
		return this;
	}

	@Override
	public IComparator max(IColumn column) {
		this.whereClause.max(column);
		return this;
	}

	@Override
	public IComparator sum(IColumn column) {
		this.whereClause.sum(column);
		return this;
	}

	@Override
	public IComparator avg(IColumn column) {
		this.whereClause.avg(column);
		return this;
	}

	@Override
	public IComparator col(IColumn column) {
		this.whereClause.col(column);
		return this;
	}

	@Override
	public IWhere condition(ICondition condition) {
		this.whereClause.condition(condition);
		return this;
	}

}
