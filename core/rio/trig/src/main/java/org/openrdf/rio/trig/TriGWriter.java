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
package org.openrdf.rio.trig;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.turtle.TurtleWriter;

/**
 * An extension of {@link TurtleWriter} that writes RDF documents in <a
 * href="http://www.wiwiss.fu-berlin.de/suhl/bizer/TriG/Spec/">TriG</a> format
 * by adding graph scopes to the Turtle document.
 * 
 * @author Arjohn Kampman
 */
public class TriGWriter extends TurtleWriter {

	/*-----------*
	 * Variables *
	 *-----------*/

	private boolean inActiveContext;

	private Resource currentContext;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new TriGWriter that will write to the supplied OutputStream.
	 * using the default base URI.
	 * <p>
	 * The current base URI at any point in the document can be changed using
	 * calls to {@link #handleBaseURI(String)}.
	 * 
	 * @param out
	 *        The OutputStream to write the TriG document to.
	 */
	public TriGWriter(OutputStream out) {
		super(out);
	}

	/**
	 * Creates a new TriGWriter that will write to the supplied OutputStream.
	 * using the specified default base URI.
	 * <p>
	 * The current base URI at any point in the document can be changed using
	 * calls to {@link #handleBaseURI(String)}.
	 * 
	 * @param out
	 *        The OutputStream to write the TriG document to.
	 * @param defaultBaseURI
	 *        The default base URI.
	 * @since 2.8.0
	 */
	public TriGWriter(OutputStream out, URI defaultBaseURI) {
		super(out, defaultBaseURI);
	}

	/**
	 * Creates a new TriGWriter that will write to the supplied Writer. using the
	 * default base URI.
	 * <p>
	 * The current base URI at any point in the document can be changed using
	 * calls to {@link #handleBaseURI(String)}.
	 * 
	 * @param writer
	 *        The Writer to write the TriG document to.
	 * @param defaultBaseURI
	 *        The default base URI.
	 */
	public TriGWriter(Writer writer) {
		super(writer);
	}

	/**
	 * Creates a new TriGWriter that will write to the supplied Writer. using the
	 * specified default base URI.
	 * <p>
	 * The current base URI at any point in the document can be changed using
	 * calls to {@link #handleBaseURI(String)}.
	 * 
	 * @param writer
	 *        The Writer to write the TriG document to.
	 * @param defaultBaseURI
	 *        The default base URI.
	 * @since 2.8.0
	 */
	public TriGWriter(Writer writer, URI defaultBaseURI) {
		super(writer, defaultBaseURI);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public RDFFormat getRDFFormat() {
		return RDFFormat.TRIG;
	}

	@Override
	public void startRDF()
		throws RDFHandlerException
	{
		super.startRDF();

		inActiveContext = false;
		currentContext = null;
	}

	@Override
	public void endRDF()
		throws RDFHandlerException
	{
		super.endRDF();

		try {
			closeActiveContext();
			writer.flush();
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	@Override
	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		if (!writingStarted) {
			throw new RuntimeException("Document writing has not yet been started");
		}

		try {
			Resource context = st.getContext();

			if (inActiveContext && !contextsEquals(context, currentContext)) {
				closePreviousStatement();
				closeActiveContext();
			}

			if (!inActiveContext) {
				writer.writeEOL();

				if (context != null) {
					writeResource(context);
					writer.write(" ");
				}

				writer.write("{");
				writer.increaseIndentation();

				currentContext = context;
				inActiveContext = true;
			}
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}

		super.handleStatement(st);
	}

	@Override
	protected void writeCommentLine(String line)
		throws IOException
	{
		closeActiveContext();
		super.writeCommentLine(line);
	}

	@Override
	protected void writeNamespace(String prefix, String name)
		throws IOException
	{
		closeActiveContext();
		super.writeNamespace(prefix, name);
	}

	protected void closeActiveContext()
		throws IOException
	{
		if (inActiveContext) {
			writer.decreaseIndentation();
			writer.write("}");
			writer.writeEOL();

			inActiveContext = false;
			currentContext = null;
		}
	}

	private static final boolean contextsEquals(Resource context1, Resource context2) {
		if (context1 == null) {
			return context2 == null;
		}
		else {
			return context1.equals(context2);
		}
	}
}
