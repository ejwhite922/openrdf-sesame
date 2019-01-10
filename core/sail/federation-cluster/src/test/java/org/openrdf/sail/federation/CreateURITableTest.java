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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.client.admin.TableOperations;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.io.Text;

/**
 *
 * @author vagrant
 */
public class CreateURITableTest {
	
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
	String instanceName = "dev";
   String tableURI ="URI_index";
	String tableSPO = "rya_spo";
	String tableOSP ="rya_osp";
	

	String zkServer1 = "192.168.33.10:2181";
	String zkServer2 = "192.168.33.20:2181";

	String userName="root";

	String passWord="root";
	
   Set<String> list = new HashSet<String>();
   


   Instance inst1 = new ZooKeeperInstance(instanceName, zkServer1);
	Connector conn1 = inst1.getConnector(userName, passWord);
	
   Instance inst2 = new ZooKeeperInstance(instanceName, zkServer2);
	Connector conn2 = inst2.getConnector(userName, passWord);
	
	

	Scanner scan2SPO =  conn2.createScanner(tableSPO, new Authorizations());
	Scanner scan2OSP =  conn2.createScanner(tableOSP, new Authorizations());
	Scanner scan1SPO =  conn1.createScanner(tableSPO, new Authorizations());
	Scanner scan1OSP =  conn1.createScanner(tableOSP, new Authorizations());
	
   Iterator<Entry<Key, Value>> iterator1SPO = scan1SPO.iterator();
   Iterator<Entry<Key, Value>> iterator1OSP = scan1OSP.iterator();
   Iterator<Entry<Key, Value>> iterator2SPO = scan2SPO.iterator();
   Iterator<Entry<Key, Value>> iterator2OSP = scan2OSP.iterator();
   
	final long start = System.currentTimeMillis();
      
      
   	  while (iterator1SPO.hasNext()) {
  		   Entry<Key, org.apache.accumulo.core.data.Value> entry = iterator1SPO.next();
  		   String [] pattern = entry.getKey().getRow().toString().split("\\x00");
          list.add(pattern[0]);
  		  }
   	  
   	  
  	  
   	  while (iterator1OSP.hasNext()) {
    		   Entry<Key, org.apache.accumulo.core.data.Value> entry = iterator1OSP.next();
    		   String [] pattern = entry.getKey().getRow().toString().split("\\x00");
            list.add(pattern[0]);
    		  }
   	  
   	  while (iterator2SPO.hasNext()) {
    		   Entry<Key, org.apache.accumulo.core.data.Value> entry = iterator2SPO.next();
    		   String [] pattern = entry.getKey().getRow().toString().split("\\x00");
            list.add(pattern[0]);
    		  }
   	  
   	  while (iterator2OSP.hasNext()) {
    		   Entry<Key, org.apache.accumulo.core.data.Value> entry = iterator2OSP.next();
    		   String [] pattern = entry.getKey().getRow().toString().split("\\x00");
            list.add(pattern[0]);
    		  }
   
   TableOperations ops = conn1.tableOperations();
        if (!ops.exists(tableURI)) {
            ops.create(tableURI);
       }
   System.out.println("size: "+list.size());
   addURIs(list,conn1,tableURI);
   	  
	final long end = System.currentTimeMillis();
	
	System.out.println((end-start));

	}
		 
}
