package org.openrdf.sail.federation;

import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.SailReadOnlyException;
import org.openrdf.sail.federation.Federation;

class ReadOnlyClusterConnection extends AbstractClusterFederationConnection{
	
	public ReadOnlyClusterConnection(ClusterFederation federation, List<RepositoryConnection> members) {
		super(federation, members);
	}



	@Override
	public void removeNamespaceInternal(String prefix)
		throws SailException
	{
		throw new SailReadOnlyException("");
	}

	@Override
	public void addStatementInternal(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		throw new SailReadOnlyException("");
	}

	@Override
	public void removeStatementsInternal(Resource subj, URI pred, Value obj, Resource... context)
		throws SailException
	{
		throw new SailReadOnlyException("");
	}

	@Override
	protected void clearInternal(Resource... contexts) throws SailException {
		throw new SailReadOnlyException("");
	}

	@Override
	protected void commitInternal() throws SailException {
		// no-op
	}

	@Override
	protected void rollbackInternal() throws SailException {
		// no-op
	}

	@Override
	protected void startTransactionInternal() throws SailException {
		// no-op
	}



	@Override
	protected void setNamespaceInternal(String prefix, String name) throws SailException {
		// TODO Auto-generated method stub
		
	}



	@Override
	protected void clearNamespacesInternal() throws SailException {
		// TODO Auto-generated method stub
		
	}
	
}