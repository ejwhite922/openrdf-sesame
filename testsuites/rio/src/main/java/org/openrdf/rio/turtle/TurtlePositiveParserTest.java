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
package org.openrdf.rio.turtle;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.util.ModelUtil;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.StatementCollector;

public class TurtlePositiveParserTest extends TestCase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private String inputURL;

	private String outputURL;

	private String baseURL;

	private RDFParser targetParser;

	private RDFParser ntriplesParser;

	protected URI testUri;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public TurtlePositiveParserTest(URI testUri, String testName, String inputURL, String outputURL,
			String baseURL, RDFParser targetParser, RDFParser ntriplesParser)
		throws MalformedURLException
	{
		super(testName);
		this.testUri = testUri;
		this.inputURL = inputURL;
		if (outputURL != null) {
			this.outputURL = outputURL;
		}
		this.baseURL = baseURL;
		this.targetParser = targetParser;
		this.ntriplesParser = ntriplesParser;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected void runTest()
		throws Exception
	{
		// Parse input data
		// targetParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);

		Set<Statement> inputCollection = new LinkedHashSet<Statement>();
		StatementCollector inputCollector = new StatementCollector(inputCollection);
		targetParser.setRDFHandler(inputCollector);

//		if (!this.getName().equals("reserved_escaped_localName")) {
//			return;
//		}
		System.out.println("");
		System.out.println("test: " + inputURL);

		InputStream in = this.getClass().getResourceAsStream(inputURL);
		assertNotNull("Test resource was not found: inputURL=" + inputURL, in);

		try {
			targetParser.parse(in, baseURL);
		}
		catch (RDFParseException rdfpe) {
			System.out.println("");
			System.out.println(rdfpe.getMessage());
			throw rdfpe;
		}
		finally {
			in.close();
		}

		if (outputURL != null) {
			// Parse expected output data
			ntriplesParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);

			Set<Statement> outputCollection = new LinkedHashSet<Statement>();
			StatementCollector outputCollector = new StatementCollector(outputCollection);
			ntriplesParser.setRDFHandler(outputCollector);

			in = this.getClass().getResourceAsStream(outputURL);
			ntriplesParser.parse(in, baseURL);
			in.close();

			// Check equality of the two models
			if (!ModelUtil.equals(inputCollection, outputCollection)) {
				System.err.println("===models not equal===");
				System.err.println("Expected: " + outputCollection);
				System.err.println("Actual  : " + inputCollection);
				System.err.println("outputURL=" + outputURL);
				System.err.println("======================");

				fail("models not equal");
			}
		}
	}

} // end inner class TurtlePositiveParserTest