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
package org.eclipse.rdf4j.query.algebra.evaluation.util;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.algebra.evaluation.util.ValueComparator;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author james
 *
 */
public class LiteralComparatorTest {

	private ValueFactory vf = SimpleValueFactory.getInstance();

	private Literal one = vf.createLiteral(1);

	private Literal ten = vf.createLiteral(10);

	private Literal a = vf.createLiteral("a");

	private Literal b = vf.createLiteral("b");

	private Literal la = vf.createLiteral("a", "en");

	private Literal lb = vf.createLiteral("b", "en");

	private Literal lf = vf.createLiteral("a", "fr");

	private Literal f = vf.createLiteral(false);

	private Literal t = vf.createLiteral(true);

	private Literal date1;

	private Literal date2;

	private Literal simple1 = vf.createLiteral("http://script.example/Latin");

	private Literal simple2 = vf.createLiteral("http://script.example/Кириллица");

	private Literal typed1 = vf.createLiteral("http://script.example/Latin", XMLSchema.STRING);

	private ValueComparator cmp = new ValueComparator();

	@Test
	public void testNumeric()
		throws Exception
	{
		assertTrue(cmp.compare(one, one) == 0);
		assertTrue(cmp.compare(one, ten) < 0);
		assertTrue(cmp.compare(ten, one) > 0);
		assertTrue(cmp.compare(ten, ten) == 0);
	}

	@Test
	public void testString()
		throws Exception
	{
		assertTrue(cmp.compare(a, a) == 0);
		assertTrue(cmp.compare(a, b) < 0);
		assertTrue(cmp.compare(b, a) > 0);
		assertTrue(cmp.compare(b, b) == 0);
	}

	@Test
	public void testSameLanguage()
		throws Exception
	{
		assertTrue(cmp.compare(la, la) == 0);
		assertTrue(cmp.compare(la, lb) < 0);
		assertTrue(cmp.compare(lb, la) > 0);
		assertTrue(cmp.compare(lb, lb) == 0);
	}

	@Test
	public void testDifferentLanguage()
		throws Exception
	{
		cmp.compare(la, lf);
	}

	@Test
	public void testBoolean()
		throws Exception
	{
		assertTrue(cmp.compare(f, f) == 0);
		assertTrue(cmp.compare(f, t) < 0);
		assertTrue(cmp.compare(t, f) > 0);
		assertTrue(cmp.compare(t, t) == 0);
	}

	@Test
	public void testDateTime()
		throws Exception
	{
		assertTrue(cmp.compare(date1, date1) == 0);
		assertTrue(cmp.compare(date1, date2) < 0);
		assertTrue(cmp.compare(date2, date1) > 0);
		assertTrue(cmp.compare(date2, date2) == 0);
	}

	@Test
	public void testBothSimple()
		throws Exception
	{
		assertTrue(cmp.compare(simple1, simple1) == 0);
		assertTrue(cmp.compare(simple1, simple2) < 0);
		assertTrue(cmp.compare(simple2, simple1) > 0);
		assertTrue(cmp.compare(simple2, simple2) == 0);
	}

	@Test
	public void testLeftSimple()
		throws Exception
	{
		assertTrue(cmp.compare(simple1, typed1) == 0);
	}

	@Test
	public void testRightSimple()
		throws Exception
	{
		assertTrue(cmp.compare(typed1, simple1) == 0);
	}

	@Test
	public void testOrder()
		throws Exception
	{
		Literal en4 = vf.createLiteral("4", "en");
		Literal nine = vf.createLiteral(9);
		List<Literal> list = new ArrayList<Literal>();
		list.add(ten);
		list.add(en4);
		list.add(nine);
		Collections.sort(list, cmp);
		assertTrue(list.indexOf(nine) < list.indexOf(ten));
	}

	@Before
	public void setUp()
		throws Exception
	{
		DatatypeFactory factory = DatatypeFactory.newInstance();
		XMLGregorianCalendar mar = factory.newXMLGregorianCalendar("2000-03-04T20:00:00Z");
		XMLGregorianCalendar oct = factory.newXMLGregorianCalendar("2002-10-10T12:00:00-05:00");
		date1 = vf.createLiteral(mar);
		date2 = vf.createLiteral(oct);
	}
}