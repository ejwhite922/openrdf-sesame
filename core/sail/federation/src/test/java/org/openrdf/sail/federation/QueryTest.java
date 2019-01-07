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

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

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

/**
 *
 * @author vagrant
 */
public class QueryTest {
	private static final Logger log = Logger.getLogger(QueryTest.class);

	private static final boolean USE_MOCK_INSTANCE = false;
	private static final boolean PRINT_QUERIES = false;
	private static final String INSTANCE = "dev";
	private static final String RYA_TABLE_PREFIX = "rya_";
	private static final String AUTHS = "";
	//the second VM's IP is 192.168.33.20
	private static final String SESAME_SERVER_1="http://192.168.33.10:8080/openrdf-sesame";

	private static final String repositoryID_1="large";

	

	public static void main(String[] args) throws Exception {
		String log4jConfPath = "/home/vagrant/accumulo-1.7.1/conf/log4j.properties";
		PropertyConfigurator.configure(log4jConfPath);
	    //repository 1
				Repository repo1 = new HTTPRepository(SESAME_SERVER_1, repositoryID_1);
				repo1.initialize();				


						

				RepositoryConnection con1=null;

				try {

					log.info("Connecting to SailRepository.");
	
					con1=repo1.getConnection();

					final long start = System.nanoTime();
		// execute query
					String query = "PREFIX code:<http://telegraphis.net/ontology/measurement/code#> PREFIX geographis:<http://telegraphis.net/ontology/geography/geography#> PREFIX money:<http://telegraphis.net/ontology/money/money#> PREFIX owl:<http://www.w3.org/2002/07/owl#> PREFIX gn:<http://www.geonames.org/ontology#> PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> select ?x where  {?x geographis:capital ?capital .?x geographis:currency ?currency .}";
					TupleQuery tupleQuery12 = con1.prepareTupleQuery(QueryLanguage.SPARQL, query);
		//			TupleQuery tupleQuery34 = con34.prepareTupleQuery(QueryLanguage.SPARQL, query);
		//			TupleQuery tupleQuery1234 = clusterCon1234.prepareTupleQuery(QueryLanguage.SPARQL, query);
					
					TupleQueryResult result12= tupleQuery12.evaluate();
		//			TupleQueryResult result34= tupleQuery34.evaluate();
		//			TupleQueryResult result1234= tupleQuery1234.evaluate();
				
					final long end = System.nanoTime();
					
					System.out.println(end-start);
					BindingSet bindingSet = null;
				/*  
					while(result1234.hasNext()){
					bindingSet = result1234.next();
					Value valueOfX = bindingSet.getValue("x");
					
					System.out.println(valueOfX);
					}
					*/
					while(result12.hasNext()){
					bindingSet = result12.next();
					Value valueOfX = bindingSet.getValue("x");					
					System.out.println(valueOfX);				
					}
		/*			
					while(result34.hasNext()){
					bindingSet = result34.next();
					Value valueOfX = bindingSet.getValue("x");				
					System.out.println(valueOfX);			
					}
			*/		
					
			//		TupleQuery tupleQuery_2 = con.prepareTupleQuery(QueryLanguage.SPARQL,
			//				query_2);
			//		tupleQuery_2.evaluate(resultHandler_2);
			//		log.info("Result count : " + resultHandler_2.getCount());
				 repo1.shutDown();
		
				} finally {
					con1.close();
			
				}
			}
}
