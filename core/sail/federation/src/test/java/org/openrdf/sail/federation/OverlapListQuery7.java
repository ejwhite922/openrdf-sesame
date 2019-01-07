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
public class OverlapListQuery7 {
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

       int numDept=5;
       int numUnderStudent=20;
       int numGraduateStudent=20;
       String univ0="University0.edu";
       String univ4="University4.edu";
       String univ2="University2.edu";
       String univ5="University5.edu";
       String univ3="University3.edu";
       String univ1="University1.edu";
       String dept0="Department0";
       String dept1="Department1";
       String dept2="Department2";
       String dept3="Department3";
       String course1 ="CourseU1";
       String course2 ="GraduateCourseU1";
       String course3 ="CourseU2";
       String course4 ="GraduateCourseU2";
       String course5 ="CourseU3";
       String course6 ="GraduateCourseU3";
       String course7 ="CourseU4";
       String course8 ="GraduateCourseU4";
       String course15 ="Course15";
       String course16 ="Course16";
       String course17 ="GraduateCourse17";
       String course18 ="GraduateCourse18";
       
       String professor ="FullProfessor1";
       String professor0="AssociateProfessor0";
       String professor1="AssociateProfessor1";
       String professor2="AssociateProfessor2";
       String studentIDU001="118";
       String studentIDU002="275";
       String studentIDU003="303";
       String studentIDU004="382";
       String studentIDU005="392";
       String studentIDU006="472";
       String studentIDU007="98";
       String studentIDU008="130";
       
       String studentIDU011="26";
       String studentIDU012="281";
       String studentIDU013="283";
       String studentIDU014="293";
       String studentIDU015="384";
       String studentIDU016="36";
       String studentIDU017="44";
       String studentIDU018="50";
       String studentIDU019="92";
       
       String studentIDU021="157";
       String studentIDU022="374";
       String studentIDU023="68";
       String studentIDU024="103";
       String studentIDU025="106";

       String studentIDU031="29";
       String studentIDU032="33";
       String studentIDU033="142";
       String studentIDU034="160";
       String studentIDU035="179";
       String studentIDU036="24";
       String studentIDU037="56";
       String studentIDU038="62";
       String studentIDU039="100";       
       
       
       String studentIDU401="17";
       String studentIDU402="24";
       String studentIDU403="27";
       String studentIDU404="48";
       String studentIDU405="65";
       String studentIDU406="76";
       String studentIDU407="127";
       
       String studentIDU411="7";
       String studentIDU412="343";
       String studentIDU413="62";
       String studentIDU414="78";
       String studentIDU415="82";
       String studentIDU416="84";
       String studentIDU417="88";
       
       String studentIDU421="154";
       String studentIDU422="293";
       String studentIDU423="407";
       String studentIDU424="56";
       String studentIDU425="67";
       String studentIDU426="71";
       String studentIDU427="88";
       String studentIDU428="102";
       String studentIDU429="114";
       
       String studentIDU431="6";
       String studentIDU432="26";
       String studentIDU433="236";
       String studentIDU434="307";
       String studentIDU435="8";
       String studentIDU436="17";
       String studentIDU437="93";
       String studentIDU438="109";
       
       
	    String rowValue= "2";
	    
	 	//Insert data  
	    
	    //query2
		 at.addData("http://www"+"."+univ0, rowValue);   
		 at.addData("http://www"+"."+univ4, rowValue);   
		 at.addData("http://www."+dept0+"."+univ0, rowValue);
		 at.addData("http://www."+dept0+"."+univ4, rowValue);
		 
	      for (int i=0;i<15;i++){
	     at.addData("http://www."+dept0+"."+univ4+"/"+"GraduateStudent"+i, rowValue);
	      }
	      for (int i=0;i<15;i++){
	     at.addData("http://www."+dept0+"."+univ0+"/"+"GraduateStudent"+i, rowValue);
	      }
	    //query4  
	    at.addData("http://www."+dept0+"."+univ0+"/"+professor0, rowValue);
	    at.addData("http://www."+dept0+"."+univ0+"/"+professor1, rowValue);
	    at.addData("http://www."+dept0+"."+univ0+"/"+professor2, rowValue);
	    //query7	    
		 at.addData("http://www."+dept0+"."+univ0+"/"+course15, rowValue);
		 at.addData("http://www."+dept0+"."+univ0+"/"+course16, rowValue);
		 at.addData("http://www."+dept0+"."+univ0+"/"+course17, rowValue);
		 at.addData("http://www."+dept0+"."+univ0+"/"+course18, rowValue);
	      for (int i=0;i<numUnderStudent;i++){
	     at.addData("http://www."+dept0+"."+univ2+"/"+"UndergraduateStudent"+i, rowValue);
	      }
	      for (int i=0;i<numUnderStudent;i++){
		     at.addData("http://www."+dept0+"."+univ4+"/"+"UndergraduateStudent"+i, rowValue);
	      }
	      for (int i=0;i<numGraduateStudent;i++){
	     at.addData("http://www."+dept0+"."+univ2+"/"+"GraduateStudent"+i, rowValue);
	      }
	      for (int i=0;i<numGraduateStudent;i++){
		     at.addData("http://www."+dept0+"."+univ4+"/"+"GraduateStudent"+i, rowValue);
	      }
       //query9
	      
	      for (int i=0;i<numDept;i++){
		     at.addData("http://www."+"Department"+i+"."+univ0+"/"+professor, rowValue);
	      }
	      for (int i=0;i<numDept;i++){
		     at.addData("http://www."+"Department"+i+"."+univ4+"/"+professor, rowValue);
	      }

		 at.addData("http://www."+dept0+"."+univ3+"/"+course1, rowValue);
		 at.addData("http://www."+dept0+"."+univ3+"/"+course2, rowValue);  
		 at.addData("http://www."+dept0+"."+univ1+"/"+course1, rowValue);
		 at.addData("http://www."+dept0+"."+univ1+"/"+course2, rowValue);
		 at.addData("http://www."+dept1+"."+univ3+"/"+course3, rowValue);
		 at.addData("http://www."+dept1+"."+univ3+"/"+course4, rowValue);  
		 at.addData("http://www."+dept1+"."+univ1+"/"+course3, rowValue);
		 at.addData("http://www."+dept1+"."+univ1+"/"+course4, rowValue);
		 at.addData("http://www."+dept2+"."+univ3+"/"+course5, rowValue);
		 at.addData("http://www."+dept2+"."+univ3+"/"+course6, rowValue);  
		 at.addData("http://www."+dept2+"."+univ1+"/"+course5, rowValue);
		 at.addData("http://www."+dept2+"."+univ1+"/"+course6, rowValue);
		 at.addData("http://www."+dept3+"."+univ3+"/"+course7, rowValue);
		 at.addData("http://www."+dept3+"."+univ3+"/"+course8, rowValue);  
		 at.addData("http://www."+dept3+"."+univ1+"/"+course7, rowValue);
		 at.addData("http://www."+dept3+"."+univ1+"/"+course8, rowValue);
		 at.addData("http://www."+dept2+"."+univ1, rowValue);
		 

		 at.addData("http://www."+dept0+"."+univ0+"/"+"UndergraduateStudent"+studentIDU001, rowValue);
		 at.addData("http://www."+dept0+"."+univ0+"/"+"UndergraduateStudent"+studentIDU002, rowValue);
		 at.addData("http://www."+dept0+"."+univ0+"/"+"UndergraduateStudent"+studentIDU003, rowValue);
		 at.addData("http://www."+dept0+"."+univ0+"/"+"UndergraduateStudent"+studentIDU004, rowValue);
		 at.addData("http://www."+dept0+"."+univ0+"/"+"UndergraduateStudent"+studentIDU005, rowValue);
		 at.addData("http://www."+dept0+"."+univ0+"/"+"UndergraduateStudent"+studentIDU006, rowValue);
		 at.addData("http://www."+dept0+"."+univ0+"/"+"GraduateStudent"+studentIDU007, rowValue);
		 at.addData("http://www."+dept0+"."+univ0+"/"+"GraduateStudent"+studentIDU008, rowValue);
		 
		 at.addData("http://www."+dept1+"."+univ0+"/"+"UndergraduateStudent"+studentIDU011, rowValue);
		 at.addData("http://www."+dept1+"."+univ0+"/"+"UndergraduateStudent"+studentIDU012, rowValue);
		 at.addData("http://www."+dept1+"."+univ0+"/"+"UndergraduateStudent"+studentIDU013, rowValue);
		 at.addData("http://www."+dept1+"."+univ0+"/"+"UndergraduateStudent"+studentIDU014, rowValue);
		 at.addData("http://www."+dept1+"."+univ0+"/"+"UndergraduateStudent"+studentIDU015, rowValue);
		 at.addData("http://www."+dept1+"."+univ0+"/"+"GraduateStudent"+studentIDU016, rowValue);
		 at.addData("http://www."+dept1+"."+univ0+"/"+"GraduateStudent"+studentIDU017, rowValue);
		 at.addData("http://www."+dept1+"."+univ0+"/"+"GraduateStudent"+studentIDU018, rowValue);
		 at.addData("http://www."+dept1+"."+univ0+"/"+"GraduateStudent"+studentIDU019, rowValue);	
		 
		 at.addData("http://www."+dept2+"."+univ0+"/"+"UndergraduateStudent"+studentIDU021, rowValue);
		 at.addData("http://www."+dept2+"."+univ0+"/"+"UndergraduateStudent"+studentIDU022, rowValue);
		 at.addData("http://www."+dept2+"."+univ0+"/"+"GraduateStudent"+studentIDU023, rowValue);
		 at.addData("http://www."+dept2+"."+univ0+"/"+"GraduateStudent"+studentIDU024, rowValue);
		 at.addData("http://www."+dept2+"."+univ0+"/"+"GraduateStudent"+studentIDU025, rowValue);
		 
		 at.addData("http://www."+dept3+"."+univ0+"/"+"UndergraduateStudent"+studentIDU031, rowValue);
		 at.addData("http://www."+dept3+"."+univ0+"/"+"UndergraduateStudent"+studentIDU032, rowValue);
		 at.addData("http://www."+dept3+"."+univ0+"/"+"UndergraduateStudent"+studentIDU033, rowValue);
		 at.addData("http://www."+dept3+"."+univ0+"/"+"UndergraduateStudent"+studentIDU034, rowValue);
		 at.addData("http://www."+dept3+"."+univ0+"/"+"UndergraduateStudent"+studentIDU035, rowValue);
		 at.addData("http://www."+dept3+"."+univ0+"/"+"GraduateStudent"+studentIDU036, rowValue);
		 at.addData("http://www."+dept3+"."+univ0+"/"+"GraduateStudent"+studentIDU037, rowValue);
		 at.addData("http://www."+dept3+"."+univ0+"/"+"GraduateStudent"+studentIDU038, rowValue);
		 at.addData("http://www."+dept3+"."+univ0+"/"+"GraduateStudent"+studentIDU039, rowValue);		 
		 
		 at.addData("http://www."+dept0+"."+univ4+"/"+"UndergraduateStudent"+studentIDU401, rowValue);
		 at.addData("http://www."+dept0+"."+univ4+"/"+"GraduateStudent"+studentIDU402, rowValue);
		 at.addData("http://www."+dept0+"."+univ4+"/"+"GraduateStudent"+studentIDU403, rowValue);
		 at.addData("http://www."+dept0+"."+univ4+"/"+"GraduateStudent"+studentIDU404, rowValue);
		 at.addData("http://www."+dept0+"."+univ4+"/"+"GraduateStudent"+studentIDU405, rowValue);
		 at.addData("http://www."+dept0+"."+univ4+"/"+"GraduateStudent"+studentIDU406, rowValue);
		 at.addData("http://www."+dept0+"."+univ4+"/"+"GraduateStudent"+studentIDU407, rowValue);
		 
		 at.addData("http://www."+dept1+"."+univ4+"/"+"UndergraduateStudent"+studentIDU411, rowValue);
		 at.addData("http://www."+dept1+"."+univ4+"/"+"UndergraduateStudent"+studentIDU412, rowValue);
		 at.addData("http://www."+dept1+"."+univ4+"/"+"GraduateStudent"+studentIDU413, rowValue);
		 at.addData("http://www."+dept1+"."+univ4+"/"+"GraduateStudent"+studentIDU414, rowValue);
		 at.addData("http://www."+dept1+"."+univ4+"/"+"GraduateStudent"+studentIDU415, rowValue);
		 at.addData("http://www."+dept1+"."+univ4+"/"+"GraduateStudent"+studentIDU416, rowValue);
		 at.addData("http://www."+dept1+"."+univ4+"/"+"GraduateStudent"+studentIDU417, rowValue);
		 
		 at.addData("http://www."+dept2+"."+univ4+"/"+"UndergraduateStudent"+studentIDU421, rowValue);
		 at.addData("http://www."+dept2+"."+univ4+"/"+"UndergraduateStudent"+studentIDU422, rowValue);
		 at.addData("http://www."+dept2+"."+univ4+"/"+"UndergraduateStudent"+studentIDU423, rowValue);
		 at.addData("http://www."+dept2+"."+univ4+"/"+"GraduateStudent"+studentIDU424, rowValue);
		 at.addData("http://www."+dept2+"."+univ4+"/"+"GraduateStudent"+studentIDU425, rowValue);
		 at.addData("http://www."+dept2+"."+univ4+"/"+"GraduateStudent"+studentIDU426, rowValue);
		 at.addData("http://www."+dept2+"."+univ4+"/"+"GraduateStudent"+studentIDU427, rowValue);
		 at.addData("http://www."+dept2+"."+univ4+"/"+"GraduateStudent"+studentIDU428, rowValue);
		 at.addData("http://www."+dept2+"."+univ4+"/"+"GraduateStudent"+studentIDU429, rowValue);
		 
		 at.addData("http://www."+dept3+"."+univ4+"/"+"UndergraduateStudent"+studentIDU431, rowValue);
		 at.addData("http://www."+dept3+"."+univ4+"/"+"UndergraduateStudent"+studentIDU432, rowValue);
		 at.addData("http://www."+dept3+"."+univ4+"/"+"UndergraduateStudent"+studentIDU433, rowValue);
		 at.addData("http://www."+dept3+"."+univ4+"/"+"UndergraduateStudent"+studentIDU434, rowValue);
		 at.addData("http://www."+dept3+"."+univ4+"/"+"GraduateStudent"+studentIDU435, rowValue);
		 at.addData("http://www."+dept3+"."+univ4+"/"+"GraduateStudent"+studentIDU436, rowValue);
		 at.addData("http://www."+dept3+"."+univ4+"/"+"GraduateStudent"+studentIDU437, rowValue);
		 at.addData("http://www."+dept3+"."+univ4+"/"+"GraduateStudent"+studentIDU438, rowValue);

		  
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
