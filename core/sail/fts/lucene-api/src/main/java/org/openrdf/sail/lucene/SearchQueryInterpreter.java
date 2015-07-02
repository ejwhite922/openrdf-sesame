/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.sail.lucene;

import java.util.Collection;

import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.sail.SailException;

public interface SearchQueryInterpreter {
	void setIncompleteQueryFails(boolean f);
	/**
	 * Processes a TupleExpr into a set of SearchQueryEvaluators.
	 * @param tupleExpr the TupleExpr to process.
	 * @param bindings any bindings.
	 * @param specs the Collection to add any SearchQueryEvaluators to.
	 */
	void process(TupleExpr tupleExpr, BindingSet bindings, Collection<SearchQueryEvaluator> specs) throws SailException;
}
