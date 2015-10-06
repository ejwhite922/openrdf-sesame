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
package org.eclipse.rdf4j.rio.trix;

import static org.eclipse.rdf4j.rio.trix.TriXConstants.BNODE_TAG;
import static org.eclipse.rdf4j.rio.trix.TriXConstants.CONTEXT_TAG;
import static org.eclipse.rdf4j.rio.trix.TriXConstants.DATATYPE_ATT;
import static org.eclipse.rdf4j.rio.trix.TriXConstants.LANGUAGE_ATT;
import static org.eclipse.rdf4j.rio.trix.TriXConstants.NAMESPACE;
import static org.eclipse.rdf4j.rio.trix.TriXConstants.PLAIN_LITERAL_TAG;
import static org.eclipse.rdf4j.rio.trix.TriXConstants.ROOT_TAG;
import static org.eclipse.rdf4j.rio.trix.TriXConstants.TRIPLE_TAG;
import static org.eclipse.rdf4j.rio.trix.TriXConstants.TYPED_LITERAL_TAG;
import static org.eclipse.rdf4j.rio.trix.TriXConstants.URI_TAG;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import org.eclipse.rdf4j.common.xml.XMLWriter;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Literals;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFWriter;
import org.eclipse.rdf4j.rio.helpers.XMLWriterSettings;

/**
 * An implementation of the RDFWriter interface that writes RDF documents in <a
 * href="http://www.w3.org/2004/03/trix/">TriX format</a>.
 * 
 * @author Arjohn Kampman
 */
public class TriXWriter extends AbstractRDFWriter implements RDFWriter {

	/*-----------*
	 * Variables *
	 *-----------*/

	private XMLWriter xmlWriter;

	private boolean writingStarted;

	private boolean inActiveContext;

	private Resource currentContext;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new TriXWriter that will write to the supplied OutputStream.
	 * 
	 * @param out
	 *        The OutputStream to write the RDF/XML document to.
	 */
	public TriXWriter(OutputStream out) {
		this(new XMLWriter(out));
	}

	/**
	 * Creates a new TriXWriter that will write to the supplied Writer.
	 * 
	 * @param writer
	 *        The Writer to write the RDF/XML document to.
	 */
	public TriXWriter(Writer writer) {
		this(new XMLWriter(writer));
	}

	protected TriXWriter(XMLWriter xmlWriter) {
		this.xmlWriter = xmlWriter;
		this.xmlWriter.setPrettyPrint(true);

		writingStarted = false;
		inActiveContext = false;
		currentContext = null;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public RDFFormat getRDFFormat() {
		return RDFFormat.TRIX;
	}

	public void startRDF()
		throws RDFHandlerException
	{
		if (writingStarted) {
			throw new RDFHandlerException("Document writing has already started");
		}

		try {

			if (getWriterConfig().get(XMLWriterSettings.INCLUDE_XML_PI)) {
				xmlWriter.startDocument();
			}
			
			xmlWriter.setAttribute("xmlns", NAMESPACE);
			xmlWriter.startTag(ROOT_TAG);
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
		finally {
			writingStarted = true;
		}
	}

	public void endRDF()
		throws RDFHandlerException
	{
		if (!writingStarted) {
			throw new RDFHandlerException("Document writing has not yet started");
		}

		try {
			if (inActiveContext) {
				xmlWriter.endTag(CONTEXT_TAG);
				inActiveContext = false;
				currentContext = null;
			}
			xmlWriter.endTag(ROOT_TAG);
			xmlWriter.endDocument();
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
		finally {
			writingStarted = false;
		}
	}

	public void handleNamespace(String prefix, String name) {
		// ignore
	}

	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		if (!writingStarted) {
			throw new RDFHandlerException("Document writing has not yet been started");
		}

		try {
			Resource context = st.getContext();

			if (inActiveContext && !contextsEquals(context, currentContext)) {
				// Close currently active context
				xmlWriter.endTag(CONTEXT_TAG);
				inActiveContext = false;
			}

			if (!inActiveContext) {
				// Open new context
				xmlWriter.startTag(CONTEXT_TAG);

				if (context != null) {
					writeValue(context);
				}

				currentContext = context;
				inActiveContext = true;
			}

			xmlWriter.startTag(TRIPLE_TAG);

			writeValue(st.getSubject());
			writeValue(st.getPredicate());
			writeValue(st.getObject());

			xmlWriter.endTag(TRIPLE_TAG);
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	public void handleComment(String comment)
		throws RDFHandlerException
	{
		try {
			xmlWriter.comment(comment);
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	/**
	 * Writes out the XML-representation for the supplied value.
	 */
	private void writeValue(Value value)
		throws IOException, RDFHandlerException
	{
		if (value instanceof IRI) {
			IRI uri = (IRI)value;
			xmlWriter.textElement(URI_TAG, uri.toString());
		}
		else if (value instanceof BNode) {
			BNode bNode = (BNode)value;
			xmlWriter.textElement(BNODE_TAG, bNode.getID());
		}
		else if (value instanceof Literal) {
			Literal literal = (Literal)value;
			IRI datatype = literal.getDatatype();

			if (Literals.isLanguageLiteral(literal)) {
				xmlWriter.setAttribute(LANGUAGE_ATT, literal.getLanguage().get());
				xmlWriter.textElement(PLAIN_LITERAL_TAG, literal.getLabel());
			}
			else {
				xmlWriter.setAttribute(DATATYPE_ATT, datatype.toString());
				xmlWriter.textElement(TYPED_LITERAL_TAG, literal.getLabel());
			}
		}
		else {
			throw new RDFHandlerException("Unknown value type: " + value.getClass());
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