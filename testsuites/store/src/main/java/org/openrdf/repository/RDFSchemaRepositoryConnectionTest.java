/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.result.ModelResult;

/**
 * @author jeen
 * @author Arjohn Kampman
 */
public abstract class RDFSchemaRepositoryConnectionTest extends RepositoryConnectionTest {

	private URI person;

	private URI woman;

	private URI man;

	public RDFSchemaRepositoryConnectionTest(String name) {
		super(name);
	}

	@Override
	public void setUp()
		throws Exception
	{
		super.setUp();

		person = vf.createURI(FOAF_NS + "Person");
		woman = vf.createURI("http://example.org/Woman");
		man = vf.createURI("http://example.org/Man");
	}

	public void testDomainInference()
		throws Exception
	{
		testCon.add(name, RDFS.DOMAIN, person);
		testCon.add(bob, name, nameBob);

		assertTrue(testCon.hasMatch(bob, RDF.TYPE, person, true));
	}

	public void testSubClassInference()
		throws Exception
	{
		testCon.begin();
		testCon.add(woman, RDFS.SUBCLASSOF, person);
		testCon.add(man, RDFS.SUBCLASSOF, person);
		testCon.add(alice, RDF.TYPE, woman);
		testCon.commit();

		assertTrue(testCon.hasMatch(alice, RDF.TYPE, person, true));
	}

	public void testMakeExplicit()
		throws Exception
	{
		testCon.begin();
		testCon.add(woman, RDFS.SUBCLASSOF, person);
		testCon.add(alice, RDF.TYPE, woman);
		testCon.commit();

		assertTrue(testCon.hasMatch(alice, RDF.TYPE, person, true));

		testCon.add(alice, RDF.TYPE, person);

		assertTrue(testCon.hasMatch(alice, RDF.TYPE, person, true));
	}

	public void testExplicitFlag()
		throws Exception
	{
		ModelResult result = testCon.match(RDF.TYPE, RDF.TYPE, null, true);
		try {
			assertTrue("result should not be empty", result.hasNext());
		}
		finally {
			result.close();
		}

		result = testCon.match(RDF.TYPE, RDF.TYPE, null, false);
		try {
			assertFalse("result should be empty", result.hasNext());
		}
		finally {
			result.close();
		}
	}

	public void testInferencerUpdates()
		throws Exception
	{
		testCon.begin();
		testCon.add(bob, name, nameBob);
		testCon.removeMatch(bob, name, nameBob);
		testCon.commit();

		assertFalse(testCon.hasMatch(bob, RDF.TYPE, RDFS.RESOURCE, true));
	}

	public void testInferencerQueryDuringTransaction()
		throws Exception
	{
		testCon.begin();
		testCon.add(bob, name, nameBob);
		assertTrue(testCon.hasMatch(bob, RDF.TYPE, RDFS.RESOURCE, true));
		testCon.commit();
	}

	public void testInferencerTransactionIsolation()
		throws Exception
	{
		testCon.begin();
		testCon.add(woman, RDFS.SUBCLASSOF, person);

		assertTrue(testCon.hasMatch(woman, RDF.TYPE, RDFS.CLASS, true));
		assertFalse(testCon2.hasMatch(woman, RDF.TYPE, RDFS.CLASS, true));

		testCon.commit();

		assertTrue(testCon.hasMatch(woman, RDF.TYPE, RDFS.CLASS, true));
		assertTrue(testCon2.hasMatch(woman, RDF.TYPE, RDFS.CLASS, true));
	}
}
