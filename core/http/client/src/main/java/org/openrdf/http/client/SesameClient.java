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
package org.openrdf.http.client;

import org.apache.http.client.HttpClient;

/**
 * Manages remote HTP connections.
 * 
 * @author James Leigh
 */
public interface SesameClient {

	/**
	 * @return Returns the httpClient.
	 */
	HttpClient getHttpClient();

	/**
	 * Creates a new session to the remote SPARQL endpoint to manage the auth
	 * state.
	 */
	SparqlSession createSparqlSession(String queryEndpointUrl, String updateEndpointUrl);

	/**
	 * Creates a new session to the remote Sesame server to manage the auth
	 * state.
	 */
	SesameSession createSesameSession(String serverURL);

	/**
	 * Closes any remaining TCP connections and threads used by the sessions
	 * created by this object.
	 */
	void shutDown();

}
