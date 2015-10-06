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
package org.eclipse.rdf4j.sail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.UnknownSailTransactionStateException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple tests to sanity check that Sail correctly supports claimed isolation
 * levels.
 * 
 * @author James Leigh
 */
public abstract class SailIsolationLevelTest {

	private final Logger logger = LoggerFactory.getLogger(SailIsolationLevelTest.class);

	/*-----------*
	 * Variables *
	 *-----------*/

	protected Sail store;

	private ValueFactory vf;

	private String failedMessage;

	private Throwable failed;

	/*---------*
	 * Methods *
	 *---------*/

	@Before
	public void setUp()
		throws Exception
	{
		store = createSail();
		store.initialize();
		vf = store.getValueFactory();
		failed = null;
	}

	@After
	public void tearDown()
		throws Exception
	{
		store.shutDown();
	}

	protected abstract Sail createSail()
		throws SailException;

	protected boolean isSupported(IsolationLevels level)
		throws SailException
	{
		SailConnection con = store.getConnection();
		try {
			con.begin(level);
			return true;
		}
		catch (UnknownSailTransactionStateException e) {
			return false;
		}
		finally {
			con.rollback();
			con.close();
		}
	}

	@Test
	public void testNone()
		throws Exception
	{
		readPending(IsolationLevels.NONE);
	}

	@Test
	public void testReadUncommitted()
		throws Exception
	{
		rollbackTriple(IsolationLevels.READ_UNCOMMITTED);
		readPending(IsolationLevels.READ_UNCOMMITTED);
		readPendingWhileActive(IsolationLevels.READ_UNCOMMITTED);
	}

	@Test
	public void testReadCommitted()
		throws Exception
	{
		readCommitted(IsolationLevels.READ_COMMITTED);
		rollbackTriple(IsolationLevels.READ_COMMITTED);
		readPending(IsolationLevels.READ_COMMITTED);
		readPendingWhileActive(IsolationLevels.READ_COMMITTED);
	}

	@Test
	public void testSnapshotRead()
		throws Exception
	{
		if (isSupported(IsolationLevels.SNAPSHOT_READ)) {
			snapshotRead(IsolationLevels.SNAPSHOT_READ);
			readCommitted(IsolationLevels.SNAPSHOT_READ);
			rollbackTriple(IsolationLevels.SNAPSHOT_READ);
			readPending(IsolationLevels.SNAPSHOT_READ);
			readPendingWhileActive(IsolationLevels.SNAPSHOT_READ);
		}
		else {
			logger.warn("{} does not support {}", store, IsolationLevels.SNAPSHOT_READ);
		}
	}

	@Test
	public void testSnapshot()
		throws Exception
	{
		if (isSupported(IsolationLevels.SNAPSHOT)) {
			snapshot(IsolationLevels.SNAPSHOT);
			snapshotRead(IsolationLevels.SNAPSHOT);
			repeatableRead(IsolationLevels.SNAPSHOT);
			readCommitted(IsolationLevels.SNAPSHOT);
			rollbackTriple(IsolationLevels.SNAPSHOT);
			readPending(IsolationLevels.SNAPSHOT);
			readPendingWhileActive(IsolationLevels.SNAPSHOT);
		}
		else {
			logger.warn("{} does not support {}", store, IsolationLevels.SNAPSHOT);
		}
	}

	@Test
	public void testSerializable()
		throws Exception
	{

		if (isSupported(IsolationLevels.SERIALIZABLE)) {
			serializable(IsolationLevels.SERIALIZABLE);
			snapshot(IsolationLevels.SERIALIZABLE);
			snapshotRead(IsolationLevels.SERIALIZABLE);
			repeatableRead(IsolationLevels.SERIALIZABLE);
			readCommitted(IsolationLevels.SERIALIZABLE);
			rollbackTriple(IsolationLevels.SERIALIZABLE);
			readPending(IsolationLevels.SERIALIZABLE);
			readPendingWhileActive(IsolationLevels.SERIALIZABLE);
		}
		else {
			logger.warn("{} does not support {}", store, IsolationLevels.SERIALIZABLE);
		}
	}

	/**
	 * Every connection must support reading it own changes
	 */
	private void readPending(IsolationLevel level)
		throws SailException
	{
		clear(store);
		SailConnection con = store.getConnection();
		try {
			con.begin(level);
			con.addStatement(RDF.NIL, RDF.TYPE, RDF.LIST);
			Assert.assertEquals(1, count(con, RDF.NIL, RDF.TYPE, RDF.LIST, false));
			con.removeStatements(RDF.NIL, RDF.TYPE, RDF.LIST);
			con.commit();
		}
		finally {
			con.close();
		}
	}

	/**
	 * Every connection must support reading its own changes while another
	 * iteration is active.
	 */
	private void readPendingWhileActive(IsolationLevel level)
		throws SailException
	{
		clear(store);
		SailConnection con = store.getConnection();
		try {
			@SuppressWarnings("unused")
			// open an iteration outside the transaction and leave it open.
			CloseableIteration<? extends Statement, SailException> statements = con.getStatements(null, null,
					null, true);
			con.begin(level);
			con.addStatement(RDF.NIL, RDF.TYPE, RDF.LIST);
			Assert.assertEquals(1, count(con, RDF.NIL, RDF.TYPE, RDF.LIST, false));
			con.removeStatements(RDF.NIL, RDF.TYPE, RDF.LIST);
			con.commit();
		}
		finally {
			con.close();
		}
	}

	/**
	 * Supports rolling back added triples
	 */
	private void rollbackTriple(IsolationLevel level)
		throws SailException
	{
		clear(store);
		SailConnection con = store.getConnection();
		try {
			con.begin(level);
			con.addStatement(RDF.NIL, RDF.TYPE, RDF.LIST);
			con.rollback();
			Assert.assertEquals(0, count(con, RDF.NIL, RDF.TYPE, RDF.LIST, false));
		}
		finally {
			con.close();
		}
	}

	/**
	 * Read operations must not see uncommitted changes
	 */
	private void readCommitted(final IsolationLevel level)
		throws Exception
	{
		clear(store);
		final CountDownLatch start = new CountDownLatch(2);
		final CountDownLatch begin = new CountDownLatch(1);
		final CountDownLatch uncommitted = new CountDownLatch(1);
		Thread writer = new Thread(new Runnable() {

			public void run() {
				try {
					SailConnection write = store.getConnection();
					try {
						start.countDown();
						start.await();
						write.begin(level);
						write.addStatement(RDF.NIL, RDF.TYPE, RDF.LIST);
						begin.countDown();
						uncommitted.await(1, TimeUnit.SECONDS);
						write.rollback();
					}
					finally {
						write.close();
					}
				}
				catch (Throwable e) {
					fail("Writer failed", e);
				}
			}
		});
		Thread reader = new Thread(new Runnable() {

			public void run() {
				try {
					SailConnection read = store.getConnection();
					try {
						start.countDown();
						start.await();
						begin.await();
						read.begin(level);
						// must not read uncommitted changes
						long counted = count(read, RDF.NIL, RDF.TYPE, RDF.LIST, false);
						uncommitted.countDown();
						try {
							read.commit();
						}
						catch (SailException e) {
							// it is okay to abort after a dirty read
							e.printStackTrace();
							return;
						}
						// not read if transaction is consistent
						Assert.assertEquals(0, counted);
					}
					finally {
						read.close();
					}
				}
				catch (Throwable e) {
					fail("Reader failed", e);
				}
			}
		});
		reader.start();
		writer.start();
		reader.join();
		writer.join();
		assertNotFailed();
	}

	/**
	 * Any statement read in a transaction must remain present until the
	 * transaction is over
	 */
	private void repeatableRead(final IsolationLevels level)
		throws Exception
	{
		clear(store);
		final CountDownLatch start = new CountDownLatch(2);
		final CountDownLatch begin = new CountDownLatch(1);
		final CountDownLatch observed = new CountDownLatch(1);
		final CountDownLatch changed = new CountDownLatch(1);
		Thread writer = new Thread(new Runnable() {

			public void run() {
				try {
					SailConnection write = store.getConnection();
					try {
						start.countDown();
						start.await();
						write.begin(level);
						write.addStatement(RDF.NIL, RDF.TYPE, RDF.LIST);
						write.commit();

						begin.countDown();
						observed.await(1, TimeUnit.SECONDS);

						write.begin(level);
						write.removeStatements(RDF.NIL, RDF.TYPE, RDF.LIST);
						write.commit();
						changed.countDown();
					}
					finally {
						write.close();
					}
				}
				catch (Throwable e) {
					fail("Writer failed", e);
				}
			}
		});
		Thread reader = new Thread(new Runnable() {

			public void run() {
				try {
					SailConnection read = store.getConnection();
					try {
						start.countDown();
						start.await();
						begin.await();
						read.begin(level);
						long first = count(read, RDF.NIL, RDF.TYPE, RDF.LIST, false);
						Assert.assertEquals(1, first);
						observed.countDown();
						changed.await(1, TimeUnit.SECONDS);
						// observed statements must continue to exist
						long second = count(read, RDF.NIL, RDF.TYPE, RDF.LIST, false);
						try {
							read.commit();
						}
						catch (SailException e) {
							// it is okay to abort on inconsistency
							e.printStackTrace();
							return;
						}
						// statement must continue to exist if transaction consistent
						Assert.assertEquals(first, second);
					}
					finally {
						read.close();
					}
				}
				catch (Throwable e) {
					fail("Reader failed", e);
				}
			}
		});
		reader.start();
		writer.start();
		reader.join();
		writer.join();
		assertNotFailed();
	}

	/**
	 * Query results must not include statements added after the first result is
	 * read
	 */
	private void snapshotRead(IsolationLevel level)
		throws SailException
	{
		clear(store);
		SailConnection con = store.getConnection();
		try {
			con.begin(level);
			int size = 1000;
			for (int i = 0; i < size; i++) {
				insertTestStatement(con, i);
			}
			int counter = 0;
			CloseableIteration<? extends Statement, SailException> stmts;
			stmts = con.getStatements(null, null, null, false);
			try {
				while (stmts.hasNext()) {
					Statement st = stmts.next();
					counter++;
					if (counter < size) {
						// remove observed statement to force new state
						con.removeStatements(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
						insertTestStatement(con, size + counter);
						insertTestStatement(con, size + size + counter);
					}
				}
			}
			finally {
				stmts.close();
			}
			try {
				con.commit();
			}
			catch (SailException e) {
				// it is okay to abort after a dirty read
				e.printStackTrace();
				return;
			}
			Assert.assertEquals(size, counter);
		}
		finally {
			con.close();
		}
	}

	/**
	 * Reader observes the complete state of the store and ensure that does not
	 * change
	 */
	private void snapshot(final IsolationLevels level)
		throws Exception
	{
		clear(store);
		final CountDownLatch start = new CountDownLatch(2);
		final CountDownLatch begin = new CountDownLatch(1);
		final CountDownLatch observed = new CountDownLatch(1);
		final CountDownLatch changed = new CountDownLatch(1);
		Thread writer = new Thread(new Runnable() {

			public void run() {
				try {
					SailConnection write = store.getConnection();
					try {
						start.countDown();
						start.await();
						write.begin(level);
						insertTestStatement(write, 1);
						write.commit();

						begin.countDown();
						observed.await(1, TimeUnit.SECONDS);

						write.begin(level);
						insertTestStatement(write, 2);
						write.commit();
						changed.countDown();
					}
					finally {
						write.close();
					}
				}
				catch (Throwable e) {
					fail("Writer failed", e);
				}
			}
		});
		Thread reader = new Thread(new Runnable() {

			public void run() {
				try {
					SailConnection read = store.getConnection();
					try {
						start.countDown();
						start.await();
						begin.await();
						read.begin(level);
						long first = count(read, null, null, null, false);
						observed.countDown();
						changed.await(1, TimeUnit.SECONDS);
						// new statements must not be observed
						long second = count(read, null, null, null, false);
						try {
							read.commit();
						}
						catch (SailException e) {
							// it is okay to abort on inconsistency
							e.printStackTrace();
							return;
						}
						// store must not change if transaction consistent
						Assert.assertEquals(first, second);
					}
					finally {
						read.close();
					}
				}
				catch (Throwable e) {
					fail("Reader failed", e);
				}
			}
		});
		reader.start();
		writer.start();
		reader.join();
		writer.join();
		assertNotFailed();
	}

	/**
	 * Two transactions read a value and replace it
	 */
	private void serializable(final IsolationLevels level)
		throws Exception
	{
		clear(store);
		final ValueFactory vf = store.getValueFactory();
		final IRI subj = vf.createIRI("http://test#s");
		final IRI pred = vf.createIRI("http://test#p");
		SailConnection prep = store.getConnection();
		try {
			prep.begin(level);
			prep.addStatement(subj, pred, vf.createLiteral(1));
			prep.commit();
		}
		finally {
			prep.close();
		}
		final CountDownLatch start = new CountDownLatch(2);
		final CountDownLatch observed = new CountDownLatch(2);
		Thread t1 = incrementBy(start, observed, level, vf, subj, pred, 3);
		Thread t2 = incrementBy(start, observed, level, vf, subj, pred, 5);
		t2.start();
		t1.start();
		t2.join();
		t1.join();
		assertNotFailed();
		SailConnection check = store.getConnection();
		try {
			check.begin(level);
			Literal lit = readLiteral(check, subj, pred);
			int val = lit.intValue();
			// val could be 4 or 6 if one transaction was aborted
			if (val != 4 && val != 6) {
				Assert.assertEquals(9, val);
			}
			check.commit();
		}
		finally {
			check.close();
		}
	}

	protected Thread incrementBy(final CountDownLatch start, final CountDownLatch observed,
			final IsolationLevels level, final ValueFactory vf, final IRI subj, final IRI pred, final int by)
	{
		return new Thread(new Runnable() {

			public void run() {
				try {
					SailConnection con = store.getConnection();
					try {
						start.countDown();
						start.await();
						con.begin(level);
						Literal o1 = readLiteral(con, subj, pred);
						observed.countDown();
						observed.await(1, TimeUnit.SECONDS);
						con.removeStatements(subj, pred, o1);
						con.addStatement(subj, pred, vf.createLiteral(o1.intValue() + by));
						try {
							con.commit();
						}
						catch (SailException e) {
							// it is okay to abort on conflict
							e.printStackTrace();
						}
					}
					finally {
						con.close();
					}
				}
				catch (Throwable e) {
					fail("Increment " + by + " failed", e);
				}
			}
		});
	}

	private void clear(Sail store)
		throws SailException
	{
		SailConnection con = store.getConnection();
		try {
			con.begin();
			con.clear();
			con.commit();
		}
		finally {
			con.close();
		}
	}

	protected long count(SailConnection con, Resource subj, IRI pred, Value obj, boolean includeInferred,
			Resource... contexts)
				throws SailException
	{
		CloseableIteration<? extends Statement, SailException> stmts;
		stmts = con.getStatements(subj, pred, obj, includeInferred, contexts);
		try {
			long counter = 0;
			while (stmts.hasNext()) {
				stmts.next();
				counter++;
			}
			return counter;
		}
		finally {
			stmts.close();
		}
	}

	protected Literal readLiteral(SailConnection con, final IRI subj, final IRI pred)
		throws SailException
	{
		CloseableIteration<? extends Statement, SailException> stmts;
		stmts = con.getStatements(subj, pred, null, false);
		try {
			if (!stmts.hasNext())
				return null;
			Value obj = stmts.next().getObject();
			if (stmts.hasNext())
				Assert.fail("multiple literals: " + obj + " and " + stmts.next());
			return (Literal)obj;
		}
		finally {
			stmts.close();
		}
	}

	protected void insertTestStatement(SailConnection connection, int i)
		throws SailException
	{
		Literal lit = vf.createLiteral(Integer.toString(i), XMLSchema.INTEGER);
		connection.addStatement(vf.createIRI("http://test#s" + i), vf.createIRI("http://test#p"), lit,
				vf.createIRI("http://test#context_" + i));
	}

	protected synchronized void fail(String message, Throwable t) {
		failedMessage = message;
		failed = t;
	}

	protected synchronized void assertNotFailed() {
		if (failed != null) {
			throw (AssertionError)new AssertionError(failedMessage).initCause(failed);
		}
	}

}