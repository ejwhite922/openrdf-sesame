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
package org.eclipse.rdf4j.http.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.eclipse.rdf4j.OpenRDFException;
import org.eclipse.rdf4j.common.iteration.LookAheadIteration;
import org.eclipse.rdf4j.query.QueryEvaluationException;

/**
 * Makes working with a queue easier by adding the methods {@link #done()} and
 * {@link #toss(Exception)} and automatically converting the exception into a
 * QueryEvaluationException with an appropriate stack trace.
 * 
 * @author James Leigh
 */
public class QueueCursor<E> extends LookAheadIteration<E, QueryEvaluationException> {

	private volatile boolean done;

	private BlockingQueue<E> queue;

	private E afterLast = createAfterLast();

	private volatile Queue<Throwable> exceptions = new LinkedList<Throwable>();

	/**
	 * Creates an <tt>QueueCursor</tt> with the given (fixed) capacity and
	 * default access policy.
	 * 
	 * @param capacity
	 *        the capacity of this queue
	 */
	public QueueCursor(int capacity) {
		this(capacity, false);
	}

	/**
	 * Creates an <tt>QueueCursor</tt> with the given (fixed) capacity and the
	 * specified access policy.
	 * 
	 * @param capacity
	 *        the capacity of this queue
	 * @param fair
	 *        if <tt>true</tt> then queue accesses for threads blocked on
	 *        insertion or removal, are processed in FIFO order; if
	 *        <tt>false</tt> the access order is unspecified.
	 */
	public QueueCursor(int capacity, boolean fair) {
		this.queue = new ArrayBlockingQueue<E>(capacity, fair);
	}

	/**
	 * The next time {@link #next()} is called this exception will be thrown. If
	 * it is not a QueryEvaluationException or RuntimeException it will be
	 * wrapped in a QueryEvaluationException.
	 */
	public void toss(Exception exception) {
		synchronized (exceptions) {
			exceptions.add(exception);
		}
	}

	/**
	 * Adds another item to the queue, blocking while the queue is full.
	 */
	public void put(E item)
		throws InterruptedException
	{
		if (!done) {
			queue.put(item);
		}
	}

	/**
	 * Indicates the method {@link #put(Object)} will not be called in the queue
	 * anymore.
	 */
	public void done() {
		done = true;
		try {
			queue.add(afterLast);
		}
		catch (IllegalStateException e) {
			// no thread is waiting on this queue anyway
		}
	}

	/**
	 * Returns the next item in the queue or throws an exception.
	 */
	@Override
	public E getNextElement()
		throws QueryEvaluationException
	{
		try {
			checkException();
			E take;
			if (done) {
				take = queue.poll();
			}
			else {
				take = queue.take();
				if (done) {
					done(); // in case the queue was full before
				}
			}
			if (isAfterLast(take)) {
				checkException();
				done(); // put afterLast back for others
				return null;
			}
			return take;
		}
		catch (InterruptedException e) {
			checkException();
			throw new QueryEvaluationException(e);
		}
	}

	@Override
	public void handleClose()
		throws QueryEvaluationException
	{
		done = true;
		do {
			queue.clear(); // ensure extra room is available
		}
		while (!queue.offer(afterLast));
		checkException();
	}

	public void checkException()
		throws QueryEvaluationException
	{
		synchronized (exceptions) {
			if (!exceptions.isEmpty()) {
				try {
					throw exceptions.remove();
				}
				catch (OpenRDFException e) {
					if (e instanceof QueryEvaluationException) {
						List<StackTraceElement> stack = new ArrayList<StackTraceElement>();
						stack.addAll(Arrays.asList(e.getStackTrace()));
						StackTraceElement[] thisStack = new Throwable().getStackTrace();
						stack.addAll(Arrays.asList(thisStack).subList(1, thisStack.length));
						e.setStackTrace(stack.toArray(new StackTraceElement[stack.size()]));
						throw e;
					}
					else {
						throw new QueryEvaluationException(e);
					}
				}
				catch (RuntimeException e) {
					// any RuntimeException that is not an OpenRDFException should be
					// reported as-is
					List<StackTraceElement> stack = new ArrayList<StackTraceElement>();
					stack.addAll(Arrays.asList(e.getStackTrace()));
					StackTraceElement[] thisStack = new Throwable().getStackTrace();
					stack.addAll(Arrays.asList(thisStack));
					e.setStackTrace(stack.toArray(new StackTraceElement[stack.size()]));
					throw e;
				}
				catch (Throwable e) {
					throw new QueryEvaluationException(e);
				}
			}
		}
	}

	private boolean isAfterLast(E take) {
		return take == null || take == afterLast;
	}

	@SuppressWarnings("unchecked")
	private E createAfterLast() {
		return (E)new Object();
	}

}