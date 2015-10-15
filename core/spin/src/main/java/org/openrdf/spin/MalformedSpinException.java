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
package org.openrdf.spin;

import org.openrdf.OpenRDFException;

/**
 * An exception thrown by the SPIN parser when it encounters malformed
 * SPIN rules, constraints or constructors.
 */
public class MalformedSpinException extends OpenRDFException {

	private static final long serialVersionUID = -5345676977796873420L;

	public MalformedSpinException() {
		super();
	}

	public MalformedSpinException(String msg) {
		super(msg);
	}

	public MalformedSpinException(Throwable t) {
		super(t);
	}

	public MalformedSpinException(String msg, Throwable t) {
		super(msg, t);
	}
}
