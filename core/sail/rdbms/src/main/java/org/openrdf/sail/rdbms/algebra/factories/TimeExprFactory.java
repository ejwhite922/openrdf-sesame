/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.algebra.factories;

import static org.openrdf.sail.rdbms.algebra.base.SqlExprSupport.sqlNull;
import static org.openrdf.sail.rdbms.algebra.base.SqlExprSupport.unsupported;
import static org.openrdf.sail.rdbms.managers.LiteralManager.getCalendarValue;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.query.algebra.Datatype;
import org.openrdf.query.algebra.Lang;
import org.openrdf.query.algebra.MathExpr;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.Str;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.sail.rdbms.algebra.DateTimeColumn;
import org.openrdf.sail.rdbms.algebra.NumberValue;
import org.openrdf.sail.rdbms.algebra.SqlNull;
import org.openrdf.sail.rdbms.algebra.base.SqlExpr;
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;

/**
 * Creates an SQL expression for a literal's time value.
 * 
 * @author James Leigh
 * 
 */
public class TimeExprFactory extends
		QueryModelVisitorBase<UnsupportedRdbmsOperatorException> {
	protected SqlExpr result;

	public SqlExpr createTimeExpr(ValueExpr expr)
			throws UnsupportedRdbmsOperatorException {
		result = null;
		if (expr == null)
			return new SqlNull();
		expr.visit(this);
		if (result == null)
			return new SqlNull();
		return result;
	}

	@Override
	public void meet(Datatype node) {
		result = sqlNull();
	}

	@Override
	public void meet(Lang node) throws UnsupportedRdbmsOperatorException {
		result = sqlNull();
	}

	@Override
	public void meet(MathExpr node) throws UnsupportedRdbmsOperatorException {
		result = sqlNull();
	}

	@Override
	public void meet(Str node) {
		result = sqlNull();
	}

	@Override
	public void meet(ValueConstant vc) {
		result = valueOf(vc.getValue());
	}

	@Override
	public void meet(Var node) {
		if (node.getValue() == null) {
			result = new DateTimeColumn(node);
		} else {
			result = valueOf(node.getValue());
		}
	}

	@Override
	protected void meetNode(QueryModelNode arg)
			throws UnsupportedRdbmsOperatorException {
		throw unsupported(arg);
	}

	private SqlExpr valueOf(Value value) {
		if (value instanceof Literal) {
			Literal lit = (Literal) value;
			URI dt = lit.getDatatype();
			if (dt != null && XMLDatatypeUtil.isCalendarDatatype(dt)) {
				try {
					return new NumberValue(getCalendarValue(lit.calendarValue()));
				} catch (IllegalArgumentException e) {
					return null;
				}
			}
		}
		return null;
	}
}
