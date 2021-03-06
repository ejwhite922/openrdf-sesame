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
package org.openrdf.repository.util;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.AbstractRDFHandler;

/**
 * An RDFHandler that removes RDF data from a repository.
 */
public class RDFRemover extends AbstractRDFHandler {

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * The connection to use for the removal operations.
	 */
	private final RepositoryConnection con;

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * Flag indicating whether the context specified for this RDFRemover should
	 * be enforced upon all reported statements.
	 */
	private boolean enforceContext;

	/**
	 * The context to remove the statements from; <tt>null</tt> to indicate the
	 * null context. This context value is used when enforceContext is set to
	 * true.
	 */
	private Resource context;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new RDFRemover object that removes the data from the default
	 * context.
	 * 
	 * @param con
	 *        The connection to use for the removal operations.
	 */
	public RDFRemover(RepositoryConnection con) {
		this.con = con;
		this.enforceContext = false;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Enforces the supplied context upon all statements that are reported to
	 * this RDFRemover.
	 * 
	 * @param context
	 *        A Resource identifying the context, or <tt>null</tt> for the null
	 *        context.
	 */
	public void enforceContext(Resource context) {
		this.context = context;
		enforceContext = true;
	}

	/**
	 * Checks whether this RDFRemover enforces its context upon all statements
	 * that are reported to it.
	 * 
	 * @return <tt>true</tt> if it enforces its context, <tt>false</tt>
	 *         otherwise.
	 */
	public boolean enforcesContext() {
		return enforceContext;
	}

	/**
	 * Gets the context identifier that this RDFRemover enforces upon all
	 * statements that are reported to it (in case <tt>enforcesContext()</tt>
	 * returns <tt>true</tt>).
	 * 
	 * @return A Resource identifying the context, or <tt>null</tt> if the null
	 *         context is enforced.
	 */
	public Resource getContext() {
		return context;
	}

	@Override
	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		try {
			if (enforceContext) {
				// Override supplied context info
				con.remove(st.getSubject(), st.getPredicate(), st.getObject(), context);
			}
			else {
				con.remove(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
			}
		}
		catch (RepositoryException e) {
			throw new RDFHandlerException(e);
		}
	}
}
