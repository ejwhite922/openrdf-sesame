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
package org.openrdf.sail.federation.config;

import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.config.RepositoryRegistry;
import org.openrdf.repository.sail.config.RepositoryResolver;
import org.openrdf.repository.sail.config.RepositoryResolverClient;
import org.openrdf.sail.Sail;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailFactory;
import org.openrdf.sail.config.SailImplConfig;
import org.openrdf.sail.federation.ClusterFederation;


/**
 *
 * @author vagrant
 */
public class ClusterFederationFactory implements SailFactory,
RepositoryResolverClient{
	public static final String SAIL_TYPE = "openrdf:ClusterFederation";
	private String zkServer = "localhost:2181";
	private RepositoryResolver resolver;
	
	public Sail getSail(SailImplConfig config) throws SailConfigException {
		if (!SAIL_TYPE.equals(config.getType())) {
			throw new SailConfigException("Invalid Sail type: "
					+ config.getType());
		}
		assert config instanceof ClusterFederationConfig;
		ClusterFederationConfig cfg = (ClusterFederationConfig) config;
		ClusterFederation sail = new ClusterFederation(zkServer);
		for (RepositoryImplConfig member : cfg.getMembers()) {
			RepositoryFactory factory = RepositoryRegistry.getInstance().get(
					member.getType());
			if (factory == null) {
				throw new SailConfigException("Unsupported repository type: "
						+ config.getType());
			}
			if (factory instanceof RepositoryResolverClient) {
				((RepositoryResolverClient) factory)
						.setRepositoryResolver(resolver);
			}
			try {
				sail.addMember(factory.getRepository(member));
			} catch (RepositoryConfigException e) {
				throw new SailConfigException(e);
			}
		}
		sail.setLocalPropertySpace(cfg.getLocalPropertySpace());
		sail.setDistinct(cfg.isDistinct());
		sail.setReadOnly(cfg.isReadOnly());
		return sail;
	}

	@Override
	public void setRepositoryResolver(RepositoryResolver resolver) {
		this.resolver = resolver;
		
	}

	@Override
	public String getSailType() {
		// TODO Auto-generated method stub
		return SAIL_TYPE;
	}

	@Override
	public SailImplConfig getConfig() {
		// TODO Auto-generated method stub
		return new ClusterFederationConfig();
	}
}
