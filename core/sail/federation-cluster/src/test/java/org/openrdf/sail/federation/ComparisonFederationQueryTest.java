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
package org.openrdf.sail.federation;

import java.util.Iterator;

import org.apache.log4j.Logger;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.http.config.HTTPRepositoryConfig;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;

/**
 *
 * @author vagrant
 */
public class ComparisonFederationQueryTest {
	private static final Logger log = Logger.getLogger(FederationTest.class);

	private static final boolean USE_MOCK_INSTANCE = false;
	private static final boolean PRINT_QUERIES = false;
	private static final String INSTANCE = "dev";
	private static final String RYA_TABLE_PREFIX = "rya_";
	private static final String AUTHS = "";
	//the second VM's IP is 192.168.33.20
//	private static final String SESAME_SERVER_1="http://192.168.33.10:8080/openrdf-sesame";
	private static final String SESAME_SERVER_2="http://192.168.33.20:8080/openrdf-sesame";
//	private static final String SESAME_SERVER_3="http://192.168.33.30:8080/openrdf-sesame";
//	private static final String SESAME_SERVER_4="http://192.168.33.40:8080/openrdf-sesame";
	
//	private static final String repositoryID_1="RyaAccumulo_1_sec";
//	private static final String repositoryID_2="RyaAccumulo_2_sec";
//	private static final String repositoryID_3="RyaAccumulo_3_sec";
//	private static final String repositoryID_4="RyaAccumulo_4_sec";
//	private static final String repositoryID_1234="Federation1234";
	private static final String repositoryID_123456 = "Federation123456";
	

	public static void main(String[] args) throws Exception {
/*
		// repository 1
				Repository repo1 = new HTTPRepository(SESAME_SERVER_1, repositoryID_1);		
				repo1.initialize();	
						
		// repository 2
				Repository repo2 = new HTTPRepository(SESAME_SERVER_2, repositoryID_2);		
				repo2.initialize();	

		// repository 3
				Repository repo3 = new HTTPRepository(SESAME_SERVER_3, repositoryID_3);		
				repo3.initialize();	
						
		// repository 4
				Repository repo4 = new HTTPRepository(SESAME_SERVER_4, repositoryID_4);		
				repo4.initialize();	
			*/	
		
	//		   Repository repo1234 =new HTTPRepository(SESAME_SERVER_2, repositoryID_1234);
	//			RepositoryConnection con1234=null;
		
		Repository repo123456 =new HTTPRepository(SESAME_SERVER_2, repositoryID_123456);
		RepositoryConnection con123456=null;

	//		   repo1234.initialize();
		repo123456.initialize();
				try {

					log.info("Connecting to SailRepository.");
			/*        
		// federation of 1, 2, 3, 4
			        Federation federation1234 = new Federation();
			        federation1234.addMember(repo1);
			        federation1234.addMember(repo2);
			        federation1234.addMember(repo3);
			        federation1234.addMember(repo4);
				*/	



		
	   	//		con1234=repo1234.getConnection();				
					con123456=repo123456.getConnection();	

					final long start = System.currentTimeMillis();
		// execute query
					String query = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
							"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
							"prefix daml: <http://www.daml.org/2001/03/daml+oil#>"+
							"prefix ub: <https://rya.apache.org#>"+
							"SELECT ?X ?Y ?Z "+
							"WHERE{"+			
							 "?Z ub:subOrganizationOf ?Y ."+	
							 "?X ub:memberOf ?Z ."+
							 "?X ub:undergraduateDegreeFrom ?Y ."+			
							 "}";

					
					TupleQuery tupleQuery = con123456.prepareTupleQuery(QueryLanguage.SPARQL, query);
				
					TupleQueryResult result= tupleQuery.evaluate();
               
					final long end = System.currentTimeMillis();
					
					System.out.println((end-start));
					
					long count=0;
					
					BindingSet bindingSet = null;	
					
					while(result.hasNext()){
						bindingSet = result.next();
						Value valueOfX = bindingSet.getValue("X");
             //     System.out.println("X: "+valueOfX);		
					count++;
					}
					
					System.out.println("result size: "+count);

					repo123456.shutDown();
		
				} finally {
					con123456.close();

			
				} 
			}

}
