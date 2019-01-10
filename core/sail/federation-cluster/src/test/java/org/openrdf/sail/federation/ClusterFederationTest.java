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
import org.openrdf.repository.sail.SailRepositoryConnection;

public class ClusterFederationTest{
	
	private static final Logger log = Logger.getLogger(FederationTest.class);

	private static final boolean USE_MOCK_INSTANCE = false;
	private static final boolean PRINT_QUERIES = false;
	private static final String INSTANCE = "dev";
	private static final String RYA_TABLE_PREFIX = "rya_";
	private static final String AUTHS = "";
	//the second VM's IP is 192.168.33.20
	private static final String SESAME_SERVER_1="http://192.168.33.10:8080/openrdf-sesame";
	private static final String SESAME_SERVER_2="http://192.168.33.20:8080/openrdf-sesame";
	private static final String SESAME_SERVER_3="http://192.168.33.50:8080/openrdf-sesame";
	private static final String SESAME_SERVER_4="http://192.168.33.60:8080/openrdf-sesame";
	private static final String repositoryID_12="ClusterFederation12";
	private static final String repositoryID_34="ClusterFederation56";
	private static final String repositoryID_1="RyaAccumulo_1";
	private static final String repositoryID_2="RyaAccumulo_2";
	private static final String repositoryID_3="RyaAccumulo_5";
	private static final String repositoryID_4="RyaAccumulo_6";
	

	public static void main(String[] args) throws Exception {

		
	    //repository 1
				Repository repo1 = new HTTPRepository(SESAME_SERVER_1, repositoryID_1);
				repo1.initialize();				
		//repository 2
				Repository repo2 = new HTTPRepository(SESAME_SERVER_2, repositoryID_2);		
				repo2.initialize();
				
		// repository 3
				Repository repo3 = new HTTPRepository(SESAME_SERVER_3, repositoryID_3);		
				repo3.initialize();	
				
		//repository 4
				Repository repo4 = new HTTPRepository(SESAME_SERVER_4, repositoryID_4);		
				repo4.initialize();		

	//	federation repository 12
				Repository repo12 = new HTTPRepository(SESAME_SERVER_1, repositoryID_12);		
				repo12.initialize();	
						
		//federation repository 34
				Repository repo34 = new HTTPRepository(SESAME_SERVER_3, repositoryID_34);		
				repo34.initialize();	


			   SailRepository sailRepo12 =null;
				RepositoryConnection con12_34=null;
				RepositoryConnection clusterCon12=null;
				RepositoryConnection con12=null;
				RepositoryConnection con34=null;

			    SailRepository sailRepo34 =null;
				 SailRepository sailRepo1234 =null;
				try {

					log.info("Connecting to SailRepository.");
		//overlap list info
					String instanceName="dev";
					String tableName = "rya_overlap";
					String zkServer = "localhost:2181";
			      String userName="root";
			      String passWord="root";
			        
		// federation of 1,2
			//        Federation federation12 = new Federation();
			//        federation12.addMember(repo1);
			//        federation12.addMember(repo 2);
				
		        
		// Cluster federation of 1,2
				   ClusterFederation clusterFederation12=new ClusterFederation(zkServer);
			   //   Federation clusterFederation12=new Federation();
					clusterFederation12.addMember(repo1);
					clusterFederation12.addMember(repo2);

					
	    //Cluster federation of 3, 4    
					ClusterFederation clusterFederation34 = new ClusterFederation(zkServer);	
			//	Federation clusterFederation34=new Federation();
					clusterFederation34.addMember(repo3);
					clusterFederation34.addMember(repo4);
					
					
		   sailRepo12 = new SailRepository(clusterFederation12);
			sailRepo34 = new SailRepository(clusterFederation34);
			sailRepo12.initialize();
			sailRepo34.initialize();
			      
					Federation federation = new Federation();
					federation.addMember(repo12);
					federation.addMember(repo34);

		         sailRepo1234=new SailRepository(federation);
               sailRepo1234.initialize();
		         con12_34=sailRepo1234.getConnection();							

					final long start = System.currentTimeMillis();	
		// execute query
					String query = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
							"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
							"prefix daml: <http://www.daml.org/2001/03/daml+oil#>"+
							"prefix ub: <https://rya.apache.org#>"+
							"SELECT ?X ?Y ?Z "+
							"WHERE"+
							"{ "+
							 "?Y ub:teacherOf ?Z . "+							 
							 "?X ub:advisor ?Y . "+
							 "?X ub:takesCourse ?Z. "+
							 " }";
					
					
					TupleQuery tupleQuery12_34 = con12_34.prepareTupleQuery(QueryLanguage.SPARQL, query);		
					TupleQueryResult result12_34= tupleQuery12_34.evaluate();

				
					final long end = System.currentTimeMillis();	

					BindingSet bindingSet = null;
					int count=0;
					while(result12_34.hasNext()){
					bindingSet = result12_34.next();
					Value valueOfX = bindingSet.getValue("X");
					Value valueOfY = bindingSet.getValue("Y");	
					Value valueOfZ = bindingSet.getValue("Z");
					count++;
					System.out.println("X: "+valueOfX);	
					System.out.println("Y: "+valueOfY);	
					System.out.println("Z: "+valueOfZ);	
		
					}
					
				System.out.println(end-start);
				
				System.out.println("result size: "+count);
				
				 sailRepo1234.shutDown();
				 	
				} finally {
					
				con12_34.close();
					
				}
			}
}
