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
package org.openrdf.query.impl;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.TimeLimitIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryInterruptedException;
import org.openrdf.query.impl.AbstractQuery;
import org.openrdf.query.impl.FallbackDataset;
import org.openrdf.query.parser.ParsedQuery;

/**
 * @author Arjohn Kampman
 */
public abstract class AbstractParserQuery extends AbstractQuery {

	private final ParsedQuery parsedQuery;

	protected AbstractParserQuery(ParsedQuery parsedQuery) {
		this.parsedQuery = parsedQuery;
	}

	public ParsedQuery getParsedQuery() {
		return parsedQuery;
	}

	protected CloseableIteration<? extends BindingSet, QueryEvaluationException> enforceMaxQueryTime(
			CloseableIteration<? extends BindingSet, QueryEvaluationException> bindingsIter)
	{
		if (getMaxExecutionTime() > 0) {
			bindingsIter = new QueryInterruptIteration(bindingsIter, 1000L * getMaxExecutionTime());
		}

		return bindingsIter;
	}

	/**
	 * Gets the "active" dataset for this query. The active dataset is either the
	 * dataset that has been specified using {@link #setDataset(Dataset)} or the
	 * dataset that has been specified in the query, where the former takes
	 * precedence over the latter.
	 * 
	 * @return The active dataset, or <tt>null</tt> if there is no dataset.
	 */
	public Dataset getActiveDataset() {
		if (dataset != null) {
			return FallbackDataset.fallback(dataset, parsedQuery.getDataset());
		}

		// No external dataset specified, use query's own dataset (if any)
		return parsedQuery.getDataset();
	}

	@Override
	public String toString() {
		return parsedQuery.toString();
	}

	protected class QueryInterruptIteration extends TimeLimitIteration<BindingSet, QueryEvaluationException> {

		public QueryInterruptIteration(
				Iteration<? extends BindingSet, ? extends QueryEvaluationException> iter, long timeLimit)
		{
			super(iter, timeLimit);
		}

		@Override
		protected void throwInterruptedException()
			throws QueryEvaluationException
		{
			throw new QueryInterruptedException("Query evaluation took too long");
		}
	}
}
