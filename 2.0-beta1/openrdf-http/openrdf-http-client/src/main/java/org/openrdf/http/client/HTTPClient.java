/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.iteration.CloseableIteratorIteration;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.transaction.TransactionWriter;
import org.openrdf.http.protocol.transaction.operations.TransactionOperation;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.GraphQueryResultImpl;
import org.openrdf.query.impl.TupleQueryResultBuilder;
import org.openrdf.query.resultio.QueryResultUtil;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultParseException;
import org.openrdf.query.resultio.TupleQueryResultParser;
import org.openrdf.query.resultio.UnsupportedQueryResultFormatException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.ntriples.NTriplesUtil;

/**
 * 
 */
public class HTTPClient {

	/*-----------*
	 * Constants *
	 *-----------*/

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	/*-----------*
	 * Variables *
	 *-----------*/

	private ValueFactory _valueFactory;

	private String _serverURL;

	private String _repositoryID;

	private HttpClient _httpClient;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public HTTPClient() {
		_valueFactory = new ValueFactoryImpl();

		// TODO: check configuration options for HttpClient
		_httpClient = new HttpClient();
	}

	/*-----------------*
	 * Get/set methods *
	 *-----------------*/

	public void setValueFactory(ValueFactory valueFactory) {
		_valueFactory = valueFactory;
	}

	public ValueFactory getValueFactory() {
		return _valueFactory;
	}

	public void setServerURL(String serverURL) {
		_serverURL = serverURL;
	}

	public String getServerURL() {
		return _serverURL;
	}

	protected void _checkServerURL() {
		if (_serverURL == null) {
			throw new IllegalStateException("Server URL has not been set");
		}
	}

	public void setRepositoryID(String repositoryID) {
		_repositoryID = repositoryID;
	}

	public String getRepositoryID() {
		return _repositoryID;
	}

	protected void _checkRepositoryID() {
		if (_repositoryID == null) {
			throw new IllegalStateException("Repository ID has not been set");
		}
	}

	public String getRepositoryURL() {
		_checkServerURL();
		_checkRepositoryID();
		return Protocol.getRepositoryLocation(_serverURL, _repositoryID);
	}

	/*------------------*
	 * Protocol version *
	 *------------------*/

	public String getServerProtocol()
		throws IOException
	{
		_checkServerURL();

		GetMethod getMethod = new GetMethod(Protocol.getProtocolLocation(_serverURL));

		int httpCode = _httpClient.executeMethod(getMethod);

		try {
			if (httpCode == HttpURLConnection.HTTP_OK) {
				return getMethod.getResponseBodyAsString();
			}
			else {
				throw new IOException("Failed to get server protocol: " + getMethod.getStatusText());
			}
		}
		finally {
			getMethod.releaseConnection();
		}
	}

	/*-----------------*
	 * Repository list *
	 *-----------------*/

	public TupleQueryResult getRepositoryList()
		throws IOException
	{
		try {
			TupleQueryResultBuilder builder = new TupleQueryResultBuilder();
			getRepositoryList(builder);
			return builder.getQueryResult();
		}
		catch (TupleQueryResultHandlerException e) {
			// Found a bug in TupleQueryResultBuilder?
			throw new RuntimeException(e);
		}
	}

	public void getRepositoryList(TupleQueryResultHandler handler)
		throws IOException, TupleQueryResultHandlerException
	{
		_checkServerURL();

		GetMethod method = new GetMethod(Protocol.getRepositoriesLocation(_serverURL));
		try {
			_getTupleQueryResult(method, handler);
		}
		finally {
			_releaseConnection(method);
		}
	}

	/*------------------*
	 * Query evaluation *
	 *------------------*/

	public TupleQueryResult sendTupleQuery(QueryLanguage ql, String query, boolean includeInferred)
		throws IOException
	{
		try {
			TupleQueryResultBuilder builder = new TupleQueryResultBuilder();
			sendTupleQuery(ql, query, includeInferred, builder);
			return builder.getQueryResult();
		}
		catch (TupleQueryResultHandlerException e) {
			// Found a bug in TupleQueryResultBuilder?
			throw new RuntimeException(e);
		}
	}

	public void sendTupleQuery(QueryLanguage ql, String query, boolean includeInferred,
			TupleQueryResultHandler handler)
		throws IOException, TupleQueryResultHandlerException
	{
		GetMethod method = new GetMethod(getRepositoryURL());

		method.setQueryString(new NameValuePair[] {
				new NameValuePair(Protocol.QUERY_LANGUAGE_PARAM_NAME, ql.toString()),
				new NameValuePair(Protocol.QUERY_PARAM_NAME, query),
				new NameValuePair(Protocol.INCLUDE_INFERRED_PARAM_NAME, Boolean.toString(includeInferred)) });

		try {
			_getTupleQueryResult(method, handler);
		}
		finally {
			_releaseConnection(method);
		}
	}

	public GraphQueryResult sendGraphQuery(QueryLanguage ql, String query, boolean includeInferred)
		throws IOException
	{
		try {
			StatementCollector collector = new StatementCollector();
			sendGraphQuery(ql, query, includeInferred, collector);
			return new GraphQueryResultImpl(collector.getNamespaces(), new CloseableIteratorIteration<Statement, QueryEvaluationException>(collector.getStatements().iterator()));
		}
		catch (RDFHandlerException e) {
			// Found a bug in TupleQueryResultBuilder?
			throw new RuntimeException(e);
		}
	}

	public void sendGraphQuery(QueryLanguage ql, String query, boolean includeInferred, RDFHandler handler)
		throws IOException, RDFHandlerException
	{
		GetMethod method = new GetMethod(getRepositoryURL());

		method.setQueryString(new NameValuePair[] {
				new NameValuePair(Protocol.QUERY_LANGUAGE_PARAM_NAME, ql.toString()),
				new NameValuePair(Protocol.QUERY_PARAM_NAME, query),
				new NameValuePair(Protocol.INCLUDE_INFERRED_PARAM_NAME, Boolean.toString(includeInferred)) });

		try {
			_getRDF(method, handler);
		}
		finally {
			_releaseConnection(method);
		}
	}

	/*---------------------------*
	 * Get/add/remove statements *
	 *---------------------------*/

	public void getStatements(Resource subj, URI pred, Value obj, boolean includeInferred, RDFHandler handler,
			Resource... contexts)
		throws IOException, RDFHandlerException
	{
		_getStatements(subj, pred, obj, includeInferred, handler, contexts);
	}

	public void _getStatements(Resource subj, URI pred, Value obj, boolean includeInferred,
			RDFHandler handler, Resource... contexts)
		throws IOException, RDFHandlerException
	{
		_checkServerURL();
		_checkRepositoryID();

		GetMethod method = new GetMethod(Protocol.getStatementsLocation(_serverURL, _repositoryID));

		List<NameValuePair> params = new ArrayList<NameValuePair>(5);
		if (subj != null) {
			params.add(new NameValuePair(Protocol.SUBJECT_PARAM_NAME, NTriplesUtil.toNTriplesString(subj)));
		}
		if (pred != null) {
			params.add(new NameValuePair(Protocol.PREDICATE_PARAM_NAME, NTriplesUtil.toNTriplesString(pred)));
		}
		if (obj != null) {
			params.add(new NameValuePair(Protocol.OBJECT_PARAM_NAME, NTriplesUtil.toNTriplesString(obj)));
		}
		if (contexts != null && contexts.length > 0) {
			for (Resource context : contexts) {
				if (context == null) {
					params.add(new NameValuePair(Protocol.CONTEXT_PARAM_NAME, Protocol.NULL_PARAM_VALUE));
				}
				else {
					params.add(new NameValuePair(Protocol.CONTEXT_PARAM_NAME,
							NTriplesUtil.toNTriplesString(context)));
				}
			}
		}
		if (includeInferred) {
			params.add(new NameValuePair(Protocol.INCLUDE_INFERRED_PARAM_NAME, Boolean.toString(true)));
		}

		method.setQueryString(params.toArray(new NameValuePair[params.size()]));

		try {
			_getRDF(method, handler);
		}
		finally {
			_releaseConnection(method);
		}
	}

	public void sendTransaction(final Iterable<? extends TransactionOperation> txn)
		throws IOException
	{
		_checkServerURL();
		_checkRepositoryID();

		PostMethod method = new PostMethod(Protocol.getStatementsLocation(_serverURL, _repositoryID));

		// Create a RequestEntity for the transaction data
		method.setRequestEntity(new RequestEntity() {

			public long getContentLength() {
				return -1; // don't know
			}

			public String getContentType() {
				return Protocol.TXN_MIME_TYPE;
			}

			public boolean isRepeatable() {
				return true;
			}

			public void writeRequest(OutputStream out)
				throws IOException
			{
				TransactionWriter taWriter = new TransactionWriter();
				taWriter.serialize(txn, out);
			}
		});

		try {
			int httpCode = _httpClient.executeMethod(method);

			if (httpCode < 200 || httpCode >= 300) {
				throw new IOException("Transaction failed: " + method.getStatusText() + " (" + httpCode + ")");
			}
		}
		finally {
			_releaseConnection(method);
		}
	}

	public void upload(Reader contents, String baseURI, RDFFormat dataFormat, boolean overwrite,
			Resource... contexts)
		throws IOException
	{
		RequestEntity entity = new ReaderRequestEntity(contents, dataFormat.getMIMEType());
		_upload(entity, baseURI, overwrite, contexts);
	}

	public void upload(InputStream contents, String baseURI, RDFFormat dataFormat, boolean overwrite,
			Resource... contexts)
		throws IOException
	{
		RequestEntity entity = new InputStreamRequestEntity(contents, dataFormat.getMIMEType());
		_upload(entity, baseURI, overwrite, contexts);
	}

	private void _upload(RequestEntity contents, String baseURI, boolean overwrite, Resource... contexts)
		throws IOException
	{
		_checkServerURL();
		_checkRepositoryID();

		String uploadURL = Protocol.getStatementsLocation(_serverURL, _repositoryID);

		// Select appropriate HTTP method
		EntityEnclosingMethod method;
		if (overwrite) {
			method = new PutMethod(uploadURL);
		}
		else {
			method = new PostMethod(uploadURL);
		}

		// Set relevant query parameters
		List<NameValuePair> params = new ArrayList<NameValuePair>(5);
		for (Resource context : contexts) {
			String encodedContext = Protocol.encodeContext(context);
			params.add(new NameValuePair(Protocol.CONTEXT_PARAM_NAME, encodedContext));
		}
		if (baseURI != null && baseURI.trim().length() != 0) {
			params.add(new NameValuePair(Protocol.BASEURI_PARAM_NAME, baseURI));
		}
		method.setQueryString(params.toArray(new NameValuePair[params.size()]));

		// Set payload
		method.setRequestEntity(contents);

		// Send request
		try {
			int httpCode = _httpClient.executeMethod(method);

			if (httpCode < 200 || httpCode >= 300) {
				throw new IOException("Failed to upload: " + method.getStatusText() + " (" + httpCode + ")");
			}
		}
		finally {
			_releaseConnection(method);
		}
	}

	/*-------------*
	 * Context IDs *
	 *-------------*/

	public TupleQueryResult getContextIDs()
		throws IOException
	{
		try {
			TupleQueryResultBuilder builder = new TupleQueryResultBuilder();
			getContextIDs(builder);
			return builder.getQueryResult();
		}
		catch (TupleQueryResultHandlerException e) {
			// Found a bug in TupleQueryResultBuilder?
			throw new RuntimeException(e);
		}
	}

	public void getContextIDs(TupleQueryResultHandler handler)
		throws IOException, TupleQueryResultHandlerException
	{
		_checkServerURL();
		_checkRepositoryID();

		GetMethod method = new GetMethod(Protocol.getContextsLocation(_serverURL, _repositoryID));
		try {
			_getTupleQueryResult(method, handler);
		}
		finally {
			_releaseConnection(method);
		}
	}

	/*---------------------------*
	 * Get/add/remove namespaces *
	 *---------------------------*/

	public TupleQueryResult getNamespaces()
		throws IOException
	{
		try {
			TupleQueryResultBuilder builder = new TupleQueryResultBuilder();
			getNamespaces(builder);
			return builder.getQueryResult();
		}
		catch (TupleQueryResultHandlerException e) {
			// Found a bug in TupleQueryResultBuilder?
			throw new RuntimeException(e);
		}
	}

	public void getNamespaces(TupleQueryResultHandler handler)
		throws IOException, TupleQueryResultHandlerException
	{
		_checkServerURL();
		_checkRepositoryID();

		GetMethod method = new GetMethod(Protocol.getNamespacesLocation(_serverURL, _repositoryID));
		try {
			_getTupleQueryResult(method, handler);
		}
		finally {
			_releaseConnection(method);
		}
	}

	public String getNamespace(String prefix)
		throws IOException
	{
		_checkServerURL();
		_checkRepositoryID();

		GetMethod method = new GetMethod(Protocol.getNamespacePrefixLocation(_serverURL, _repositoryID, prefix));

		try {
			int httpCode = _httpClient.executeMethod(method);

			if (httpCode == HttpURLConnection.HTTP_OK) {
				return method.getResponseBodyAsString();
			}
			else if (httpCode == HttpURLConnection.HTTP_NOT_FOUND) {
				return null;
			}
			else {
				throw new IOException("Failed to get namespace: " + method.getStatusText() + " (" + httpCode
						+ ")");
			}
		}
		finally {
			_releaseConnection(method);
		}
	}

	public void setNamespacePrefix(String prefix, String name)
		throws IOException
	{
		_checkServerURL();
		_checkRepositoryID();

		PutMethod method = new PutMethod(Protocol.getNamespacePrefixLocation(_serverURL, _repositoryID, prefix));
		RequestEntity namespace = new StringRequestEntity(name, "text/plain", "UTF-8");
		method.setRequestEntity(namespace);

		try {
			int httpCode = _httpClient.executeMethod(method);

			if (httpCode < 200 || httpCode >= 300) {
				throw new IOException("Failed to set namespace: " + method.getStatusText() + " (" + httpCode
						+ ")");
			}
		}
		finally {
			_releaseConnection(method);
		}
	}

	public void removeNamespacePrefix(String prefix)
		throws IOException
	{
		_checkServerURL();
		_checkRepositoryID();

		DeleteMethod method = new DeleteMethod(Protocol.getNamespacePrefixLocation(_serverURL, _repositoryID,
				prefix));

		try {
			int httpCode = _httpClient.executeMethod(method);

			if (httpCode < 200 || httpCode >= 300) {
				throw new IOException("Failed to remove namespace: " + method.getStatusText() + " (" + httpCode
						+ ")");
			}
		}
		finally {
			_releaseConnection(method);
		}
	}

	/*-------------------------*
	 * Repository/context size *
	 *-------------------------*/

	public long size()
		throws IOException
	{
		return _size(null);
	}

	public long size(Resource... contexts)
		throws IOException
	{
		Resource context = null;
		// FIXME revise protocol!
		if (contexts.length != 0) {
			// FIXME we need to take the entire contexts into account, not just the
			// first element
			context = contexts[0];
		}
		String encodedContext = Protocol.encodeContext(context);
		return _size(encodedContext);
	}

	private long _size(String encodedContext)
		throws IOException
	{
		_checkServerURL();
		_checkRepositoryID();

		GetMethod getMethod = new GetMethod(Protocol.getSizeLocation(_serverURL, _repositoryID));

		if (encodedContext != null) {
			NameValuePair contextParam = new NameValuePair(Protocol.CONTEXT_PARAM_NAME, encodedContext);
			getMethod.setQueryString(new NameValuePair[] { contextParam });
		}

		int httpCode = _httpClient.executeMethod(getMethod);

		try {
			if (httpCode == HttpURLConnection.HTTP_OK) {
				String response = getMethod.getResponseBodyAsString();
				try {
					return Long.parseLong(response);
				}
				catch (NumberFormatException e) {
					throw new IOException("Server responded with invalid size value: " + response);
				}
			}
			else {
				throw new IOException("Failed to get size: " + getMethod.getStatusText());
			}
		}
		finally {
			getMethod.releaseConnection();
		}
	}

	/*------------------*
	 * Response parsing *
	 *------------------*/

	private void _getTupleQueryResult(HttpMethod method, TupleQueryResultHandler handler)
		throws IOException, TupleQueryResultHandlerException
	{
		// TODO: add format preferrence through 'q' attribute?
		for(TupleQueryResultFormat format:TupleQueryResultFormat.values()) {
			method.addRequestHeader("Accept", format.getMIMEType());			
		}

		int httpCode = _httpClient.executeMethod(method);

		if (httpCode == HttpURLConnection.HTTP_OK) {
			String mimeType = _getResponseMIMEType(method);
			try {
				TupleQueryResultFormat format = TupleQueryResultFormat.forMIMEType(mimeType);
				TupleQueryResultParser parser = QueryResultUtil.createParser(format, getValueFactory());

				parser.setTupleQueryResultHandler(handler);
				parser.parse(method.getResponseBodyAsStream());
			}
			catch (UnsupportedQueryResultFormatException e) {
				throw new IOException("Server responded with an unsupported file format: " + mimeType);
			}
			catch (TupleQueryResultParseException e) {
				IOException ioe = new IOException("Malformed query result from server");
				ioe.initCause(e);
				throw ioe;
			}
		}
		else {
			throw new IOException("Failed to get query result from server: " + method.getStatusText());
		}
	}

	private void _getRDF(HttpMethod method, RDFHandler handler)
		throws IOException, RDFHandlerException
	{
		// TODO: add format preferrence through 'q' attribute?
		for(RDFFormat format:RDFFormat.values()) {
			method.addRequestHeader("Accept", format.getMIMEType());			
		}

		int httpCode = _httpClient.executeMethod(method);

		if (httpCode == HttpURLConnection.HTTP_OK) {
			String mimeType = _getResponseMIMEType(method);
			try {
				RDFFormat format = RDFFormat.forMIMEType(mimeType);
				RDFParser parser = Rio.createParser(format, getValueFactory());
				parser.setPreserveBNodeIDs(false);
				parser.setRDFHandler(handler);
				parser.parse(method.getResponseBodyAsStream(), method.getURI().getURI());
			}
			catch (UnsupportedRDFormatException e) {
				throw new IOException("Server responded with an unsupported file format: " + mimeType);
			}
			catch (RDFParseException e) {
				IOException ioe = new IOException("Malformed query result from server");
				ioe.initCause(e);
				throw ioe;
			}
		}
		else {
			throw new IOException("Failed to get RDF from server: " + method.getStatusText());
		}
	}

	/*-----------------------------------------------*
	 * Utility methods related to Commons HttpClient *
	 *-----------------------------------------------*/

	/**
	 * Gets the MIME type specified in the response headers of the supplied
	 * method, if any. For example, if the response headers contain
	 * <tt>Content-Type: application/xml;charset=UTF-8</tt>, this method will
	 * return <tt>application/xml</tt> as the MIME type.
	 * 
	 * @param method
	 *        The method to get the reponse MIME type from.
	 * @return The response MIME type, or <tt>null</tt> if not available.
	 */
	private String _getResponseMIMEType(HttpMethod method)
		throws IOException
	{
		Header[] headers = method.getResponseHeaders("Content-Type");

		for (Header header : headers) {
			HeaderElement[] headerElements = header.getElements();

			for (HeaderElement headerEl : headerElements) {
				String mimeType = headerEl.getName();
				if (mimeType != null) {
					return mimeType;
				}
			}
		}

		return null;
	}

	private void _releaseConnection(HttpMethod method) {
		try {
			// Read the entire response body to enable the reuse of the connection
			InputStream responseStream = method.getResponseBodyAsStream();
			if (responseStream != null) {
				while (responseStream.read() >= 0) {
					// do nothing
				}
			}

			method.releaseConnection();
		}
		catch (IOException e) {
			logger.warn("I/O error upon releasing connection", e);
		}
	}
}
