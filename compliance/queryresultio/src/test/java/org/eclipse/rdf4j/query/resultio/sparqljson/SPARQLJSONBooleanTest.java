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
package org.eclipse.rdf4j.query.resultio.sparqljson;

import static org.junit.Assert.*;

import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.resultio.AbstractQueryResultIOBooleanTest;
import org.eclipse.rdf4j.query.resultio.BooleanQueryResultFormat;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultFormat;
import org.eclipse.rdf4j.query.resultio.helpers.QueryResultCollector;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLBooleanJSONParser;
import org.junit.Test;

/**
 * @author Peter Ansell
 * @author Sebastian Schaffert
 */
public class SPARQLJSONBooleanTest extends AbstractQueryResultIOBooleanTest {

	@Override
	protected String getFileName() {
		return "test.srj";
	}

	@Override
	protected BooleanQueryResultFormat getBooleanFormat() {
		return BooleanQueryResultFormat.JSON;
	}

	@Override
	protected TupleQueryResultFormat getMatchingTupleFormatOrNull() {
		return TupleQueryResultFormat.JSON;
	}

	@Test
	public void testBoolean1()
		throws Exception
	{
		SPARQLBooleanJSONParser parser = new SPARQLBooleanJSONParser(SimpleValueFactory.getInstance());
		QueryResultCollector handler = new QueryResultCollector();
		parser.setQueryResultHandler(handler);

		parser.parseQueryResult(this.getClass().getResourceAsStream("/sparqljson/boolean1.srj"));

		assertTrue(handler.getBoolean());
	}

	@Test
	public void testBoolean2()
		throws Exception
	{
		SPARQLBooleanJSONParser parser = new SPARQLBooleanJSONParser(SimpleValueFactory.getInstance());
		QueryResultCollector handler = new QueryResultCollector();
		parser.setQueryResultHandler(handler);

		parser.parseQueryResult(this.getClass().getResourceAsStream("/sparqljson/boolean2.srj"));

		assertTrue(handler.getBoolean());
	}

	@Test
	public void testBoolean3()
		throws Exception
	{
		SPARQLBooleanJSONParser parser = new SPARQLBooleanJSONParser(SimpleValueFactory.getInstance());
		QueryResultCollector handler = new QueryResultCollector();
		parser.setQueryResultHandler(handler);

		parser.parseQueryResult(this.getClass().getResourceAsStream("/sparqljson/boolean3.srj"));

		assertFalse(handler.getBoolean());
	}

	@Test
	public void testBoolean4()
		throws Exception
	{
		SPARQLBooleanJSONParser parser = new SPARQLBooleanJSONParser(SimpleValueFactory.getInstance());
		QueryResultCollector handler = new QueryResultCollector();
		parser.setQueryResultHandler(handler);

		parser.parseQueryResult(this.getClass().getResourceAsStream("/sparqljson/boolean4.srj"));

		assertFalse(handler.getBoolean());
	}

}