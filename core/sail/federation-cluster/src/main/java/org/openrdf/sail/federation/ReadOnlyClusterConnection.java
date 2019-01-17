package org.openrdf.sail.federation;

import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.SailReadOnlyException;

/**
 * Finishes the {@link AbstractClusterFederationConnection} by throwing
 * {@link SailReadOnlyException}s for all write operations except for setting
 * and clearing the internal namespaces.
 */
class ReadOnlyClusterConnection extends AbstractClusterFederationConnection {
    /**
     * Creates a new instance of {@link ReadOnlyClusterConnection}.
     * @param federation the {@link ClusterFederation} to connect to.
     * @param members the {@link List} of {@link RepositoryConnection}s that
     * are members of the cluster.
     */
    public ReadOnlyClusterConnection(final ClusterFederation federation, final List<RepositoryConnection> members) {
        super(federation, members);
    }

    @Override
    public void setNamespaceInternal(final String prefix, final String name) throws SailException {
    }

    @Override
    public void clearNamespacesInternal() throws SailException {
    }

    @Override
    public void removeNamespaceInternal(final String prefix)
        throws SailException
    {
        throw new SailReadOnlyException("");
    }

    @Override
    public void addStatementInternal(final Resource subj, final URI pred, final Value obj, final Resource... contexts)
        throws SailException
    {
        throw new SailReadOnlyException("");
    }

    @Override
    public void removeStatementsInternal(final Resource subj, final URI pred, final Value obj, final Resource... context)
        throws SailException
    {
        throw new SailReadOnlyException("");
    }

    @Override
    protected void clearInternal(final Resource... contexts) throws SailException {
        throw new SailReadOnlyException("");
    }

    @Override
    protected void commitInternal() throws SailException {
    }

    @Override
    protected void rollbackInternal() throws SailException {
    }

    @Override
    protected void startTransactionInternal() throws SailException {
    }
}