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
package org.openrdf.sail.spin;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.function.FunctionRegistry;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.spin.AskFunction;
import org.openrdf.spin.SPINParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SPINFunctionPreparer implements QueryOptimizer {
	private static final Logger logger = LoggerFactory.getLogger(SPINFunctionPreparer.class);

	private final TripleSource tripleSource;
	private final SPINParser parser;
	private final FunctionRegistry functionRegistry;

	public SPINFunctionPreparer(TripleSource tripleSource, SPINParser parser, FunctionRegistry functionRegistry) {
		this.tripleSource = tripleSource;
		this.parser = parser;
		this.functionRegistry = functionRegistry;
		functionRegistry.add(new org.openrdf.sail.spin.function.Concat());
		functionRegistry.add(new AskFunction(parser));
	}

	@Override
	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new FunctionScanner());
	}



	class FunctionScanner extends QueryModelVisitorBase<RuntimeException> {
		ValueFactory vf = tripleSource.getValueFactory();

		@Override
		public void meet(FunctionCall node)
			throws RuntimeException
		{
			String name = node.getURI();
			if(!functionRegistry.has(name)) {
				URI funcUri = vf.createURI(name);
				try {
					Function f = parser.parseFunction(funcUri, tripleSource);
					functionRegistry.add(f);
				}
				catch(OpenRDFException e) {
					logger.warn("Failed to parse function: {}", funcUri);
				}
			}
			super.meet(node);
		}
	}
}
