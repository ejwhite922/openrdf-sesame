package org.openrdf.sail.federation;

import org.apache.log4j.Logger;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.federation.config.ClusterFederationConfig;

public class ClusterFederationTest {
    private static final Logger log = Logger.getLogger(ClusterFederationTest.class);

//    private static final boolean USE_MOCK_INSTANCE = false;
//    private static final boolean PRINT_QUERIES = false;
//    private static final String INSTANCE = "dev";
//    private static final String RYA_TABLE_PREFIX = "rya_";
//    private static final String AUTHS = "";
    //the second VM's IP is 192.168.33.20
    private static final String SESAME_SERVER_1 = "http://192.168.33.10:8080/openrdf-sesame";
    private static final String SESAME_SERVER_2 = "http://192.168.33.20:8080/openrdf-sesame";
    private static final String SESAME_SERVER_3 = "http://192.168.33.50:8080/openrdf-sesame";
    private static final String SESAME_SERVER_4 = "http://192.168.33.60:8080/openrdf-sesame";
    private static final String REPOSITORY_ID_12 = "ClusterFederation12";
    private static final String REPOSITORY_ID_34 = "ClusterFederation56";
    private static final String REPOSITORY_ID_1 = "RyaAccumulo_1";
    private static final String REPOSITORY_ID_2 = "RyaAccumulo_2";
    private static final String REPOSITORY_ID_3 = "RyaAccumulo_5";
    private static final String REPOSITORY_ID_4 = "RyaAccumulo_6";

    public static void main(final String[] args) throws Exception {
        // Repository 1
        final Repository repo1 = new HTTPRepository(SESAME_SERVER_1, REPOSITORY_ID_1);
        repo1.initialize();

        // Repository 2
        final Repository repo2 = new HTTPRepository(SESAME_SERVER_2, REPOSITORY_ID_2);
        repo2.initialize();

        // Repository 3
        final Repository repo3 = new HTTPRepository(SESAME_SERVER_3, REPOSITORY_ID_3);
        repo3.initialize();

        // Repository 4
        final Repository repo4 = new HTTPRepository(SESAME_SERVER_4, REPOSITORY_ID_4);
        repo4.initialize();

        // Federation repository 12
        final Repository repo12 = new HTTPRepository(SESAME_SERVER_1, REPOSITORY_ID_12);
        repo12.initialize();

        // Federation repository 34
        final Repository repo34 = new HTTPRepository(SESAME_SERVER_3, REPOSITORY_ID_34);
        repo34.initialize();

        SailRepository sailRepo12 = null;
        RepositoryConnection con12_34 = null;
//        final RepositoryConnection clusterCon12 = null;
//        final RepositoryConnection con12 = null;
//        final RepositoryConnection con34 = null;

        SailRepository sailRepo34 = null;
        SailRepository sailRepo1234 = null;
        try {
            log.info("Connecting to SailRepository.");
            // Overlap list info
            final ClusterFederationConfig config = new ClusterFederationConfig();
            final String instanceName = "dev";
            final String tableName = "rya_overlap";
            final String zkServer = "localhost:2181";
            final String username = "root";
            final String password = "root";
            config.setInstanceName(instanceName);
            config.setTableName(tableName);
            config.setZkServer(zkServer);
            config.setUsername(username);
            config.setPassword(password);

            // Federation of 1,2
//            final Federation federation12 = new Federation();
//            federation12.addMember(repo1);
//            federation12.addMember(repo2);

            // Cluster federation of 1,2
            final ClusterFederation clusterFederation12 = new ClusterFederation(config);
//            final Federation clusterFederation12 = new Federation();
            clusterFederation12.addMember(repo1);
            clusterFederation12.addMember(repo2);

            // Cluster federation of 3, 4
            final ClusterFederation clusterFederation34 = new ClusterFederation(config);
//            final Federation clusterFederation34 = new Federation();
            clusterFederation34.addMember(repo3);
            clusterFederation34.addMember(repo4);

            sailRepo12 = new SailRepository(clusterFederation12);
            sailRepo34 = new SailRepository(clusterFederation34);
            sailRepo12.initialize();
            sailRepo34.initialize();

            final Federation federation = new Federation();
            federation.addMember(repo12);
            federation.addMember(repo34);

            sailRepo1234 = new SailRepository(federation);
            sailRepo1234.initialize();
            con12_34 = sailRepo1234.getConnection();

            final long start = System.currentTimeMillis();
            // Execute query
            final String query =
                "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "prefix daml: <http://www.daml.org/2001/03/daml+oil#>\n" +
                "prefix ub: <https://rya.apache.org#>\n" +
                "SELECT ?X ?Y ?Z\n" +
                "WHERE\n" +
                "{\n" +
                "    ?Y ub:teacherOf ?Z .\n" +
                "    ?X ub:advisor ?Y .\n" +
                "    ?X ub:takesCourse ?Z.\n" +
                "}";

            final TupleQuery tupleQuery12_34 = con12_34.prepareTupleQuery(QueryLanguage.SPARQL, query);
            final TupleQueryResult result12_34 = tupleQuery12_34.evaluate();

            final long end = System.currentTimeMillis();

            BindingSet bindingSet = null;
            int count = 0;
            while (result12_34.hasNext()) {
                bindingSet = result12_34.next();
                final Value valueOfX = bindingSet.getValue("X");
                final Value valueOfY = bindingSet.getValue("Y");
                final Value valueOfZ = bindingSet.getValue("Z");
                count++;
                log.info("X: " + valueOfX);
                log.info("Y: " + valueOfY);
                log.info("Z: " + valueOfZ);
            }

            log.info(end - start);

            log.info("result size: " + count);

            sailRepo1234.shutDown();
        } finally {
            if (con12_34 != null) {
                con12_34.close();
            }
        }
    }
}