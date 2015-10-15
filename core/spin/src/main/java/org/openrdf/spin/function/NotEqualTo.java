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
package org.openrdf.spin.function;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.model.vocabulary.SP;
import org.openrdf.query.algebra.Compare.CompareOp;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.BinaryFunction;
import org.openrdf.query.algebra.evaluation.util.QueryEvaluationUtil;

public class NotEqualTo extends BinaryFunction {

	@Override
	public String getURI() {
		return SP.NE.toString();
	}

	@Override
	protected Value evaluate(ValueFactory valueFactory, Value arg1, Value arg2)
		throws ValueExprEvaluationException
	{
		return BooleanLiteralImpl.valueOf(QueryEvaluationUtil.compare(arg1, arg2, CompareOp.NE));
	}
}