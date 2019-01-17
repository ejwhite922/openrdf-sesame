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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 * @author vagrant
 */

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


import static org.openrdf.repository.config.RepositoryImplConfigBase.create;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.ModelException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailImplConfigBase;

/**
 * Lists the members of a federation and which properties describe a resource
 * subject in a unique member.
 */
public class ClusterFederationConfig extends SailImplConfigBase {

    /** http://www.openrdf.org/config/sail/federation# */
    public static final String NAMESPACE = "http://www.openrdf.org/config/sail/clusterfederation#";

    public static final URI MEMBER = new URIImpl(NAMESPACE + "member");

    /**
     * For all triples with a predicate in this space, the container RDF store
     * contains all triples with that subject and any predicate in this space.
     */
    public static final URI LOCALPROPERTYSPACE = new URIImpl(NAMESPACE // NOPMD
            + "localPropertySpace");

    /**
     * If no two members contain the same statement.
     */
    public static final URI DISTINCT = new URIImpl(NAMESPACE + "distinct");

    /**
     * If the federation should not try and add statements to its members.
     */
    public static final URI READ_ONLY = new URIImpl(NAMESPACE + "readOnly");

    /**
     * The zookeeper host server.
     */
    public static final URI ZK_SERVER = new URIImpl(NAMESPACE + "zkServer");

    private List<RepositoryImplConfig> members = new ArrayList<RepositoryImplConfig>();

    private final Set<String> localPropertySpace = new HashSet<String>(); // NOPMD

    private boolean distinct;

    private boolean readOnly;

    private String zkServer;

    public List<RepositoryImplConfig> getMembers() {
        return members;
    }

    public void setMembers(final List<RepositoryImplConfig> members) {
        this.members = members;
    }

    public void addMember(final RepositoryImplConfig member) {
        members.add(member);
    }

    public Set<String> getLocalPropertySpace() {
        return localPropertySpace;
    }

    public void addLocalPropertySpace(final String localPropertySpace) { // NOPMD
        this.localPropertySpace.add(localPropertySpace);
    }

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(final boolean disjoint) {
        this.distinct = disjoint;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(final boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * @return the zookeeper host server.
     */
    public String getZkServer() {
        return zkServer;
    }

    /**
     * @param zkServer the zookeeper host server. (not null)
     */
    public void setZkServer(final String zkServers) {
        this.zkServer = checkNotNull(zkServers);
    }

    @Override
    public Resource export(final Graph model) {
        final ValueFactory valueFactory = ValueFactoryImpl.getInstance();
        final Resource self = super.export(model);
        for (final RepositoryImplConfig member : getMembers()) {
            model.add(self, MEMBER, member.export(model));
        }
        for (final String space : getLocalPropertySpace()) {
            model.add(self, LOCALPROPERTYSPACE, valueFactory.createURI(space));
        }
        model.add(self, DISTINCT, valueFactory.createLiteral(distinct));
        model.add(self, READ_ONLY, valueFactory.createLiteral(readOnly));
        model.add(self, ZK_SERVER, valueFactory.createLiteral(zkServer));
        return self;
    }

    @Override
    public void parse(final Graph graph, final Resource implNode)
            throws SailConfigException {
        super.parse(graph, implNode);
        final LinkedHashModel model = new LinkedHashModel(graph);
        for (final Value member : model.filter(implNode, MEMBER, null).objects()) {
            try {
                addMember(create(graph, (Resource) member));
            } catch (final RepositoryConfigException e) {
                throw new SailConfigException(e);
            }
        }
        for (final Value space : model.filter(implNode, LOCALPROPERTYSPACE, null)
                .objects()) {
            addLocalPropertySpace(space.stringValue());
        }
        try {
            Literal bool = model.filter(implNode, DISTINCT, null)
                    .objectLiteral();
            if (bool != null && bool.booleanValue()) {
                distinct = true;
            }
            bool = model.filter(implNode, READ_ONLY, null).objectLiteral();
            if (bool != null && bool.booleanValue()) {
                readOnly = true;
            }
            final Literal zookeeper = model.filter(implNode, ZK_SERVER, null)
                    .objectLiteral();
            if (zookeeper != null) {
                zkServer = zookeeper.stringValue();
            }
        } catch (final ModelException e) {
            throw new SailConfigException(e);
        }
    }

    @Override
    public void validate() throws SailConfigException {
        super.validate();
        if (members.isEmpty()) {
            throw new SailConfigException("No cluster federation members specified");
        }
        for (final RepositoryImplConfig member : members) {
            try {
                member.validate();
            } catch (final RepositoryConfigException e) {
                throw new SailConfigException(e);
            }
        }
    }
}