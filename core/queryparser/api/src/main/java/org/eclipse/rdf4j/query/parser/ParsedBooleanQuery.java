/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.query.parser;

import org.eclipse.rdf4j.query.algebra.TupleExpr;

/**
 * A query formulated in the OpenRDF query algebra that produces a boolean value
 * as its result.
 * 
 * @author Arjohn Kampman
 */
public class ParsedBooleanQuery extends ParsedQuery {

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new boolean query. To complete this query, a tuple expression
	 * needs to be supplied to it using {@link #setTupleExpr(TupleExpr)}.
	 */
	public ParsedBooleanQuery() {
		super();
	}

	/**
	 * Creates a new boolean query for the supplied tuple expression.
	 * 
	 * @param tupleExpr
	 *        A tuple expression representing the query, formulated in OpenRDF
	 *        Query Algebra objects.
	 */
	public ParsedBooleanQuery(TupleExpr tupleExpr) {
		super(tupleExpr);
	}
	
	/**
	 * Creates a new boolean query for the supplied tuple expression.
	 * 
	 * @param tupleExpr
	 *        A tuple expression representing the query, formulated in OpenRDF
	 *        Query Algebra objects.
	 */
	public ParsedBooleanQuery(String sourceString, TupleExpr tupleExpr) {
		super(sourceString, tupleExpr);
	}
}