package org.openrdf.sail.federation;

import java.util.Iterator;
import java.util.List;


import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.http.config.HTTPRepositoryConfig;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.sail.federation.Federation;
import org.apache.log4j.Logger;

public class FederationTest{
	
	private static final Logger log = Logger.getLogger(FederationTest.class);

	private static final boolean USE_MOCK_INSTANCE = false;
	private static final boolean PRINT_QUERIES = false;
	private static final String INSTANCE = "dev";
	private static final String RYA_TABLE_PREFIX = "rya_";
	private static final String AUTHS = "";
	//the second VM's IP is 192.168.33.20
	private static final String SESAME_SERVER_1="http://192.168.33.10:8080/openrdf-sesame";
	private static final String SESAME_SERVER_2="http://192.168.33.20:8080/openrdf-sesame";
	private static final String SESAME_SERVER_3="http://192.168.33.30:8080/openrdf-sesame";
	private static final String SESAME_SERVER_4="http://192.168.33.40:8080/openrdf-sesame";
	private static final String repositoryID_1="RyaAccumulo_1";
	private static final String repositoryID_2="Federation12";	
	private static final String repositoryID_3="RyaAccumulo_3";
	private static final String repositoryID_4="RyaAccumulo_4";

	

	public static void main(String[] args) throws Exception {
		// repository 1
		Repository repo1 = new HTTPRepository(SESAME_SERVER_2, repositoryID_2);		
		repo1.initialize();	
		
//repository 2
//		Repository repo2 = new HTTPRepository(SESAME_SERVER_2, repositoryID_2);		
//		repo2.initialize();	
				
		// repository 3
	//			Repository repo3 = new HTTPRepository(SESAME_SERVER_3, repositoryID_3);		
	//			repo3.initialize();	
				
		//repository 4
		//		Repository repo4 = new HTTPRepository(SESAME_SERVER_4, repositoryID_4);		
		//		repo4.initialize();		

				RepositoryConnection con1234=null;
		//	    SailRepository sailRepo12 =null;
		//	    SailRepository sailRepo34 =null;
		//	    SailRepository sailRepo1234 =null;
				try {

					log.info("Connecting to SailRepository.");
		//overlap list info
					String instanceName="dev";
					String tableName = "rya_overlap";
					String zkServer = "localhost:2181";
			        String userName="root";
			        String passWord="root";

		//Cluster federation of 1, 2    
			//	   ClusterFederation clusterFederation12=new ClusterFederation(zkServer);					
			//		clusterFederation12.addMember(repo1);
			//		clusterFederation12.addMember(repo2);				
	    //Cluster federation of 3, 4    
			//		ClusterFederation clusterFederation34=new ClusterFederation(zkServer);					
			//		clusterFederation34.addMember(repo3);
			//		clusterFederation34.addMember(repo4);

			//	   sailRepo12 = new SailRepository(clusterFederation12);
			//		sailRepo12.initialize();
			//	   sailRepo34 = new SailRepository(clusterFederation34);
			//		sailRepo34.initialize();
	
				//	Federation Federation1234=new Federation();					
				//	Federation1234.addMember(sailRepo12);
				//	Federation1234.addMember(sailRepo34);

				 //  sailRepo1234 = new SailRepository(Federation1234);
					con1234=repo1.getConnection();
			//		con34=sailRepo34.getConnection();

		//
			
		/*			
		// create a new repository and id			
					RemoteRepositoryManager manager = new RemoteRepositoryManager(SESAME_SERVER_3);
					manager.initialize();
									
					String repositoryId = "test_cfs";
					RepositoryImplConfig implConfig= new HTTPRepositoryConfig(SESAME_SERVER_3);
					RepositoryConfig repConfig = new RepositoryConfig(repositoryId, implConfig);
					manager.addRepositoryConfig(repConfig);
					
		// create a repository variable from a given id			
					 Repository Repo34 =manager.getRepository(repositoryId);
					Repo34.initialize();
					con34=Repo34.getConnection();
				
					test = new SailRepository(federation1234);
					
					
	   // remove a repository
		//			manager.removeRepository(repositoryId);
				
	   //get repository IDs			
					Iterator <String> ids =manager.getRepositoryIDs().iterator();
					while (ids.hasNext()) {
						System.out.println(ids.next());
					}
                    
					con_test = (SailRepositoryConnection) test.getConnection();
			*/		
					

					final long start = System.currentTimeMillis();
		// execute query
					/*
					String query = "	PREFIX code:<http://telegraphis.net/ontology/measurement/code#>" 
					+"PREFIX geographis:<http://telegraphis.net/ontology/geography/geography#>" 
						+"PREFIX money:<http://telegraphis.net/ontology/money/money#>" 
						+"PREFIX owl:<http://www.w3.org/2002/07/owl#>" 
						+"PREFIX gn:<http://www.geonames.org/ontology#>" 
						+"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>" 
						+"select ?x where"  
						+"{?x geographis:capital ?capital ."
						+"?x geographis:currency ?currency .}";	
					*/
					/* 
					String query = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
							"prefix dc: <http://purl.org/dc/elements/1.1/>"+ 
								"prefix foaf: <http://xmlns.com/foaf/0.1/>"+ 
								"prefix foafcorp: <http://xmlns.com/foaf/corp#>"+ 
								"prefix vcard: <http://www.w3.org/2001/vcard-rdf/3.0#>"+ 
								"prefix sec: <http://www.rdfabout.com/rdf/schema/ussec/>"+ 
								"prefix id: <http://www.rdfabout.com/rdf/usgov/sec/id/>"+ 
								"select ?x where  {"+
								"?x rdf:type foaf:Person."+
								"?x vcard:ADR ?address."+
								"}";
					*/

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
					
					TupleQuery tupleQuery1234 = con1234.prepareTupleQuery(QueryLanguage.SPARQL, query);
		//			TupleQuery tupleQuery1234 = clusterCon1234.prepareTupleQuery(QueryLanguage.SPARQL, query);
					
		//			TupleQueryResult result12= tupleQuery12.evaluate();
					TupleQueryResult result1234= tupleQuery1234.evaluate();
		//			TupleQueryResult result1234= tupleQuery1234.evaluate();
				
					final long end = System.currentTimeMillis();
					System.out.println("execution time: "+(end-start));
					BindingSet bindingSet = null;
					int count=0;
				/*  
					while(result1234.hasNext()){
					bindingSet = result1234.next();
					Value valueOfX = bindingSet.getValue("x");
					
					System.out.println(valueOfX);
					}
				
					while(result12.hasNext()){
					bindingSet = result12.next();
					Value valueOfX = bindingSet.getValue("x");					
					System.out.println(valueOfX);				
					}
		 */
					while(result1234.hasNext()){
					bindingSet = result1234.next();
					Value valueOfX = bindingSet.getValue("X");	
					Value valueOfY = bindingSet.getValue("Y");	
					Value valueOfZ = bindingSet.getValue("Z");	
					count++;
					}
					
					System.out.println("result size: "+count);
			//		TupleQuery tupleQuery_2 = con.prepareTupleQuery(QueryLanguage.SPARQL,
			//				query_2);
			//		tupleQuery_2.evaluate(resultHandler_2);
			//		log.info("Result count : " + resultHandler_2.getCount());
		//		sailRepo1234.shutDown();
		//		 sailRepo12.shutDown();
		//		 sailRepo34.shutDown();
				 repo1.shutDown();
		//		 repo2.shutDown();
		//		 repo3.shutDown();
		//		 repo4.shutDown();
		
				} finally {
		//			con12.close();				
					con1234.close();
			
				}
			}

	
		
}