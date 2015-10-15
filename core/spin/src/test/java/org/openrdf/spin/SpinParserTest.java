package org.openrdf.spin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.evaluation.ModelTripleSource;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.vocabulary.SP;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.parser.ParsedOperation;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedUpdate;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;

@RunWith(Parameterized.class)
public class SpinParserTest {

	@Parameters(name="{0}")
	public static Collection<Object[]> testData() {
		List<Object[]> params = new ArrayList<Object[]>();
		for(int i=0; ; i++) {
			String suffix = String.valueOf(i+1);
			String testFile = "/testcases/test"+suffix+".ttl";
			URL rdfURL = SpinParserTest.class.getResource(testFile);
			if(rdfURL == null) {
				break;
			}
			params.add(new Object[] {testFile, rdfURL});
		}
		return params;
	}

	private final URL testURL;
	private final SpinParser textParser = new SpinParser(SpinParser.Input.TEXT_ONLY);
	private final SpinParser rdfParser = new SpinParser(SpinParser.Input.RDF_ONLY);

	public SpinParserTest(String testName, URL testURL) {
		this.testURL = testURL;
	}

	@Test
	public void testSpinParser() throws IOException, OpenRDFException {
		StatementCollector expected = new StatementCollector();
		RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
		parser.setRDFHandler(expected);
		InputStream rdfStream = testURL.openStream();
		parser.parse(rdfStream, testURL.toString());
		rdfStream.close();

		// get query resource from sp:text
		Resource queryResource = null;
		for(Statement stmt : expected.getStatements()) {
			if(SP.TEXT_PROPERTY.equals(stmt.getPredicate())) {
				queryResource = stmt.getSubject();
				break;
			}
		}
		assertNotNull(queryResource);

		TripleSource store = new ModelTripleSource(new TreeModel(expected.getStatements()));
		ParsedOperation textParsedOp = textParser.parse(queryResource, store);
		ParsedOperation rdfParsedOp = rdfParser.parse(queryResource, store);

		if(textParsedOp instanceof ParsedQuery) {
			assertEquals(((ParsedQuery)textParsedOp).getTupleExpr(), ((ParsedQuery)rdfParsedOp).getTupleExpr());
		}
		else {
			assertEquals(((ParsedUpdate)textParsedOp).getUpdateExprs(), ((ParsedUpdate)rdfParsedOp).getUpdateExprs());
		}
	}
}