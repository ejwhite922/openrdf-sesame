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

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.skjegstad.utils.BloomFilter;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.client.admin.TableOperations;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.io.Text;

import jcifs.smb.SmbFile;

/**
 *
 * @author vagrant
 */
public class CreateNewURIIndexTest {
	
	static public BatchWriter createWriter(Connector conn, String tableName) throws AccumuloException, AccumuloSecurityException, TableNotFoundException{
		long memBuf = 1000000L; // bytes to store before sending a batch
		long timeout = 1000L; // milliseconds to wait before sending
		int numThreads = 10;
		BatchWriter writer =
		    conn.createBatchWriter(tableName, memBuf, timeout, numThreads);		
		return writer;
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
	
	public static void main(String[] args) throws Exception {
		
		String url13 = "smb://192.168.33.30/share/3";
		SmbFile  file13 = new SmbFile(url13);
      ObjectInputStream in13 = new ObjectInputStream(file13.getInputStream());
      BloomFilter<String> instance13 = (BloomFilter)in13.readObject();
       
      in13.close();
  //    file3.close();
      
		String url15 = "smb://192.168.33.50/share/5";
		SmbFile  file15 = new SmbFile(url15);
  //    FileInputStream file5 = new FileInputStream("/home/vagrant/share/5");
      ObjectInputStream in15 = new ObjectInputStream(file15.getInputStream());
       
      // Method for deserialization of object
      BloomFilter<String> instance15 = (BloomFilter)in15.readObject();
       
      in15.close();
     
   //   file5.close();
      
		final long start = System.currentTimeMillis();
      

		
     final long phase2 = System.currentTimeMillis();
    	System.out.println((phase2-start));
     
     List<String>overlap13 = new ArrayList<String>();
     List<String>overlap15 = new ArrayList<String>();
  	String instanceName = "dev";
   String tableURI ="URI_index";
	String tableNewURI13 = "new_URI_index_13";
	String tableNewURI15 = "new_URI_index_15";


	

	String zkServer1 = "192.168.33.10:2181";


	String userName="root";

	String passWord="root";
	

 	   
	Instance inst1 = new ZooKeeperInstance(instanceName, zkServer1);
	Connector conn1 = inst1.getConnector(userName, passWord);
	
	Scanner scan1 =  conn1.createScanner(tableURI, new Authorizations());
	
	Iterator<Map.Entry<Key,Value>> iterator1 = scan1.iterator();
	  while (iterator1.hasNext()) {
		   Map.Entry<Key,Value> entry1 = iterator1.next();
		   String key1 = entry1.getKey().getRow().toString();
		   if(instance13.contains(key1)){
		   	overlap13.add(key1);
		   }
		   if(instance15.contains(key1)){
		   	overlap15.add(key1);
		   }
	  }
	   TableOperations ops = conn1.tableOperations();
      if (!ops.exists(tableNewURI13)) {
          ops.create(tableNewURI13);
     }
      if (!ops.exists(tableNewURI15)) {
         ops.create(tableNewURI15);
      }
	  addURIs(overlap13,conn1,tableNewURI13);
	  addURIs(overlap15,conn1,tableNewURI15);
	  
  	final long end = System.currentTimeMillis();
	
  	System.out.println((end-phase2));
				
	}
	

}
