package org.openrdf.sail.spin;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.sail.NotifyingSail;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.openrdf.sail.memory.MemoryStore;

public class SPINSailTest {
	@Rule
	public ExpectedException constraintException = ExpectedException.none();

	private Repository repo;
	private RepositoryConnection conn;

	@Before
	public void setup() throws RepositoryException {
		NotifyingSail baseSail = new MemoryStore();
		URI axiomContext = ValueFactoryImpl.getInstance().createURI("sesame:axioms");
		ForwardChainingRDFSInferencer rdfsInferencer = new ForwardChainingRDFSInferencer(baseSail);
		rdfsInferencer.setAxiomContext(axiomContext);
		rdfsInferencer.setContextPreserved(true);
		SPINSail spinSail = new SPINSail(rdfsInferencer);
		spinSail.setAxiomContext(axiomContext);
		repo = new SailRepository(spinSail);
		repo.initialize();
		conn = repo.getConnection();
	}

	@After
	public void tearDown() throws RepositoryException {
		conn.close();
		repo.shutDown();
	}

	@Test
	public void testAskConstraint() throws Exception {
		constraintException.expect(ConstraintViolationException.class);
		loadStatements("testAskConstraint.ttl");
	}

	@Test
	public void testConstructRule() throws Exception {
		loadStatements("testConstructRule.ttl");
		assertStatements("testConstructRule-expected.ttl");
	}

	private void loadStatements(String ttl) throws RepositoryException, RDFParseException, IOException {
		URL url = getClass().getResource(ttl);
		InputStream in = url.openStream();
		try {
			conn.add(in, url.toString(), RDFFormat.TURTLE);
		}
		finally {
			in.close();
		}
	}

	private void assertStatements(String ttl) throws RDFParseException, RDFHandlerException, IOException, RepositoryException {
		StatementCollector expected = new StatementCollector();
		RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
		parser.setRDFHandler(expected);
		URL url = getClass().getResource(ttl);
		InputStream rdfStream = url.openStream();
		parser.parse(rdfStream, url.toString());
		rdfStream.close();

		for(Statement stmt : expected.getStatements()) {
			assertTrue("Expected statement: "+stmt, conn.hasStatement(stmt, true));
		}
	}
}
