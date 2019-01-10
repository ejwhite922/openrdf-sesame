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

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFFormat;

/**
 *
 * @author vagrant
 */
public class IndexingTest {
	private static final Logger log = Logger.getLogger(FederationTest.class);
	
	private static final String SESAME_SERVER="http://192.168.33.20:8080/openrdf-sesame";
	private static final String repositoryID="RyaAccumulo_2";
	
	public static void main(String[] args) throws Exception {
		Repository repo = new HTTPRepository(SESAME_SERVER, repositoryID);
		repo.initialize();
		
		File file = new File("/home/vagrant/Downloads/university_1/University1_0.daml");
		String baseURI = "file://University1_0.daml";
		
		RepositoryConnection con=null;

		   try {

				log.info("Connecting to SailRepository.");
				
            con = repo.getConnection();
            
            
	      con.add(file, baseURI, RDFFormat.RDFXML);
		      
				String query = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
						"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
						"prefix daml: <http://www.daml.org/2001/03/daml+oil#>"+
						"prefix ub: <https://rya.apache.org#>"+
						"SELECT ?X ?Y ?Z "+
						"WHERE{"+
						 "?Z rdf:type ub:Department ."+
						 "?Z ub:subOrganizationOf ?W ."+
						 "?Y rdf:type ub:University ."+
						 "?X ub:memberOf ?Z ."+
						 "?X ub:undergraduateDegreeFrom ?Y ."+
						 "?X rdf:type ub:GraduateStudent ."+
						 "}";
											
				TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
				TupleQueryResult result= tupleQuery.evaluate();
				
				BindingSet bindingSet = null;	
				int count=0;

			
				while(result.hasNext()){
					bindingSet = result.next();
					Value valueOfX = bindingSet.getValue("X");	
					Value valueOfY = bindingSet.getValue("Y");	
					Value valueOfZ = bindingSet.getValue("Z");	
					count++;
					System.out.println("X: "+valueOfX);		
					System.out.println("Y: "+valueOfY);	
					System.out.println("Z: "+valueOfZ);	
					System.out.println(bindingSet);		
				}
				System.out.println("result size: "+count);

				repo.shutDown();
		   }
		   finally {
		      con.close();
		   }
		}


	
}
