package org.openrdf.sail.federation;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.BatchWriterConfig;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.MutationsRejectedException;
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
import org.apache.log4j.*;

public class OverlapList {
	
	private String instanceName;
	private BatchWriterConfig config;
	private BatchWriter bw;
	private Scanner scan;
	private Connector conn;
	private String tableName;
	private String zkServer;
	private Instance inst;
	
	public OverlapList(String zkServer,String instanceName){
		this.zkServer=zkServer;
		this.instanceName=instanceName;
//		this.tableName=tableName;	
	}
	
	//select table, if table not exist, then create
	public void selectTable(String tableName) throws AccumuloException, AccumuloSecurityException, TableExistsException{
        TableOperations ops = conn.tableOperations();
        if (!ops.exists(tableName)) {
            ops.create(tableName);
       }
        this.tableName=tableName;

	}
	
	public BatchWriterConfig createConfig(){
		config.setMaxLatency(1, TimeUnit.MINUTES);
		config.setMaxMemory(10000000);
		config.setMaxWriteThreads(10);
		config.setTimeout(10, TimeUnit.MINUTES);
		return config;
	}
	//get connection
	public void getConnection(String userName, String passWord) throws AccumuloException, AccumuloSecurityException{
		inst = new ZooKeeperInstance(instanceName, zkServer);
		conn= inst.getConnector(userName, passWord);
	}
	
	public BatchWriter createWriter() throws AccumuloException, AccumuloSecurityException, TableNotFoundException{
	    bw = conn.createBatchWriter(tableName, config);
		return bw;
	}
	
	//Create Scan
	public Scanner createScan() throws TableNotFoundException, AccumuloException, AccumuloSecurityException{
	    scan = conn.createScanner(tableName, new Authorizations());
		return scan;
	}
	
	//Insert data into table
	public void addData(String rowId, String value) throws AccumuloException, AccumuloSecurityException, TableNotFoundException{
		  BatchWriter writer= createWriter();
		  Text rowID = new Text(rowId);
		  Text colFam = new Text("   ");
		  Text colQual = new Text("   ");
	//	  ColumnVisibility colVis = new ColumnVisibility("public");
		  long timestamp = System.currentTimeMillis();

		  Value temp_value = new Value(value.getBytes());

		  Mutation mutation = new Mutation(rowID);
		  mutation.put(colFam, colQual, timestamp, temp_value);

		  
		  writer.addMutation(mutation);      
		  writer.close();
	}
	
	public void deleteData(String rowId, String value) throws AccumuloException, AccumuloSecurityException, TableNotFoundException{
		  BatchWriter writer= createWriter();
		  Text rowID = new Text(rowId);
		  Text colFam = new Text("   ");
		  Text colQual = new Text("   ");
	//	  ColumnVisibility colVis = new ColumnVisibility("public");
		  long timestamp = System.currentTimeMillis();

		  Value temp_value = new Value(value.getBytes());

		  Mutation mutation = new Mutation(rowID);
		  mutation.putDelete(colFam, colQual, timestamp);

		  
		  writer.addMutation(mutation);      
		  writer.close();
	}
	
	public static void main(String[] args) throws Exception {
		String instanceName="dev";
		String tableName = "rya_spo";
		String zkServer = "192.168.33.50:2181";
        OverlapList at = new OverlapList(zkServer,instanceName);
        
        String userName="root";
        String passWord="root";
        
        at.getConnection(userName, passWord);
        at.selectTable(tableName);
        Scanner sc = at.createScan();

        String course15="http://www.Department0.University0.edu/Course15";
        String course16="http://www.Department0.University0.edu/Course16";
    //    String course17="http://www.Department0.University0.edu/GraduateCourse17";
    //    String course18="http://www.Department0.University0.edu/GraduateCourse18";
        
         int studentID=50;
 	//	  String rowID = "http://www.Department0.University0.edu/GraduateStudent19";
 	      String rowValue= "2";
 	//	  ColumnVisibility colVis = new ColumnVisibility("public");

 	//Insert data  
 	      for (int i=0;i<studentID;i++){
 		     at.addData("http://www.Department0.University2.edu/UndergraduateStudent"+i, rowValue);
 	      }      
 	      
           at.addData(course15, rowValue);
     //      at.addData(course16, rowValue);
   //        at.addData(course17, rowValue);
   //        at.addData(course18, rowValue);
	//Delete data
	   //   at.deleteData(rowID, rowValue);
	//Scan data	
		  Iterator<Map.Entry<Key,Value>> iterator = sc.iterator();
	//	  Set<String> result = new HashSet<String>();
		  
		  while (iterator.hasNext()) {
		   Map.Entry<Key,Value> entry = iterator.next();
		   Key key = entry.getKey();
		   Value value = entry.getValue();
		   System.out.println(key.getRow()+ " ==> " + value.toString());
		  }
	}
		
	
}
