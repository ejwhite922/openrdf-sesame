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
package org.eclipse.rdf4j.query.resultio;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.rdf4j.rio.RioSetting;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;

/**
 * Base class for {@link QueryResultWriter}s offering common functionality for
 * query result writers.
 * 
 * @author Peter Ansell
 * @since 2.7.0
 */
public abstract class AbstractQueryResultWriter implements QueryResultWriter {

	private WriterConfig writerConfig = new WriterConfig();

	/**
	 * Default constructor.
	 */
	public AbstractQueryResultWriter() {
	}

	@Override
	public void setWriterConfig(WriterConfig config) {
		this.writerConfig = config;
	}

	@Override
	public WriterConfig getWriterConfig() {
		return this.writerConfig;
	}

	@Override
	public Collection<RioSetting<?>> getSupportedSettings() {
		return Collections.emptyList();
	}

	protected boolean xsdStringToPlainLiteral() {
		return getWriterConfig().get(BasicWriterSettings.XSD_STRING_TO_PLAIN_LITERAL);
	}
}