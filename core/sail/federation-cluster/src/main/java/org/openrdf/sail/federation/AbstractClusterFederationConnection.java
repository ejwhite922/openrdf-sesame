package org.openrdf.sail.federation;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.data.Key;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.DistinctIteration;

/**
 * Unions the results from multiple {@link RepositoryConnection} into one
 * {@link SailConnection}.
 */
abstract class AbstractClusterFederationConnection extends AbstractFederationConnection {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AbstractClusterFederationConnection.class);

    private final String zkServer;

    private final String instanceName;

    private final String tableName;

    private final String userName;

    private final String passWord;

    private final Set<String> includeSet = new HashSet<String>();

    private Iterator<Entry<Key, org.apache.accumulo.core.data.Value>> iterator;

    /**
     * Creates a new instance of {@link AbstractClusterFederationConnection}.
     * @param federation the {@link ClusterFederation} to connect to.
     * @param members the {@link List} of {@link RepositoryConnection}s that
     * are members of the cluster.
     */
    public AbstractClusterFederationConnection(final ClusterFederation federation,
            final List<RepositoryConnection> members) {
        super(federation, members);

        instanceName = "dev";

        tableName = "rya_overlap";

        zkServer = "localhost:2181";

        userName = "root";

        passWord = "root";

        final OverlapList at = new OverlapList(zkServer, instanceName);

        try {
            at.createConnection(userName, passWord);
            at.selectTable(tableName);
            final Scanner sc = at.createScanner();
            iterator = sc.iterator();
        } catch (final TableNotFoundException e) {
            LOGGER.error("Failed to create overlap list iterator", e);
        } catch (final AccumuloException e) {
            LOGGER.error("Failed to create overlap list iterator", e);
        } catch (final AccumuloSecurityException e) {
            LOGGER.error("Failed to create overlap list iterator", e);
        } catch (final TableExistsException e) {
            LOGGER.error("Failed to create overlap list iterator", e);
        }

        while (iterator.hasNext()) {
            final Entry<Key, org.apache.accumulo.core.data.Value> entry = iterator.next();
            includeSet.add(entry.getKey().getRow().toString());
        }
    }

    @Override
    public CloseableIteration<? extends Statement, SailException> getStatementsInternal(
            final Resource subj, final URI pred, final Value obj,
            final boolean includeInferred, final Resource... contexts)
            throws SailException {

        LOGGER.debug("cluster federation get statement internal");

        CloseableIteration<? extends Statement, SailException> cursor = super.getStatementsInternal(subj, pred, obj, includeInferred, contexts);
        if (cursor instanceof DistinctIteration) {
            cursor = new IntersectOverlapList<Statement, SailException>(cursor, includeSet);
        }

        return cursor;
    }
}