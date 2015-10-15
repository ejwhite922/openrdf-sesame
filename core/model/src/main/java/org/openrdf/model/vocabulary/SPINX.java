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
package org.openrdf.model.vocabulary;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * http://spinrdf.org/spinx#.
 */
public final class SPINX {

	private SPINX() {}

	/**
	 * http://spinrdf.org/spinx
	 */
	public static final String NAMESPACE = "http://spinrdf.org/spinx#";

	public static final String PREFIX = "spinx";

	public static final URI JAVA_SCRIPT_CODE_PROPERTY;
	public static final URI JAVA_SCRIPT_FILE_PROPERTY;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		JAVA_SCRIPT_CODE_PROPERTY = factory.createURI(NAMESPACE, "javaScriptCode");
		JAVA_SCRIPT_FILE_PROPERTY = factory.createURI(NAMESPACE, "javaScriptFile");
	}
}
