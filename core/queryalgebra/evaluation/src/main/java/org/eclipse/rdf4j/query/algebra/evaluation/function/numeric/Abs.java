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
package org.eclipse.rdf4j.query.algebra.evaluation.function.numeric;

import java.math.BigDecimal;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.datatypes.XMLDatatypeUtil;
import org.eclipse.rdf4j.model.vocabulary.FN;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.function.Function;

/**
 * The SPARQL built-in {@link Function} ABS, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-abs">SPARQL Query Language
 * for RDF</a>
 * 
 * @author Jeen Broekstra
 */
public class Abs implements Function {

	public String getURI() {
		return FN.NUMERIC_ABS.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 1) {
			throw new ValueExprEvaluationException("ABS requires exactly 1 argument, got " + args.length);
		}

		if (args[0] instanceof Literal) {
			Literal literal = (Literal)args[0];

			IRI datatype = literal.getDatatype();
			
			// ABS function accepts only numeric literals
			if (datatype != null && XMLDatatypeUtil.isNumericDatatype(datatype))
			{
				if (XMLDatatypeUtil.isDecimalDatatype(datatype)) {
					BigDecimal absoluteValue = literal.decimalValue().abs();
					
					return valueFactory.createLiteral(absoluteValue.toPlainString(), datatype);
				}
				else if (XMLDatatypeUtil.isFloatingPointDatatype(datatype)) {
					double absoluteValue = Math.abs(literal.doubleValue());
					return valueFactory.createLiteral(Double.toString(absoluteValue), datatype);
				}
				else {
					throw new ValueExprEvaluationException("unexpected datatype for function operand: " + args[0]);
				}
			}
			else {
				throw new ValueExprEvaluationException("unexpected input value for function: " + args[0]);
			}
		}
		else {
			throw new ValueExprEvaluationException("unexpected input value for function: " + args[0]);
		}

	}

}