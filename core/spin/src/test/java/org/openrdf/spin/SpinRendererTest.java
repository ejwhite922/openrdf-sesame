package org.openrdf.spin;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.output.StringBuilderWriter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Statement;
import org.openrdf.model.util.Models;
import org.openrdf.model.vocabulary.SP;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.ParsedOperation;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedUpdate;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.WriterConfig;
import org.openrdf.rio.helpers.BasicWriterSettings;
import org.openrdf.rio.helpers.StatementCollector;

@RunWith(Parameterized.class)
public class SpinRendererTest {

	@Parameters(name="{0}")
	public static Collection<Object[]> testData() {
		List<Object[]> params = new ArrayList<Object[]>();
		for(int i=0; ; i++) {
			String suffix = String.valueOf(i+1);
			String testFile = "/testcases/test"+suffix+".ttl";
			URL rdfURL = SpinRendererTest.class.getResource(testFile);
			if(rdfURL == null) {
				break;
			}
			params.add(new Object[] {testFile, rdfURL});
		}
		return params;
	}

	private final URL testURL;
	private final SpinRenderer renderer = new SpinRenderer();

	public SpinRendererTest(String testName, URL testURL) {
		this.testURL = testURL;
	}

	@Test
	public void testSpinRenderer() throws IOException, OpenRDFException {
		StatementCollector expected = new StatementCollector();
		RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
		parser.setRDFHandler(expected);
		InputStream rdfStream = testURL.openStream();
		parser.parse(rdfStream, testURL.toString());
		rdfStream.close();

		// get query from sp:text
		String query = null;
		for(Statement stmt : expected.getStatements()) {
			if(SP.TEXT_PROPERTY.equals(stmt.getPredicate())) {
				query = stmt.getObject().stringValue();
				break;
			}
		}
		assertNotNull(query);

		ParsedOperation parsedOp = QueryParserUtil.parseOperation(QueryLanguage.SPARQL, query, testURL.toString());

		StatementCollector actual = new StatementCollector();
		renderer.render(parsedOp, actual);

		Object operation = (parsedOp instanceof ParsedQuery) ? ((ParsedQuery)parsedOp).getTupleExpr() : ((ParsedUpdate)parsedOp).getUpdateExprs();
		assertTrue("Operation was\n"+operation+"\nExpected\n"+toRDF(expected)+"\nbut was\n"+toRDF(actual), Models.isomorphic(actual.getStatements(), expected.getStatements()));
	}

	private static String toRDF(StatementCollector stmts) throws RDFHandlerException
	{
		WriterConfig config = new WriterConfig();
		config.set(BasicWriterSettings.PRETTY_PRINT, false);
		StringBuilderWriter writer = new StringBuilderWriter();
		final RDFWriter rdfWriter = Rio.createWriter(RDFFormat.TURTLE, writer);
		rdfWriter.setWriterConfig(config);

		rdfWriter.startRDF();
		for (Map.Entry<String,String> entry : stmts.getNamespaces().entrySet()) {
			rdfWriter.handleNamespace(entry.getKey(), entry.getValue());
		}
		for (final Statement st : stmts.getStatements()) {
			rdfWriter.handleStatement(st);
		}
		rdfWriter.endRDF();

		writer.close();
		return writer.toString();
	}
}