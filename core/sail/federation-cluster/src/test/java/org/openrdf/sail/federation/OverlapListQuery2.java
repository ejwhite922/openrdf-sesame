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
import java.util.Map;

import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Value;

/**
 *
 * @author vagrant
 */
public class OverlapListQuery2 {
	
	public static void main(String[] args) throws Exception {
		String instanceName="dev";
		String tableName = "rya_overlap";
		String zkServer = "localhost:2181";
        OverlapList at = new OverlapList(zkServer,instanceName);
       
        String userName="root";
        String passWord="root"; 
        
        at.getConnection(userName, passWord);
        at.selectTable(tableName);
        Scanner sc = at.createScan();

       int numDept=10;
       String univ0="http://www.University0.edu";
//       String univ2="http://www.University2.edu";
        String predicate="type";
        int studentID=100;
	//	  String rowID = "http://www.Department0.University0.edu/GraduateStudent19";
	      String rowValue= "2";
	//	  ColumnVisibility colVis = new ColumnVisibility("public");

	//Insert data  
	      for (int i=0;i<numDept;i++){
		     at.addData("http://www.Department"+i+".University0.edu", rowValue);
			  for(int j=0;j<studentID;j++){
					at.addData("http://www.Department"+i+".University0.edu"+"/GraduateStudent"+j, rowValue);  
					  
				  }
	      }

		  at.addData(univ0, rowValue);
		  
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
