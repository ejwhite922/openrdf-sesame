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

import org.apache.accumulo.core.bloomfilter.BloomFilter;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Instance;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.file.keyfunctor.RowFunctor;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.util.hash.Hash;

/**
 *
 * @author vagrant
 */
public class CreateBloomFilterTest {
	public static void main(String[] args) throws Exception {
        final int vectorSize = 100000;
        final int nbHash = 500;
        final int hashType = Hash.MURMUR_HASH;
        final BloomFilter bloomFilter = new BloomFilter(vectorSize, nbHash, hashType);
      
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
		   final Key accumuloKey = entry.getKey();
		   final RowFunctor rowFunctor = new RowFunctor();
		   final org.apache.hadoop.util.bloom.Key key = rowFunctor.transform(accumuloKey);
		   bloomFilter.add(key);
		}
		
      FileOutputStream file = new FileOutputStream("/home/vagrant/share/3");
      ObjectOutputStream out = new ObjectOutputStream(file);
      // Method for serialization of object
      out.writeObject(bloomFilter);
       
      out.close();
      file.close();
      
   	final long end = System.currentTimeMillis();
   	
   	System.out.println((end-start));

	}

}
