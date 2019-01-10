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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.io.Text;



/**
 *
 * @author vagrant
 */
public class NHopOverlapTest {
	
	static public void computeClosure(Connector conn1,Connector conn2,String tableSPO,  Set<String>newOverlap) throws TableNotFoundException{
		
		Scanner scanSPO1 =  conn1.createScanner(tableSPO, new Authorizations());
	
		Scanner scanSPO2 =  conn2.createScanner(tableSPO, new Authorizations());
	
		
		String key = null;
		Iterator<Map.Entry<Key,Value>> iteratorSPO1 = scanSPO1.iterator();
		while(iteratorSPO1.hasNext()){
		   Map.Entry<Key,Value> entry = iteratorSPO1.next();
		   key = entry.getKey().getRow().toString();
  		   String [] pattern = key.split("\\x00");
  		   String subject = pattern[0];
	      String object = pattern[2].replaceAll("\\x01\\x02", "");
	      object = object.replaceAll("\\x01\\x03", "");
	 if (newOverlap.contains(subject)  && !(key.contains("type"))  && !(key.contains("DegreeFrom")) && !(key.contains("Publication"))){
		 newOverlap.add(object);	 
	 }	
	 if (newOverlap.contains(object)  && !(key.contains("type"))  && !(key.contains("DegreeFrom")) && !(key.contains("Publication"))){
		 newOverlap.add(subject);	 
	 }	
	}
		
	
		Iterator<Map.Entry<Key,Value>> iteratorSPO2 = scanSPO2.iterator();
		while(iteratorSPO2.hasNext()){
		   Map.Entry<Key,Value> entry = iteratorSPO2.next();
		   key = entry.getKey().getRow().toString();
  		   String [] pattern = key.split("\\x00");
  		   String subject = pattern[0];
	      String object = pattern[2].replaceAll("\\x01\\x02", "");
	      object = object.replaceAll("\\x01\\x03", "");
	 if (newOverlap.contains(subject)  && !(key.contains("type"))){
		 newOverlap.add(object);	 
	 }	
	 if (newOverlap.contains(object)  && !(key.contains("type"))){
		 newOverlap.add(subject);	 
	 }	
	}
			
	}
	
	static public BatchWriter createWriter(Connector conn, String tableName) throws AccumuloException, AccumuloSecurityException, TableNotFoundException{
		long memBuf = 1000000L; // bytes to store before sending a batch
		long timeout = 1000L; // milliseconds to wait before sending
		int numThreads = 10;
		BatchWriter writer =
		    conn.createBatchWriter(tableName, memBuf, timeout, numThreads);		
		return writer;
	}
	
	static public void addURIs(Set<String> URIs, Connector conn, String tableName) throws AccumuloException, AccumuloSecurityException, TableNotFoundException{
		
		  BatchWriter writer = createWriter(conn, tableName);
			Iterator<String> iter = URIs.iterator();
		  while(iter.hasNext()){
		  Text rowID = new Text(iter.next());
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
	public static void main(String[] args) throws Exception {
		

     final int N = 1;
      
   	String instanceName = "dev";
	   String tableSPO = "rya_spo";

	   String tableOverlap = "rya_overlap";
		
		String zkServer1 = "192.168.33.10:2181";
		String zkServer2 = "192.168.33.20:2181";


		String userName="root";

		String passWord="root";
		
   	Set<String>overlap = new HashSet<String>();
   	Set<String>newOverlap = new HashSet<String>();

	 	   
		Instance inst1 = new ZooKeeperInstance(instanceName, zkServer1);
		Connector conn1 = inst1.getConnector(userName, passWord);
		Scanner scanSPO1 =  conn1.createScanner(tableSPO, new Authorizations());

		Scanner scanOverlap = conn1.createScanner(tableOverlap, new Authorizations());
		
		Instance inst2 = new ZooKeeperInstance(instanceName, zkServer2);
		Connector conn2 = inst2.getConnector(userName, passWord);
		Scanner scanSPO2 =  conn2.createScanner(tableSPO, new Authorizations());
	
		
		String key =null;
		
		Iterator<Map.Entry<Key,Value>> iteratorOverlap = scanOverlap.iterator();
		while(iteratorOverlap.hasNext()){
		   Map.Entry<Key,Value> entry = iteratorOverlap.next();
		   key = entry.getKey().getRow().toString();
  		   overlap.add(key);		  
		}
		
   	System.out.println("size: "+overlap.size());
		
		final long start = System.currentTimeMillis();
		

	 //  Map<String,List<String>> so = new HashMap<String, List<String>>(); 
		Iterator<Map.Entry<Key,Value>> iteratorSPO1 = scanSPO1.iterator();
		while(iteratorSPO1.hasNext()){
		   Map.Entry<Key,Value> entry = iteratorSPO1.next();
		   key = entry.getKey().getRow().toString();
  		   String [] pattern = key.split("\\x00");
  		   String subject = pattern[0];
	      String object = pattern[2].replaceAll("\\x01\\x02", "");
	      object = object.replaceAll("\\x01\\x03", "");

	 if (overlap.contains(subject) && !(key.contains("type"))){
		 newOverlap.add(object);	 
	 }	
	 if (overlap.contains(object) && !(key.contains("type")) && !(key.contains("DegreeFrom")) && !(key.contains("Publication")) ){
		 newOverlap.add(subject);
	 }
	}
		

		
		Iterator<Map.Entry<Key,Value>> iteratorSPO2 = scanSPO2.iterator();
		while(iteratorSPO2.hasNext()){
		   Map.Entry<Key,Value> entry = iteratorSPO2.next();
		   key = entry.getKey().getRow().toString();
  		   String [] pattern = key.split("\\x00");
  		   String subject = pattern[0];
	      String object = pattern[2].replaceAll("\\x01\\x02", "");
	      object = object.replaceAll("\\x01\\x03", "");

	 if (overlap.contains(subject) && !(key.contains("type"))  && !(key.contains("DegreeFrom")) && !(key.contains("Publication"))){
		 newOverlap.add(object);	 
	 }	
	 if (overlap.contains(object) && !(key.contains("type"))  && !(key.contains("DegreeFrom")) && !(key.contains("Publication"))){
		 newOverlap.add(subject);
	 }
	}
		


	   for (int hop=1;hop<N;hop++){
	   	
	   	List<String>contents = new ArrayList<String>();
	   	for (String content: newOverlap){
	   		if(content.contains("org")){
              contents.add(content);
	   	}
			}
			newOverlap.removeAll(contents);
	   	System.out.println("1st: "+newOverlap.size());
	  // 	computeClosure(conn1,conn2,tableSPO, newOverlap);
	   }
     
   	System.out.println("2nd: "+newOverlap.size());
     	addURIs(newOverlap, conn1, tableOverlap); 
     	
   	final long end = System.currentTimeMillis();
   	
   	System.out.println((end-start));

	}
}
