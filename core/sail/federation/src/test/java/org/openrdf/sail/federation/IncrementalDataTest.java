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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.BatchWriterConfig;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.io.Text;

/**
 *
 * @author vagrant
 */
public class IncrementalDataTest {
	

	
	static public BatchWriter createWriter(Connector conn, String tableName) throws AccumuloException, AccumuloSecurityException, TableNotFoundException{
		long memBuf = 1000000L; // bytes to store before sending a batch
		long timeout = 1000L; // milliseconds to wait before sending
		int numThreads = 10;
		BatchWriter writer =
		    conn.createBatchWriter(tableName, memBuf, timeout, numThreads);		
		return writer;
	}
	static public boolean lookUp(String URI, Scanner scan) throws TableNotFoundException{
			scan.setRange(Range.exact(URI));
			Iterator<Map.Entry<Key,Value>> iterator = scan.iterator();
			  if (iterator.hasNext()) {
				   	return true;
				   }				   			  	
		return false;
	}
	
	static public void addURI(String URI, Connector conn, String tableName) throws AccumuloException, AccumuloSecurityException, TableNotFoundException{
		
		  BatchWriter writer = createWriter(conn, tableName);
		 
		  Text rowID = new Text(URI);
		  Text colFam = new Text("   ");
		  Text colQual = new Text("   ");
	//	  ColumnVisibility colVis = new ColumnVisibility("public");
		  long timestamp = System.currentTimeMillis();

		  Value temp_value = new Value("2".getBytes());

		  Mutation mutation = new Mutation(rowID);
		  mutation.put(colFam, colQual, timestamp, temp_value);		  
		  writer.addMutation(mutation);      
		  
		  writer.close();
		
	}
	
	static public void addURIs(List<String> URIs, Connector conn, String tableName) throws AccumuloException, AccumuloSecurityException, TableNotFoundException{
		
		  BatchWriter writer = createWriter(conn, tableName);
		  for(int i=0;i<URIs.size();i++){
		  Text rowID = new Text(URIs.get(i));
		  Text colFam = new Text("   ");
		  Text colQual = new Text("   ");
	//	  ColumnVisibility colVis = new ColumnVisibility("public");
		  long timestamp = System.currentTimeMillis();

		  Value temp_value = new Value("2".getBytes());

		  Mutation mutation = new Mutation(rowID);
		  mutation.put(colFam, colQual, timestamp, temp_value);		  
		  writer.addMutation(mutation);      
		  }
		  writer.close();
		
	}
	
	static public void intersectOverlap(String URI, Connector conn, Connector conn2,String tableOverlap) throws AccumuloException, AccumuloSecurityException, TableNotFoundException{
			   	addURI(URI, conn, tableOverlap);
			   	addURI(URI, conn2, tableOverlap);
			   
	}
	
	static public void intersectData3(Scanner scan1NewURI,Scanner scan3URI, Connector conn3,String tableOverlap) throws AccumuloException, AccumuloSecurityException, TableNotFoundException{
		
      List<String>overlapData = new ArrayList<String>();
		Iterator<Map.Entry<Key,Value>> iterator1 = scan1NewURI.iterator();
		  while (iterator1.hasNext()) {
			   Map.Entry<Key,Value> entry1 = iterator1.next();
			   String key1 = entry1.getKey().getRow().toString();
				scan3URI.setRange(Range.exact(key1));
				Iterator<Map.Entry<Key,Value>> iterator3 = scan3URI.iterator();
					   if(iterator3.hasNext()){
					  overlapData.add(key1);
				   					   
				  }
			   }
		  if (overlapData.size()>0)
	       addURIs(overlapData,conn3,tableOverlap);		
	}
	
	static public void intersectData5(Scanner scan1NewURI,Scanner scan5URI, Connector conn5,String tableOverlap) throws AccumuloException, AccumuloSecurityException, TableNotFoundException{
		
      List<String>overlapData = new ArrayList<String>();
		Iterator<Map.Entry<Key,Value>> iterator1 = scan1NewURI.iterator();

		  while (iterator1.hasNext()) {
			   Map.Entry<Key,Value> entry1 = iterator1.next();
			   String key1 = entry1.getKey().getRow().toString();
				scan5URI.setRange(Range.exact(key1));
				Iterator<Map.Entry<Key,Value>> iterator5 = scan5URI.iterator();
			   if(iterator5.hasNext()){
			  // 	System.out.println("overlap51: "+key1);
					  overlapData.add(key1);
					   }					   
				  }
		  if (overlapData.size()>0)
		       addURIs(overlapData,conn5,tableOverlap);	
	}
	
	public static void main(String[] args) throws Exception {
		

		List<String>triple = new ArrayList<String>();
		for (int i=12;i<13;i++){

		for (int j=270;j<271;j++){
	//	triple.add("http://www.Department"+i+".University2.edu/UndergraduateStudent"+j);
		triple.add("http://www.Department"+i+".University3.edu/UndergraduateStudent"+j);
	//	triple.add("http://www.Department"+i+".University4.edu/UndergraduateStudent"+j);
	//	triple.add("http://www.Department"+i+".University5.edu/UndergraduateStudent"+j);
		}

	}
		
		String instanceName = "dev";
	   String tableURI ="URI_index";
		String tableNewURI = "new_URI_index";
		String tableOverlap ="rya_overlap";

		

		String zkServer1 = "192.168.33.10:2181";
		String zkServer3 = "192.168.33.30:2181";
		String zkServer5 = "192.168.33.50:2181";

		String userName="root";

		String passWord="root";
		

	 	   
		Instance inst1 = new ZooKeeperInstance(instanceName, zkServer1);
		Connector conn1 = inst1.getConnector(userName, passWord);
		Scanner scan1NewURI =  conn1.createScanner(tableNewURI, new Authorizations());
		Scanner scan1URI =  conn1.createScanner(tableURI, new Authorizations());
		
		Instance inst3 = new ZooKeeperInstance(instanceName, zkServer3);
		Connector conn3 = inst3.getConnector(userName, passWord);
		Scanner scan3URI =  conn3.createScanner(tableURI, new Authorizations());
		
		
		Instance inst5 = new ZooKeeperInstance(instanceName, zkServer5);
		Connector conn5 = inst5.getConnector(userName, passWord);
		Scanner scan5URI =  conn5.createScanner(tableURI, new Authorizations());
		
		
      List<String>incrementalData= new ArrayList<String>();
	   try {
	//   final long start = System.currentTimeMillis();	
		   final long start = System.currentTimeMillis();	 
	 
	   	

	   	 	
	   	for (int j=0;j<triple.size();j++){	   		
	   	if (lookUp(triple.get(j), scan1URI)==false){
	   		System.out.println("new URI: "+triple.get(j));
   		incrementalData.add(triple.get(j));
	   	}	   	
	   }     
	   

	   if(incrementalData.size()>0){
			addURIs(incrementalData,conn1,tableURI);
			addURIs(incrementalData,conn1,tableNewURI);
	   }
	   final long phase1=System.currentTimeMillis();
		System.out.println("phase 1 execution time: "+(phase1-start));
	   
		intersectData3(scan1NewURI, scan3URI, conn3, tableOverlap);
		intersectData5(scan1NewURI, scan5URI, conn5, tableOverlap);	
	   	   
	   final long end = System.currentTimeMillis();
		System.out.println("phase 2 execution time: "+(end-phase1));
		}
		catch (TableNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

}
