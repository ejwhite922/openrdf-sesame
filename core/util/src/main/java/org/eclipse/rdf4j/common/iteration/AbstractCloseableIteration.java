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

package org.eclipse.rdf4j.common.iteration;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Base class for {@link CloseableIteration}s offering common functionality.
 * This class keeps track of whether the iteration has been closed and handles
 * multiple calls to {@link #close()} by ignoring all but the first call.
 */
public abstract class AbstractCloseableIteration<E, X extends Exception> implements CloseableIteration<E, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * Flag indicating whether this iteration has been closed.
	 */
	private final AtomicBoolean closed = new AtomicBoolean(false);

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Checks whether this CloseableIteration has been closed.
	 * 
	 * @return <tt>true</tt> if the CloseableIteration has been closed,
	 *         <tt>false</tt> otherwise.
	 */
	public final boolean isClosed() {
		return closed.get();
	}

	/**
	 * Calls {@link #handleClose()} upon first call and makes sure this method
	 * gets called only once.
	 */
	public final void close()
		throws X
	{
		if (closed.compareAndSet(false, true)) {
			handleClose();
		}
	}

	/**
	 * Called by {@link #close} when it is called for the first time. This method
	 * is only called once on each iteration. By default, this method does
	 * nothing.
	 * 
	 * @throws X
	 */
	protected void handleClose()
		throws X
	{
	}
}