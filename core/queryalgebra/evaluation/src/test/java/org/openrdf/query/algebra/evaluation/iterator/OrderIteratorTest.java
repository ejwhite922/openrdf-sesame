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
package org.openrdf.query.algebra.evaluation.iterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.openrdf.query.BindingSet;

/**
 * @author james
 */
public class OrderIteratorTest extends TestCase {

	private IterationStub iteration;

	private OrderIterator order;

	private List<BindingSet> list;

	private BindingSet b1 = new BindingSetSize(1);

	private BindingSet b2 = new BindingSetSize(2);

	private BindingSet b3 = new BindingSetSize(3);

	private BindingSet b4 = new BindingSetSize(4);

	private BindingSet b5 = new BindingSetSize(5);

	private SizeComparator cmp;

	public void testFirstHasNext()
		throws Exception
	{
		order.hasNext();
		assertEquals(list.size() + 1, iteration.hasNextCount);
		assertEquals(list.size(), iteration.nextCount);
		assertEquals(0, iteration.removeCount);
	}

	public void testHasNext()
		throws Exception
	{
		order.hasNext();
		order.next();
		order.hasNext();
		assertEquals(list.size() + 1, iteration.hasNextCount);
		assertEquals(list.size(), iteration.nextCount);
		assertEquals(0, iteration.removeCount);
	}

	public void testFirstNext()
		throws Exception
	{
		order.next();
		assertEquals(list.size() + 1, iteration.hasNextCount);
		assertEquals(list.size(), iteration.nextCount);
		assertEquals(0, iteration.removeCount);
	}

	public void testNext()
		throws Exception
	{
		order.next();
		order.next();
		assertEquals(list.size() + 1, iteration.hasNextCount);
		assertEquals(list.size(), iteration.nextCount);
		assertEquals(0, iteration.removeCount);
	}

	public void testRemove()
		throws Exception
	{
		try {
			order.remove();
			fail();
		}
		catch (UnsupportedOperationException e) {
		}

	}

	public void testSorting()
		throws Exception
	{
		List<BindingSet> sorted = new ArrayList<BindingSet>(list);
		Collections.sort(sorted, cmp);
		for (BindingSet b : sorted) {
			assertEquals(b, order.next());
		}
		assertFalse(order.hasNext());
	}

	@Override
	protected void setUp()
		throws Exception
	{
		list = Arrays.asList(b3, b5, b2, b1, b4, b2);
		cmp = new SizeComparator();
		iteration = new IterationStub();
		iteration.setIterator(list.iterator());
		order = new OrderIterator(iteration, cmp);
	}

}
