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

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Map;

import com.skjegstad.utils.BloomFilter;

import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;

import mvm.provenance.Hasher;

/**
 *
 * @author vagrant
 */
public class CreateBloomFilterTest {
	public static void main(String[] args) throws Exception {
      Hasher h = new Hasher(0);
      final int M = 100000;
 //     final int N = 1000;
      double proba = 0.01;
      int metric = 1;
      BloomFilter<String> instance = new BloomFilter<String>(h, proba,
              M, metric);
      
   	String instanceName = "dev";
	   String tableURI ="URI_index";
		
		String zkServer1 = "192.168.33.30:2181";


		String userName="root";

		String passWord="root";
		

	 	   
		Instance inst = new ZooKeeperInstance(instanceName, zkServer1);
		Connector conn = inst.getConnector(userName, passWord);
		Scanner scanURI =  conn.createScanner(tableURI, new Authorizations());
		
		final long start = System.currentTimeMillis();
		
		Iterator<Map.Entry<Key,Value>> iterator = scanURI.iterator();
		while(iterator.hasNext()){
		   Map.Entry<Key,Value> entry = iterator.next();
		   String key = entry.getKey().getRow().toString();
		   instance.add(key);
		}
		
      FileOutputStream file = new FileOutputStream("/home/vagrant/share/3");
      ObjectOutputStream out = new ObjectOutputStream(file);
      // Method for serialization of object
      out.writeObject(instance);
       
      out.close();
      file.close();
      
   	final long end = System.currentTimeMillis();
   	
   	System.out.println((end-start));

	}

}
